package io.github.rose.i18n.impl;

import io.github.rose.i18n.AbstractResourceMessageSource;
import io.github.rose.i18n.MessageSource;
import org.apache.commons.collections4.MapUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * YAML Resource {@link MessageSource} Class
 * 
 * <p>This implementation loads internationalization messages from YAML files.
 * It supports both .yml and .yaml file extensions.</p>
 * 
 * <p>Example YAML file structure:</p>
 * <pre>
 * messages:
 *   hello: "Hello, World!"
 *   user:
 *     not_found: "User not found"
 *     invalid_email: "Invalid email address"
 * </pre>
 * 
 * <p>The YAML structure will be flattened to support dot notation message codes:</p>
 * <ul>
 *   <li>messages.hello -> "Hello, World!"</li>
 *   <li>messages.user.not_found -> "User not found"</li>
 *   <li>messages.user.invalid_email -> "Invalid email address"</li>
 * </ul>
 *
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public abstract class AbstractYamlResourceMessageSource extends AbstractResourceMessageSource {

    /**
     * The default suffix of YAML message resource name
     */
    public static final String DEFAULT_YAML_RESOURCE_NAME_SUFFIX = ".yml";

    /**
     * Alternative YAML suffix
     */
    public static final String ALTERNATIVE_YAML_RESOURCE_NAME_SUFFIX = ".yaml";

    /**
     * YAML parser instance
     */
    private final Yaml yaml = new Yaml();

    /**
     * Cache for parsed YAML data to improve performance
     */
    private final ConcurrentMap<String, Map<String, Object>> yamlDataCache = new ConcurrentHashMap<>();

    /**
     * Whether to enable YAML data caching
     */
    private final boolean cacheEnabled;

    /**
     * Whether to fail fast on resource loading errors
     */
    private final boolean failFast;

    public AbstractYamlResourceMessageSource(String source) {
        this(source, true, false);
    }

    public AbstractYamlResourceMessageSource(String source, boolean cacheEnabled, boolean failFast) {
        super(source);
        this.cacheEnabled = cacheEnabled;
        this.failFast = failFast;
    }

    @Override
    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_YAML_RESOURCE_NAME_SUFFIX;
    }

    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = null;
        try {
            Map<String, Object> yamlData = loadYamlData(resource);
            if (!MapUtils.isEmpty(yamlData)) {
                messages = new HashMap<>();
                flattenYamlMap(yamlData, "", messages);
            } else {
                // Try alternative .yaml extension if .yml returns empty
                if (resource.endsWith(DEFAULT_YAML_RESOURCE_NAME_SUFFIX)) {
                    String alternativeResource = resource.replace(DEFAULT_YAML_RESOURCE_NAME_SUFFIX, ALTERNATIVE_YAML_RESOURCE_NAME_SUFFIX);
                    Map<String, Object> alternativeYamlData = loadYamlData(alternativeResource);
                    if (!MapUtils.isEmpty(alternativeYamlData)) {
                        messages = new HashMap<>();
                        flattenYamlMap(alternativeYamlData, "", messages);
                    }
                }
            }
        } catch (IOException e) {
            handleResourceLoadingError(resource, e);
            // Try alternative .yaml extension if .yml fails
            if (resource.endsWith(DEFAULT_YAML_RESOURCE_NAME_SUFFIX)) {
                String alternativeResource = resource.replace(DEFAULT_YAML_RESOURCE_NAME_SUFFIX, ALTERNATIVE_YAML_RESOURCE_NAME_SUFFIX);
                try {
                    Map<String, Object> yamlData = loadYamlData(alternativeResource);
                    if (!MapUtils.isEmpty(yamlData)) {
                        messages = new HashMap<>();
                        flattenYamlMap(yamlData, "", messages);
                    }
                } catch (IOException alternativeException) {
                    handleResourceLoadingError(alternativeResource, alternativeException);
                }
            } else {
                handleResourceLoadingError(resource, e);
            }
        }
        return messages == null ? emptyMap() : unmodifiableMap(messages);
    }

    /**
     * Load YAML data from resource
     *
     * @param resource the resource name
     * @return parsed YAML data as Map
     * @throws IOException if resource loading fails
     */
    protected Map<String, Object> loadYamlData(String resource) throws IOException {
        // Check cache first if enabled
        if (cacheEnabled) {
            Map<String, Object> cachedData = yamlDataCache.get(resource);
            if (cachedData != null) {
                logger.trace("Source '{}' cache hit for resource: {}", source, resource);
                return cachedData;
            }
        }

        Map<String, Object> combinedData = doLoadYamlData(resource);
        
        // Cache the result if enabled
        if (cacheEnabled && combinedData != null) {
            yamlDataCache.put(resource, combinedData);
            logger.trace("Source '{}' cached YAML data for resource: {}", source, resource);
        }
        
        return combinedData;
    }

    /**
     * Actually load YAML data from resources
     */
    private Map<String, Object> doLoadYamlData(String resource) throws IOException {
        List<InputStream> yamlResources = loadAllYamlResources(resource);
        logger.debug("Source '{}' loads {} YAML Resources['{}']", source, yamlResources.size(), resource);
        
        if (yamlResources.isEmpty()) {
            return emptyMap();
        }

        Map<String, Object> combinedData = new HashMap<>();
        for (InputStream yamlResource : yamlResources) {
            try (InputStream inputStream = yamlResource) {
                Map<String, Object> data = yaml.load(inputStream);
                if (data != null) {
                    combinedData.putAll(data);
                }
            } catch (Exception e) {
                handleYamlParsingError(resource, e);
            }
        }
        
        logger.debug("Source '{}' loads all YAML Resources[name :{}] : {}", source, resource, combinedData);
        return combinedData;
    }

    /**
     * Flatten nested YAML map structure to dot notation keys
     *
     * @param yamlMap the nested YAML map
     * @param prefix the current key prefix
     * @param flatMap the flattened result map
     */
    @SuppressWarnings("unchecked")
    private void flattenYamlMap(Map<String, Object> yamlMap, String prefix, Map<String, String> flatMap) {
        for (Map.Entry<String, Object> entry : yamlMap.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                // Recursively flatten nested maps
                flattenYamlMap((Map<String, Object>) value, key, flatMap);
            } else if (value != null) {
                // Convert value to string and store
                flatMap.put(key, value.toString());
            }
        }
    }

    /**
     * Handle resource loading error
     */
    protected void handleResourceLoadingError(String resource, Exception e) {
        if (failFast) {
            throw new RuntimeException("Failed to load YAML resource: " + resource, e);
        }
        logger.debug("Source '{}' could not load YAML resource '{}': {}", source, resource, e.getMessage());
    }

    /**
     * Handle YAML parsing error
     */
    protected void handleYamlParsingError(String resource, Exception e) {
        if (failFast) {
            throw new RuntimeException("Failed to parse YAML resource: " + resource, e);
        }
        logger.warn("Source '{}' failed to parse YAML resource '{}': {}", source, resource, e.getMessage());
    }

    /**
     * Clear YAML data cache
     */
    public void clearYamlCache() {
        if (cacheEnabled) {
            yamlDataCache.clear();
            logger.debug("Source '{}' YAML cache cleared", source);
        }
    }

    /**
     * Get YAML cache size
     */
    public int getYamlCacheSize() {
        return cacheEnabled ? yamlDataCache.size() : 0;
    }

    /**
     * Check if caching is enabled
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Check if fail fast is enabled
     */
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public void destroy() {
        clearYamlCache();
        super.destroy();
    }

    /**
     * Load all YAML resources for the given resource name
     *
     * @param resource the resource name
     * @return list of input streams for YAML resources
     * @throws IOException if resource loading fails
     */
    protected abstract List<InputStream> loadAllYamlResources(String resource) throws IOException;
}
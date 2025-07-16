package io.github.rose.i18n.impl;

import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.MessageSource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ClassPath-based YAML Resource {@link MessageSource} implementation
 * 
 * <p>This implementation loads YAML internationalization message files from the classpath.
 * It supports loading from multiple locations and follows a hierarchical resource resolution strategy.</p>
 * 
 * <p>Resource resolution order:</p>
 * <ol>
 *   <li>META-INF/i18n/{source}/{resourceName}</li>
 *   <li>META-INF/{source}/{resourceName}</li>
 *   <li>{source}/{resourceName}</li>
 *   <li>{resourceName}</li>
 * </ol>
 *
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class ClassPathYamlResourceMessageSource extends AbstractYamlResourceMessageSource {

    /**
     * Default base paths for resource resolution
     */
    private static final String[] DEFAULT_BASE_PATHS = {
        "META-INF/i18n/",
        "META-INF/",
        "",
        ""
    };

    private final ClassLoader classLoader;
    private final String[] basePaths;

    /**
     * Constructor with source and default ClassLoader
     *
     * @param source the message source identifier
     */
    public ClassPathYamlResourceMessageSource(String source) {
        this(source, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Constructor with source and custom ClassLoader
     *
     * @param source the message source identifier
     * @param classLoader the ClassLoader to use for resource loading
     */
    public ClassPathYamlResourceMessageSource(String source, ClassLoader classLoader) {
        this(source, classLoader, DEFAULT_BASE_PATHS);
    }

    /**
     * Constructor with source, ClassLoader and custom base paths
     *
     * @param source the message source identifier
     * @param classLoader the ClassLoader to use for resource loading
     * @param basePaths the base paths for resource resolution
     */
    public ClassPathYamlResourceMessageSource(String source, ClassLoader classLoader, String[] basePaths) {
        this(source, classLoader, basePaths, true, false);
    }

    /**
     * Constructor with full configuration
     *
     * @param source the message source identifier
     * @param classLoader the ClassLoader to use for resource loading
     * @param basePaths the base paths for resource resolution
     * @param cacheEnabled whether to enable caching
     * @param failFast whether to fail fast on errors
     */
    public ClassPathYamlResourceMessageSource(String source, ClassLoader classLoader, String[] basePaths,
                                              boolean cacheEnabled, boolean failFast) {
        super(source, cacheEnabled, failFast);
        this.classLoader = Objects.requireNonNull(classLoader, "ClassLoader cannot be null");
        this.basePaths = basePaths != null ? basePaths.clone() : DEFAULT_BASE_PATHS;
    }

    @Override
    protected String getResource(String resourceName) {
        // For ClassPath implementation, we use the resourceName as is
        // The actual resource resolution is handled in loadAllYamlResources
        return resourceName;
    }

    @Override
    protected List<InputStream> loadAllYamlResources(String resource) throws IOException {
        List<InputStream> inputStreams = new ArrayList<>();
        
        // Try to load from each base path
        for (String basePath : basePaths) {
            String resourcePath = buildResourcePath(basePath, resource);
            InputStream inputStream = classLoader.getResourceAsStream(resourcePath);
            
            if (inputStream != null) {
                inputStreams.add(inputStream);
                logger.debug("Source '{}' found YAML resource at: {}", source, resourcePath);
            } else {
                logger.trace("Source '{}' YAML resource not found at: {}", source, resourcePath);
            }
        }
        
        if (inputStreams.isEmpty()) {
            logger.debug("Source '{}' no YAML resources found for: {}", source, resource);
        }
        
        return inputStreams;
    }

    /**
     * Build the full resource path from base path and resource name
     *
     * @param basePath the base path
     * @param resource the resource name
     * @return the full resource path
     */
    private String buildResourcePath(String basePath, String resource) {
        if (StringUtils.isEmpty(basePath)) {
            return resource;
        }
        
        StringBuilder pathBuilder = new StringBuilder(basePath);
        
        // Add source directory if not already included in base path
        if (!basePath.endsWith("/")) {
            pathBuilder.append("/");
        }
        
        if (!StringUtils.isEmpty(source) && !basePath.contains(source)) {
            pathBuilder.append(source).append("/");
        }
        
        pathBuilder.append(resource);
        
        return pathBuilder.toString();
    }

    /**
     * Get the ClassLoader used for resource loading
     *
     * @return the ClassLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Get the base paths used for resource resolution
     *
     * @return copy of the base paths array
     */
    public String[] getBasePaths() {
        return basePaths.clone();
    }

    @Override
    public String toString() {
        return FormatUtils.format("{}{{source='{}', classLoader={}, basePaths={}}}", 
                getClass().getSimpleName(), source, classLoader, java.util.Arrays.toString(basePaths));
    }
}
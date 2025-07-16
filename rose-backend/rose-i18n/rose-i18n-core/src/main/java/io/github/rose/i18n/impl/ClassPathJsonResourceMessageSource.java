package io.github.rose.i18n.impl;

import io.github.rose.i18n.AbstractResourceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON-based implementation of ResourceMessageSource
 * 
 * <p>This implementation loads internationalization messages from JSON files,
 * supporting both flat and nested JSON structures:
 * <ul>
 *   <li>Flat JSON format: {"key1": "value1", "key2": "value2"}</li>
 *   <li>Nested JSON format: {"app": {"greeting": "Hello", "welcome": "Welcome"}}</li>
 *   <li>Array-based format: [{"key": "key1", "value": "value1"}]</li>
 *   <li>Support for Unicode characters and proper encoding</li>
 * </ul>
 * 
 * <p><strong>Supported JSON Formats:</strong></p>
 * <pre>{@code
 * // Flat format (recommended)
 * {
 *   "app.greeting": "Hello",
 *   "app.welcome": "Welcome {0}",
 *   "app.goodbye": "Goodbye"
 * }
 * 
 * // Nested format (with configurable separator)
 * {
 *   "app": {
 *     "greeting": "Hello",
 *     "welcome": "Welcome {0}",
 *     "goodbye": "Goodbye"
 *   }
 * }
 * 
 * // Array format
 * [
 *   {"key": "app.greeting", "value": "Hello"},
 *   {"key": "app.welcome", "value": "Welcome {0}"}
 * ]
 * }</pre>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Basic usage with classpath resources
 * ClassPathJsonResourceMessageSource source = new ClassPathJsonResourceMessageSource("app");
 * source.init();
 * 
 * // With custom configuration
 * ClassPathJsonResourceMessageSource source = ClassPathJsonResourceMessageSource.builder("app")
 *     .resourcePattern("messages/{locale}.json")
 *     .keySeparator(".")
 *     .jsonFormat(JsonFormat.NESTED)
 *     .encoding(StandardCharsets.UTF_8)
 *     .build();
 * 
 * // With file system resources
 * ClassPathJsonResourceMessageSource source = ClassPathJsonResourceMessageSource.builder("app")
 *     .resourcePattern("file:/path/to/i18n/{locale}.json")
 *     .build();
 * }</pre>
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class ClassPathJsonResourceMessageSource extends AbstractResourceMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathJsonResourceMessageSource.class);

    // JSON format types
    public enum JsonFormat {
        FLAT,    // {"key1": "value1", "key2": "value2"}
        NESTED,  // {"app": {"greeting": "Hello"}}
        ARRAY    // [{"key": "key1", "value": "value1"}]
    }

    // Default configuration
    public static final String DEFAULT_RESOURCE_PATTERN = "META-INF/i18n/{source}_{locale}.json";
    public static final String DEFAULT_KEY_SEPARATOR = ".";
    public static final JsonFormat DEFAULT_JSON_FORMAT = JsonFormat.FLAT;

    private final String resourcePattern;
    private final String keySeparator;
    private final JsonFormat jsonFormat;
    private final boolean useClassPath;
    
    private final Set<String> initializedResources = ConcurrentHashMap.newKeySet();

    /**
     * Constructor with default configuration
     */
    public ClassPathJsonResourceMessageSource(String source) {
        this(source, DEFAULT_RESOURCE_PATTERN, DEFAULT_KEY_SEPARATOR, DEFAULT_JSON_FORMAT, true);
    }

    /**
     * Full constructor for custom configuration
     */
    public ClassPathJsonResourceMessageSource(String source, String resourcePattern, String keySeparator,
                                              JsonFormat jsonFormat, boolean useClassPath) {
        super(source);
        this.resourcePattern = Objects.requireNonNull(resourcePattern, "Resource pattern cannot be null");
        this.keySeparator = keySeparator;
        this.jsonFormat = jsonFormat;
        this.useClassPath = useClassPath;
    }

    @Override
    protected String getResource(String resourceName) {
        // Build the resource path based on the pattern
        String locale = extractLocaleFromResource(resourceName);
        if (locale == null) {
            return null;
        }
        
        return resourcePattern
                .replace("{source}", getSource())
                .replace("{locale}", locale);
    }

    @Override
    protected Map<String, String> loadMessages(String resource) {
        String resourcePath = getResource(resource);
        if (resourcePath == null) {
            logger.warn("Cannot determine resource path for: {}", resource);
            return Collections.emptyMap();
        }

        try {
            String jsonContent = loadJsonContent(resourcePath);
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                logger.debug("No content found for resource: {}", resourcePath);
                return Collections.emptyMap();
            }

            Map<String, String> messages = parseJsonContent(jsonContent);
            
            if (!messages.isEmpty()) {
                initializedResources.add(resource);
                logger.debug("Loaded {} messages from JSON resource: {}", messages.size(), resourcePath);
            }
            
            return messages;
        } catch (Exception e) {
            logger.error("Failed to load JSON messages from resource: " + resourcePath, e);
            return Collections.emptyMap();
        }
    }

    private String loadJsonContent(String resourcePath) throws IOException {
        if (useClassPath) {
            return loadFromClassPath(resourcePath);
        } else {
            return loadFromFileSystem(resourcePath);
        }
    }

    private String loadFromClassPath(String resourcePath) throws IOException {
        // Remove classpath prefix if present
        String path = resourcePath.startsWith("classpath:") 
                ? resourcePath.substring("classpath:".length()) 
                : resourcePath;

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            logger.debug("Resource not found on classpath: {}", path);
            return null;
        }

        try (InputStream is = inputStream) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String loadFromFileSystem(String resourcePath) throws IOException {
        // Handle file:// URLs and regular file paths
        java.nio.file.Path filePath;
        if (resourcePath.startsWith("file://")) {
            filePath = java.nio.file.Paths.get(java.net.URI.create(resourcePath));
        } else if (resourcePath.startsWith("file:")) {
            filePath = java.nio.file.Paths.get(resourcePath.substring(5));
        } else {
            filePath = java.nio.file.Paths.get(resourcePath);
        }

        if (!java.nio.file.Files.exists(filePath)) {
            logger.debug("File not found: {}", filePath);
            return null;
        }

        return java.nio.file.Files.readString(filePath, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseJsonContent(String jsonContent) {
        jsonContent = jsonContent.trim();
        
        switch (jsonFormat) {
            case FLAT:
                return parseFlatJson(jsonContent);
            case NESTED:
                return parseNestedJson(jsonContent);
            case ARRAY:
                return parseArrayJson(jsonContent);
            default:
                throw new IllegalArgumentException("Unsupported JSON format: " + jsonFormat);
        }
    }

    private Map<String, String> parseFlatJson(String jsonContent) {
        Map<String, String> messages = new HashMap<>();
        
        if (!jsonContent.startsWith("{") || !jsonContent.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format for flat structure");
        }

        // Simple JSON parsing without external dependencies
        String content = jsonContent.substring(1, jsonContent.length() - 1).trim();
        if (content.isEmpty()) {
            return messages;
        }

        // Split by comma, but handle nested quotes
        List<String> pairs = splitJsonPairs(content);
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = unquote(keyValue[0].trim());
                String value = unquote(keyValue[1].trim());
                messages.put(key, value);
            }
        }
        
        return messages;
    }

    private Map<String, String> parseNestedJson(String jsonContent) {
        Map<String, String> messages = new HashMap<>();
        
        if (!jsonContent.startsWith("{") || !jsonContent.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format for nested structure");
        }

        // For nested JSON, we need to flatten the structure
        Map<String, Object> nestedMap = parseJsonToMap(jsonContent);
        flattenMap(nestedMap, "", messages);
        
        return messages;
    }

    private Map<String, String> parseArrayJson(String jsonContent) {
        Map<String, String> messages = new HashMap<>();
        
        if (!jsonContent.startsWith("[") || !jsonContent.endsWith("]")) {
            throw new IllegalArgumentException("Invalid JSON format for array structure");
        }

        String content = jsonContent.substring(1, jsonContent.length() - 1).trim();
        if (content.isEmpty()) {
            return messages;
        }

        // Split array elements
        List<String> elements = splitJsonArrayElements(content);
        
        for (String element : elements) {
            element = element.trim();
            if (element.startsWith("{") && element.endsWith("}")) {
                Map<String, String> elementMap = parseFlatJson(element);
                if (elementMap.containsKey("key") && elementMap.containsKey("value")) {
                    messages.put(elementMap.get("key"), elementMap.get("value"));
                }
            }
        }
        
        return messages;
    }

    private List<String> splitJsonPairs(String content) {
        List<String> pairs = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int braceLevel = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    braceLevel++;
                } else if (c == '}') {
                    braceLevel--;
                } else if (c == ',' && braceLevel == 0) {
                    pairs.add(current.toString().trim());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            pairs.add(current.toString().trim());
        }
        
        return pairs;
    }

    private List<String> splitJsonArrayElements(String content) {
        List<String> elements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int braceLevel = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    braceLevel++;
                } else if (c == '}') {
                    braceLevel--;
                } else if (c == ',' && braceLevel == 0) {
                    elements.add(current.toString().trim());
                    current = new StringBuilder();
                    continue;
                }
            }
            
            current.append(c);
        }
        
        if (current.length() > 0) {
            elements.add(current.toString().trim());
        }
        
        return elements;
    }

    private Map<String, Object> parseJsonToMap(String jsonContent) {
        // Simplified JSON parsing for nested structures
        // In a real implementation, you might want to use a proper JSON library
        Map<String, Object> result = new HashMap<>();
        
        String content = jsonContent.substring(1, jsonContent.length() - 1).trim();
        List<String> pairs = splitJsonPairs(content);
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = unquote(keyValue[0].trim());
                String valueStr = keyValue[1].trim();
                
                Object value;
                if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
                    value = parseJsonToMap(valueStr);
                } else {
                    value = unquote(valueStr);
                }
                
                result.put(key, value);
            }
        }
        
        return result;
    }

    private void flattenMap(Map<String, Object> map, String prefix, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + keySeparator + entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMap(nestedMap, key, result);
            } else if (value instanceof String) {
                result.put(key, (String) value);
            }
        }
    }

    private String unquote(String str) {
        str = str.trim();
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private String extractLocaleFromResource(String resource) {
        // Extract locale from resource name like "i18n_messages_en.properties" -> "en"
        if (resource.startsWith(DEFAULT_RESOURCE_NAME_PREFIX) && resource.endsWith(DEFAULT_RESOURCE_NAME_SUFFIX)) {
            return resource.substring(DEFAULT_RESOURCE_NAME_PREFIX.length(), 
                                    resource.length() - DEFAULT_RESOURCE_NAME_SUFFIX.length());
        }
        
        return null;
    }

    /**
     * Builder pattern for creating ClassPathJsonResourceMessageSource instances
     */
    public static Builder builder(String source) {
        return new Builder(source);
    }

    public static class Builder {
        private final String source;
        private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
        private String keySeparator = DEFAULT_KEY_SEPARATOR;
        private JsonFormat jsonFormat = DEFAULT_JSON_FORMAT;
        private boolean useClassPath = true;

        private Builder(String source) {
            this.source = Objects.requireNonNull(source, "Source cannot be null");
        }

        public Builder resourcePattern(String resourcePattern) {
            this.resourcePattern = resourcePattern;
            return this;
        }

        public Builder keySeparator(String keySeparator) {
            this.keySeparator = keySeparator;
            return this;
        }

        public Builder jsonFormat(JsonFormat jsonFormat) {
            this.jsonFormat = jsonFormat;
            return this;
        }

        public Builder useClassPath(boolean useClassPath) {
            this.useClassPath = useClassPath;
            return this;
        }

        public ClassPathJsonResourceMessageSource build() {
            return new ClassPathJsonResourceMessageSource(source, resourcePattern, keySeparator,
                    jsonFormat, useClassPath);
        }
    }
}
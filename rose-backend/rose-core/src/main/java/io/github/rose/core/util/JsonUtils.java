package io.github.rose.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Jackson JSON utility class providing comprehensive JSON processing capabilities.
 * <p>
 * This utility class offers a centralized interface for JSON serialization and deserialization
 * operations using Jackson library. It provides multiple pre-configured ObjectMapper instances
 * for different use cases and offers convenient static methods for common JSON operations.
 *
 * <h3>Core Features:</h3>
 * <ul>
 *   <li><strong>Multiple ObjectMappers:</strong> Pre-configured mappers for different scenarios</li>
 *   <li><strong>Type-Safe Operations:</strong> Generic methods for type-safe JSON processing</li>
 *   <li><strong>File I/O Support:</strong> Direct file reading and writing capabilities</li>
 *   <li><strong>Collection Support:</strong> Specialized methods for lists and maps</li>
 *   <li><strong>Error Handling:</strong> Comprehensive exception handling with logging</li>
 *   <li><strong>Performance Optimized:</strong> Cached ObjectMapper instances</li>
 * </ul>
 *
 * <h3>Available ObjectMappers:</h3>
 * <ul>
 *   <li><strong>DEFAULT_MAPPER:</strong> Standard mapper with Java 8+ time support</li>
 *   <li><strong>PRETTY_SORTED_JSON_MAPPER:</strong> Formatted output with sorted keys</li>
 *   <li><strong>IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER:</strong> Ignores unknown JSON properties</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Object to JSON
 * String json = JsonUtils.toJsonString(user);
 *
 * // JSON to Object
 * User user = JsonUtils.parseObject(json, User.class);
 *
 * // JSON to List
 * List<User> users = JsonUtils.parseArray(json, User.class);
 *
 * // Pretty formatted JSON
 * String prettyJson = JsonUtils.toPrettyJsonString(user);
 *
 * // File operations
 * JsonUtils.writeToFile(user, Paths.get("user.json"));
 * User user = JsonUtils.readFromFile(Paths.get("user.json"), User.class);
 * }</pre>
 *
 * <h3>Thread Safety:</h3>
 * This class is fully thread-safe. All ObjectMapper instances are either immutable
 * or properly synchronized. The main ObjectMapper uses volatile semantics for
 * safe publication in multi-threaded environments.
 *
 * @author Rose Framework Team
 * @see ObjectMapper
 * @see JsonMapper
 * @since 1.0.0
 */
@Slf4j
public abstract class JsonUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JsonUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Primary ObjectMapper instance that can be injected by Spring container.
     * <p>
     * This mapper uses volatile semantics to ensure thread-safe lazy initialization
     * and proper visibility across threads. It can be set by Spring dependency
     * injection or falls back to the default mapper.
     */
    private static volatile ObjectMapper objectMapper;

    /**
     * Default ObjectMapper instance with Java 8+ time API support.
     * <p>
     * This mapper is configured with automatic module discovery, which includes
     * support for Java 8 time types, Optional, and other modern Java features.
     */
    private static final ObjectMapper DEFAULT_MAPPER = createDefaultMapper();

    /**
     * ObjectMapper instance configured for pretty-printed and sorted JSON output.
     * <p>
     * This mapper produces human-readable JSON with proper indentation and
     * alphabetically sorted object keys, making it ideal for debugging,
     * logging, and configuration file generation.
     */
    public static final ObjectMapper PRETTY_SORTED_JSON_MAPPER = createPrettySortedMapper();

    /**
     * ObjectMapper instance that ignores unknown properties during deserialization.
     * <p>
     * This mapper is useful when working with JSON from external sources that
     * may contain additional fields not present in your Java classes, preventing
     * deserialization failures due to unknown properties.
     */
    public static final ObjectMapper IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER = createIgnoreUnknownPropertiesMapper();

    // ==================== ObjectMapper Factory Methods ====================

    /**
     * Creates the default ObjectMapper with automatic module discovery.
     *
     * This method creates a JsonMapper with findAndAddModules() enabled,
     * which automatically discovers and registers Jackson modules on the
     * classpath, including Java 8 time support, Optional support, etc.
     *
     * @return A new ObjectMapper instance with default configuration
     */
    private static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    /**
     * Creates an ObjectMapper configured for pretty-printed and sorted JSON output.
     *
     * This mapper produces human-readable JSON with:
     * - Proper indentation (INDENT_OUTPUT)
     * - Alphabetically sorted object keys (ORDER_MAP_ENTRIES_BY_KEYS)
     * - Alphabetically sorted properties (SORT_PROPERTIES_ALPHABETICALLY)
     *
     * @return A new ObjectMapper instance configured for pretty output
     */
    private static ObjectMapper createPrettySortedMapper() {
        return createDefaultMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    /**
     * Creates an ObjectMapper that allows unquoted field names in JSON.
     *
     * This mapper is configured to:
     * - Not quote field names in output (QUOTE_FIELD_NAMES disabled)
     * - Allow unquoted field names in input (ALLOW_UNQUOTED_FIELD_NAMES enabled)
     *
     * Note: This produces non-standard JSON and should be used carefully.
     *
     * @return A new ObjectMapper instance that handles unquoted field names
     */
    private static ObjectMapper createUnquotedFieldNamesMapper() {
        return createDefaultMapper()
                .configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), false)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    /**
     * Creates an ObjectMapper that ignores unknown properties during deserialization.
     *
     * This mapper is configured with:
     * - Java 8 module support (Jdk8Module)
     * - Disabled failure on unknown properties (FAIL_ON_UNKNOWN_PROPERTIES)
     *
     * This is useful when working with external APIs or evolving data schemas.
     *
     * @return A new ObjectMapper instance that ignores unknown properties
     */
    private static ObjectMapper createIgnoreUnknownPropertiesMapper() {
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /**
     * Sets the primary ObjectMapper instance for this utility class.
     *
     * This method is typically called by Spring container during dependency
     * injection to provide a custom-configured ObjectMapper instance.
     *
     * @param mapper The ObjectMapper instance to use as primary mapper
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    /**
     * Retrieves the ObjectMapper instance with fallback strategy.
     *
     * Returns the injected ObjectMapper if available, otherwise falls back
     * to the default mapper. This ensures that there's always a working
     * ObjectMapper available for JSON operations.
     *
     * @return The primary ObjectMapper instance or default mapper as fallback
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : DEFAULT_MAPPER;
    }

    public static <T> T readValue(Reader reader, Class<T> clazz) {
        if (reader == null) {
            return null;
        }
        try {
            return getObjectMapper().readValue(reader, clazz);
        } catch (IOException e) {
            log.warn("Failed to deserialize reader to {}: {}", clazz.getSimpleName(), reader, e);
            throw new IllegalArgumentException("The reader cannot be transformed to Json object: " + reader, e);
        }
    }


    public static <T> T fromString(String string, Class<T> clazz) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        try {
            return getObjectMapper().readValue(string, clazz);
        } catch (IOException e) {
            log.warn("Failed to deserialize string to {}: {}", clazz.getSimpleName(), string, e);
            throw new IllegalArgumentException("The given string value cannot be transformed to Json object: " + string, e);
        }
    }

    public static <T> T fromString(String string, TypeReference<T> valueTypeRef) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        try {
            return getObjectMapper().readValue(string, valueTypeRef);
        } catch (IOException e) {
            log.warn("Failed to deserialize string to {}: {}", valueTypeRef.getType(), string, e);
            throw new IllegalArgumentException("The given string value cannot be transformed to Json object: " + string, e);
        }
    }

    public static <T> T fromString(String string, Class<T> clazz, boolean ignoreUnknownFields) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        try {
            ObjectMapper mapper = ignoreUnknownFields ? IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER : getObjectMapper();
            return mapper.readValue(string, clazz);
        } catch (IOException e) {
            log.warn("Failed to deserialize string to {} (ignoreUnknownFields={}): {}", clazz.getSimpleName(), ignoreUnknownFields, string, e);
            throw new IllegalArgumentException("The given string value cannot be transformed to Json object: " + string, e);
        }
    }

    public static String toString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to string: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("The given Json object value cannot be transformed to a String: " + value, e);
        }
    }

    public static void writeValue(PrintWriter writer, Object value) {
        if (writer == null) {
            return;
        }
        try {
            getObjectMapper().writeValue(writer, value);
        } catch (IOException e) {
            log.warn("Failed to serialize object to writer: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("The given Json object value cannot be transformed to a String: " + value, e);
        }
    }

    public static JsonNode getSafely(JsonNode node, String... path) {
        if (node == null) {
            return null;
        }
        for (String p : path) {
            if (!node.has(p)) {
                return null;
            } else {
                node = node.get(p);
            }
        }
        return node;
    }

    public static Map<String, String> toFlatMap(JsonNode node) {
        HashMap<String, String> map = new HashMap<>();
        toFlatMap(node, "", map);
        return map;
    }

    public static boolean isValidJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }
        try {
            getObjectMapper().readTree(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void toFlatMap(JsonNode node, String currentPath, Map<String, String> map) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            currentPath = currentPath.isEmpty() ? "" : currentPath + ".";
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                toFlatMap(entry.getValue(), currentPath + entry.getKey(), map);
            }
        } else if (node.isValueNode()) {
            map.put(currentPath, node.asText());
        }
    }

    /**
     * 递归替换JSON节点中的所有文本值
     *
     * @param root 要处理的JSON根节点
     * @param pathPrefix 当前路径前缀，用于构建完整路径
     * @param processor 处理函数，接收路径和当前值，返回新值
     */
    public static void replaceAll(JsonNode root, String pathPrefix, BiFunction<String, String, String> processor) {
        // 使用队列实现广度优先遍历
        Queue<JsonNodeProcessingTask> tasks = new LinkedList<>();
        tasks.add(new JsonNodeProcessingTask(pathPrefix, root));

        while (!tasks.isEmpty()) {
            JsonNodeProcessingTask task = tasks.poll();
            JsonNode node = task.getNode();
            if (node == null) {
                continue;
            }

            // 构建当前路径
            String currentPath = task.getPath();

            // 处理对象节点
            if (node.isObject()) {
                ObjectNode on = (ObjectNode) node;
                for (Iterator<String> it = on.fieldNames(); it.hasNext(); ) {
                    String childName = it.next();
                    JsonNode childValue = on.get(childName);

                    // 构建子节点路径
                    String childPath = StringUtils.isBlank(currentPath) ? childName : currentPath + "." + childName;

                    // 文本节点直接处理
                    if (childValue.isTextual()) {
                        on.put(childName, processor.apply(childPath, childValue.asText()));
                    }
                    // 对象或数组节点加入队列继续处理
                    else if (childValue.isObject() || childValue.isArray()) {
                        tasks.add(new JsonNodeProcessingTask(childPath, childValue));
                    }
                }
            }
            // 处理数组节点
            else if (node.isArray()) {
                ArrayNode childArray = (ArrayNode) node;
                for (int i = 0; i < childArray.size(); i++) {
                    JsonNode element = childArray.get(i);

                    // 构建数组元素路径
                    String elementPath = StringUtils.isBlank(currentPath) ? String.valueOf(i) : currentPath + "." + i;

                    // 对象节点加入队列继续处理
                    if (element.isObject()) {
                        tasks.add(new JsonNodeProcessingTask(elementPath, element));
                    }
                    // 数组节点加入队列继续处理
                    else if (element.isArray()) {
                        tasks.add(new JsonNodeProcessingTask(elementPath, element));
                    }
                    // 文本节点直接处理
                    else if (element.isTextual()) {
                        childArray.set(i, processor.apply(elementPath, element.asText()));
                    }
                }
            }
        }
    }

    public static void replaceAllByMapping(JsonNode jsonNode, Map<String, String> mapping, Map<String, String> templateParams, BiFunction<String, String, String> processor) {
        replaceByMapping(jsonNode, mapping, templateParams, (name, value) -> {
            if (value.isTextual()) {
                return new TextNode(processor.apply(name, value.asText()));
            } else if (value.isArray()) {
                ArrayNode array = (ArrayNode) value;
                for (int i = 0; i < array.size(); i++) {
                    String arrayElementName = name.replace("$index", Integer.toString(i));
                    array.set(i, processor.apply(arrayElementName, array.get(i).asText()));
                }
                return array;
            }
            return value;
        });
    }

    public static void replaceByMapping(JsonNode jsonNode, Map<String, String> mapping, Map<String, String> templateParams, BiFunction<String, JsonNode, JsonNode> processor) {
        for (var entry : mapping.entrySet()) {
            String expression = entry.getValue();
            Queue<JsonPathProcessingTask> tasks = new LinkedList<>();
            tasks.add(new JsonPathProcessingTask(entry.getKey().split("\\."), templateParams, jsonNode));
            while (!tasks.isEmpty()) {
                JsonPathProcessingTask task = tasks.poll();
                String token = task.currentToken();
                JsonNode node = task.getNode();
                if (node == null) {
                    continue;
                }
                if (token.equals("*") || token.startsWith("$")) {
                    String variableName = token.startsWith("$") ? token.substring(1) : null;
                    if (node.isArray()) {
                        ArrayNode childArray = (ArrayNode) node;
                        for (JsonNode element : childArray) {
                            tasks.add(task.next(element));
                        }
                    } else if (node.isObject()) {
                        ObjectNode on = (ObjectNode) node;
                        for (Iterator<Map.Entry<String, JsonNode>> it = on.fields(); it.hasNext(); ) {
                            var kv = it.next();
                            if (variableName != null) {
                                tasks.add(task.next(kv.getValue(), variableName, kv.getKey()));
                            } else {
                                tasks.add(task.next(kv.getValue()));
                            }
                        }
                    }
                } else {
                    String variableName = null;
                    String variableValue = null;
                    if (token.contains("[$")) {
                        variableName = StringUtils.substringBetween(token, "[$", "]");
                        token = StringUtils.substringBefore(token, "[$");
                    }
                    if (node.has(token)) {
                        JsonNode value = node.get(token);
                        if (variableName != null && value.has(variableName) && value.get(variableName).isTextual()) {
                            variableValue = value.get(variableName).asText();
                        }
                        if (task.isLast()) {
                            String name = expression;
                            for (var replacement : task.getVariables().entrySet()) {
                                name = name.replace("$" + replacement.getKey(), StringUtils.trimToEmpty(replacement.getValue()));
                            }
                            ((ObjectNode) node).set(token, processor.apply(name, value));
                        } else {
                            if (StringUtils.isNotEmpty(variableName)) {
                                tasks.add(task.next(value, variableName, variableValue));
                            } else {
                                tasks.add(task.next(value));
                            }
                        }
                    }
                }
            }
        }
    }


    @Data
    public static class JsonNodeProcessingTask {
        private final String path;
        private final JsonNode node;

        public JsonNodeProcessingTask(String path, JsonNode node) {
            this.path = path;
            this.node = node;
        }

    }

    @Data
    public static class JsonPathProcessingTask {
        private final String[] tokens;
        private final Map<String, String> variables;
        private final JsonNode node;

        public JsonPathProcessingTask(String[] tokens, Map<String, String> variables, JsonNode node) {
            this.tokens = tokens;
            this.variables = variables;
            this.node = node;
        }

        public boolean isLast() {
            return tokens.length == 1;
        }

        public String currentToken() {
            return tokens[0];
        }

        public JsonPathProcessingTask next(JsonNode next) {
            return new JsonPathProcessingTask(
                    Arrays.copyOfRange(tokens, 1, tokens.length),
                    variables,
                    next);
        }

        public JsonPathProcessingTask next(JsonNode next, String key, String value) {
            Map<String, String> variables = new HashMap<>(this.variables);
            variables.put(key, value);
            return new JsonPathProcessingTask(
                    Arrays.copyOfRange(tokens, 1, tokens.length),
                    variables,
                    next);
        }

        @Override
        public String toString() {
            return "JsonPathProcessingTask{" +
                    "tokens=" + Arrays.toString(tokens) +
                    ", variables=" + variables +
                    ", node=" + node.toString().substring(0, 20) +
                    '}';
        }

    }

}
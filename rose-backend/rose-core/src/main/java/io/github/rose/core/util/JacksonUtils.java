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
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

@Slf4j
public abstract class JacksonUtils {

    private JacksonUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 主要的ObjectMapper实例，由Spring容器注入
     * 使用volatile确保线程安全
     */
    private static volatile ObjectMapper objectMapper;

    /**
     * 支持Java时间API的ObjectMapper实例
     */
    private static final ObjectMapper DEFAULT_MAPPER = createDefaultMapper();

    /**
     * 用于格式化输出的ObjectMapper实例
     */
    public static final ObjectMapper PRETTY_SORTED_JSON_MAPPER = createPrettySortedMapper();

    /**
     * 忽略未知属性的ObjectMapper实例
     */
    public static final ObjectMapper IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER = createIgnoreUnknownPropertiesMapper();

    // ==================== ObjectMapper 创建方法 ====================

    /**
     * 创建默认的ObjectMapper
     */
    private static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    /**
     * 创建格式化输出的ObjectMapper
     */
    private static ObjectMapper createPrettySortedMapper() {
        return createDefaultMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    /**
     * 创建允许不带引号字段名的ObjectMapper
     */
    private static ObjectMapper createUnquotedFieldNamesMapper() {
        return createDefaultMapper()
                .configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), false)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    /**
     * 创建忽略未知属性的ObjectMapper
     */
    private static ObjectMapper createIgnoreUnknownPropertiesMapper() {
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /**
     * 设置主要的ObjectMapper实例
     * 通常由Spring容器调用
     *
     * @param mapper ObjectMapper实例
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    /**
     * 获取ObjectMapper实例
     * 优先使用注入的实例，否则使用默认实例
     *
     * @return ObjectMapper实例
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

    public static void replaceAll(JsonNode root, String pathPrefix, BiFunction<String, String, String> processor) {
        Queue<JsonNodeProcessingTask> tasks = new LinkedList<>();
        tasks.add(new JsonNodeProcessingTask(pathPrefix, root));
        while (!tasks.isEmpty()) {
            JsonNodeProcessingTask task = tasks.poll();
            JsonNode node = task.getNode();
            if (node == null) {
                continue;
            }
            String currentPath = StringUtils.isBlank(task.getPath()) ? "" : (task.getPath() + ".");
            if (node.isObject()) {
                ObjectNode on = (ObjectNode) node;
                for (Iterator<String> it = on.fieldNames(); it.hasNext(); ) {
                    String childName = it.next();
                    JsonNode childValue = on.get(childName);
                    if (childValue.isTextual()) {
                        on.put(childName, processor.apply(currentPath + childName, childValue.asText()));
                    } else if (childValue.isObject() || childValue.isArray()) {
                        tasks.add(new JsonNodeProcessingTask(currentPath + childName, childValue));
                    }
                }
            } else if (node.isArray()) {
                ArrayNode childArray = (ArrayNode) node;
                for (int i = 0; i < childArray.size(); i++) {
                    JsonNode element = childArray.get(i);
                    if (element.isObject()) {
                        tasks.add(new JsonNodeProcessingTask(currentPath + "." + i, element));
                    } else if (element.isTextual()) {
                        childArray.set(i, processor.apply(currentPath + "." + i, element.asText()));
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
package io.github.rose.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
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

    // ==================== 公共方法 ====================

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

    // ==================== 类型转换方法 ====================

    /**
     * 将对象转换为指定类型
     *
     * @param fromValue   源对象
     * @param toValueType 目标类型
     * @param <T>         目标类型泛型
     * @return 转换后的对象
     * @throws IllegalArgumentException 转换失败时抛出
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        if (fromValue == null) {
            return null;
        }
        try {
            return getObjectMapper().convertValue(fromValue, toValueType);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert value to {}: {}", toValueType.getSimpleName(), fromValue, e);
            throw new IllegalArgumentException("The given object value cannot be converted to " + toValueType + ": " + fromValue, e);
        }
    }

    /**
     * 将对象转换为指定类型（使用TypeReference）
     *
     * @param fromValue      源对象
     * @param toValueTypeRef 目标类型引用
     * @param <T>            目标类型泛型
     * @return 转换后的对象
     * @throws IllegalArgumentException 转换失败时抛出
     */
    public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        if (fromValue == null) {
            return null;
        }
        try {
            return getObjectMapper().convertValue(fromValue, toValueTypeRef);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to convert value to {}: {}", toValueTypeRef.getType(), fromValue, e);
            throw new IllegalArgumentException("The given object value cannot be converted to " + toValueTypeRef + ": " + fromValue, e);
        }
    }

    // ==================== 字符串反序列化方法 ====================

    /**
     * 从JSON字符串反序列化为指定类型对象
     *
     * @param string JSON字符串
     * @param clazz  目标类型
     * @param <T>    目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
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

    /**
     * 从JSON字符串反序列化为指定类型对象（使用TypeReference）
     *
     * @param string       JSON字符串
     * @param valueTypeRef 目标类型引用
     * @param <T>          目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
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

    /**
     * 从JSON字符串反序列化为指定类型对象（使用JavaType）
     *
     * @param string   JSON字符串
     * @param javaType 目标Java类型
     * @param <T>      目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
    public static <T> T fromString(String string, JavaType javaType) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        try {
            return getObjectMapper().readValue(string, javaType);
        } catch (IOException e) {
            log.warn("Failed to deserialize string to {}: {}", javaType, string, e);
            throw new IllegalArgumentException("The given String value cannot be transformed to Json object: " + string, e);
        }
    }

    /**
     * 从JSON字符串反序列化为指定类型对象（忽略未知字段）
     *
     * @param string              JSON字符串
     * @param clazz               目标类型
     * @param ignoreUnknownFields 是否忽略未知字段
     * @param <T>                 目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
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

    // ==================== 字节数组反序列化方法 ====================

    /**
     * 从字节数组反序列化为指定类型对象
     *
     * @param bytes 字节数组
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
    public static <T> T fromBytes(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return getObjectMapper().readValue(bytes, clazz);
        } catch (IOException e) {
            log.warn("Failed to deserialize bytes to {}: {} bytes", clazz.getSimpleName(), bytes.length, e);
            throw new IllegalArgumentException("The given byte[] value cannot be transformed to Json object", e);
        }
    }

    /**
     * 从字节数组反序列化为指定类型对象（使用TypeReference）
     *
     * @param bytes        字节数组
     * @param valueTypeRef 目标类型引用
     * @param <T>          目标类型泛型
     * @return 反序列化后的对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
    public static <T> T fromBytes(byte[] bytes, TypeReference<T> valueTypeRef) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return getObjectMapper().readValue(bytes, valueTypeRef);
        } catch (IOException e) {
            log.warn("Failed to deserialize bytes to {}: {} bytes", valueTypeRef.getType(), bytes.length, e);
            throw new IllegalArgumentException("The given byte[] value cannot be transformed to Json object", e);
        }
    }

    /**
     * 从字节数组反序列化为JsonNode
     *
     * @param bytes 字节数组
     * @return JsonNode对象
     * @throws IllegalArgumentException 反序列化失败时抛出
     */
    public static JsonNode fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return getObjectMapper().readTree(bytes);
        } catch (IOException e) {
            log.warn("Failed to deserialize bytes to JsonNode: {} bytes", bytes.length, e);
            throw new IllegalArgumentException("The given byte[] value cannot be transformed to Json object", e);
        }
    }

    // ==================== 序列化方法 ====================

    /**
     * 将对象序列化为JSON字符串
     *
     * @param value 要序列化的对象
     * @return JSON字符串
     * @throws IllegalArgumentException 序列化失败时抛出
     */
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

    /**
     * 将对象序列化为格式化的JSON字符串
     *
     * @param value 要序列化的对象
     * @return 格式化的JSON字符串
     * @throws IllegalArgumentException 序列化失败时抛出
     */
    public static String toPrettyString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return PRETTY_SORTED_JSON_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to pretty string: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("Failed to serialize object to pretty string", e);
        }
    }

    /**
     * 将JSON字符串转换为纯文本（去除引号）
     *
     * @param data JSON字符串
     * @return 纯文本字符串
     */
    public static String toPlainText(String data) {
        if (data == null || data.trim().isEmpty()) {
            return data;
        }

        // 检查是否为JSON字符串格式
        if (data.length() >= 2 && data.startsWith("\"") && data.endsWith("\"")) {
            try {
                String result = fromString(data, String.class);
                log.trace("Trimming double quotes. Before: [{}], after: [{}]", data, result);
                return result;
            } catch (Exception e) {
                log.trace("Failed to parse as JSON string, returning original: {}", data);
                return data;
            }
        }
        return data;
    }

    public static <T> T treeToValue(JsonNode node, Class<T> clazz) {
        try {
            return getObjectMapper().treeToValue(node, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't convert value: " + node.toString(), e);
        }
    }

    public static JsonNode toJsonNode(String value) {
        return toJsonNode(value, getObjectMapper());
    }

    public static JsonNode toJsonNode(String value, ObjectMapper mapper) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return mapper.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static <T> T readValue(String file, CollectionType clazz) {
        try {
            return getObjectMapper().readValue(file, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read file: " + file, e);
        }
    }

    public static <T> T readValue(String object, TypeReference<T> clazz) {
        try {
            return getObjectMapper().readValue(object, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read object: " + object, e);
        }
    }

    public static <T> T readValue(File file, TypeReference<T> clazz) {
        try {
            return getObjectMapper().readValue(file, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read file: " + file, e);
        }
    }

    public static <T> T readValue(File file, Class<T> clazz) {
        try {
            return getObjectMapper().readValue(file, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read file: " + file, e);
        }
    }

    public static JsonNode readTree(Path file) {
        try {
            return getObjectMapper().readTree(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't read file: " + file, e);
        }
    }

    public static JsonNode readTree(File value) {
        try {
            return value != null ? getObjectMapper().readTree(value) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException("The given File object value: "
                    + value + " cannot be transformed to a JsonNode", e);
        }
    }

    public static JsonNode readTree(InputStream value) {
        try {
            return value != null ? getObjectMapper().readTree(value) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException("The given InputStream value: "
                    + value + " cannot be transformed to a JsonNode", e);
        }
    }

    public static ObjectNode newObjectNode() {
        return newObjectNode(getObjectMapper());
    }

    public static ObjectNode newObjectNode(ObjectMapper mapper) {
        return mapper.createObjectNode();
    }

    public static ArrayNode newArrayNode() {
        return newArrayNode(getObjectMapper());
    }

    public static ArrayNode newArrayNode(ObjectMapper mapper) {
        return mapper.createArrayNode();
    }

    // ==================== 工具方法 ====================

    /**
     * 深度克隆对象（通过JSON序列化/反序列化）
     *
     * @param value 要克隆的对象
     * @param <T>   对象类型
     * @return 克隆后的对象
     * @throws IllegalArgumentException 克隆失败时抛出
     */
    public static <T> T clone(T value) {
        if (value == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Class<T> valueClass = (Class<T>) value.getClass();
            return fromString(toString(value), valueClass);
        } catch (Exception e) {
            log.warn("Failed to clone object: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("Failed to clone object", e);
        }
    }

    /**
     * 将对象转换为JsonNode
     *
     * @param value 要转换的对象
     * @param <T>   对象类型
     * @return JsonNode对象
     */
    public static <T> JsonNode valueToTree(T value) {
        if (value == null) {
            return null;
        }
        try {
            return getObjectMapper().valueToTree(value);
        } catch (Exception e) {
            log.warn("Failed to convert value to tree: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("Failed to convert value to JsonNode", e);
        }
    }

    /**
     * 将对象序列化为字节数组
     *
     * @param value 要序列化的对象
     * @param <T>   对象类型
     * @return 字节数组
     * @throws IllegalArgumentException 序列化失败时抛出
     */
    public static <T> byte[] writeValueAsBytes(T value) {
        if (value == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to bytes: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException("The given Json object value cannot be transformed to bytes: " + value, e);
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

    public static ObjectNode asObject(JsonNode node) {
        return node != null && node.isObject() ? ((ObjectNode) node) : newObjectNode();
    }

    public static Map<String, String> toFlatMap(JsonNode node) {
        HashMap<String, String> map = new HashMap<>();
        toFlatMap(node, "", map);
        return map;
    }

    public static <T> T readValue(Reader reader, Class<T> clazz) {
        try {
            return reader != null ? getObjectMapper().readValue(reader, clazz) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid request payload", e);
        }
    }

    public static <T> void writeValue(Writer writer, T value) {
        try {
            getObjectMapper().writeValue(writer, value);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given writer value: "
                    + writer + "cannot be wrote", e);
        }
    }

    /**
     * 构造集合类型
     *
     * @param collectionClass 集合类型
     * @param elementClass    元素类型
     * @return JavaType对象
     */
    public static JavaType constructCollectionType(Class<?> collectionClass, Class<?> elementClass) {
        return getObjectMapper().getTypeFactory().constructCollectionType((Class<? extends Collection>) collectionClass, elementClass);
    }

    /**
     * 构造Map类型
     *
     * @param mapClass   Map类型
     * @param keyClass   键类型
     * @param valueClass 值类型
     * @return JavaType对象
     */
    public static JavaType constructMapType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return getObjectMapper().getTypeFactory().constructMapType((Class<? extends Map>) mapClass, keyClass, valueClass);
    }

    /**
     * 检查字符串是否为有效的JSON
     *
     * @param jsonString JSON字符串
     * @return 是否为有效JSON
     */
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

    /**
     * 安全地获取JSON字符串的大小（字符数）
     *
     * @param jsonString JSON字符串
     * @return 字符数，如果为null则返回0
     */
    public static int getJsonSize(String jsonString) {
        return jsonString != null ? jsonString.length() : 0;
    }

    /**
     * 压缩JSON字符串（移除空白字符）
     *
     * @param jsonString JSON字符串
     * @return 压缩后的JSON字符串
     * @throws IllegalArgumentException 如果不是有效的JSON
     */
    public static String compactJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        try {
            JsonNode node = getObjectMapper().readTree(jsonString);
            return getObjectMapper().writeValueAsString(node);
        } catch (Exception e) {
            log.warn("Failed to compact JSON string: {}", jsonString, e);
            throw new IllegalArgumentException("Invalid JSON string", e);
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
package io.github.rosestack.core.util;

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
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class JsonUtils {
    public static final ObjectMapper PRETTY_SORTED_JSON_MAPPER = createPrettySortedMapper();
    public static final ObjectMapper IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER = createIgnoreUnknownPropertiesMapper();
    private static final ObjectMapper DEFAULT_MAPPER = createDefaultMapper();
    private static volatile ObjectMapper objectMapper;

    private JsonUtils() {}

    private static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    private static ObjectMapper createPrettySortedMapper() {
        return createDefaultMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    private static ObjectMapper createUnquotedFieldNamesMapper() {
        return createDefaultMapper()
                .configure(JsonWriteFeature.QUOTE_FIELD_NAMES.mappedFeature(), false)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    private static ObjectMapper createIgnoreUnknownPropertiesMapper() {
        return JsonMapper.builder()
                .addModule(new Jdk8Module())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : DEFAULT_MAPPER;
    }

    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    public static <T> T fromString(String string, Class<T> clazz) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        try {
            return getObjectMapper().readValue(string, clazz);
        } catch (IOException e) {
            log.warn("Failed to deserialize string to {}: {}", clazz.getSimpleName(), string, e);
            throw new IllegalArgumentException(
                    "The given string value cannot be transformed to Json object: " + string, e);
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
            throw new IllegalArgumentException(
                    "The given string value cannot be transformed to Json object: " + string, e);
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
            log.warn(
                    "Failed to deserialize string to {} (ignoreUnknownFields={}): {}",
                    clazz.getSimpleName(),
                    ignoreUnknownFields,
                    string,
                    e);
            throw new IllegalArgumentException(
                    "The given string value cannot be transformed to Json object: " + string, e);
        }
    }

    public static String toString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn(
                    "Failed to serialize object to string: {}", value.getClass().getSimpleName(), e);
            throw new IllegalArgumentException(
                    "The given Json object value cannot be transformed to a String: " + value, e);
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
     * 通用字段处理方法 递归遍历对象的每个属性，使用提供的处理函数对字段进行处理
     *
     * @param object         需要处理的对象
     * @param fieldProcessor 字段处理函数，接收字段名和字段值，返回处理后的值
     * @param <T>            对象类型
     * @return 处理后的对象
     */
    public static <T> T processFields(T object, BiFunction<String, JsonNode, JsonNode> fieldProcessor) {
        if (object == null || fieldProcessor == null) {
            return object;
        }

        try {
            ObjectMapper mapper = getObjectMapper();
            // 将对象转换为JsonNode
            JsonNode jsonNode = mapper.valueToTree(object);

            // 递归处理JsonNode
            processJsonNode(jsonNode, fieldProcessor);

            // 转换回原对象类型
            @SuppressWarnings("unchecked")
            Class<T> clazz = (Class<T>) object.getClass();
            return mapper.treeToValue(jsonNode, clazz);

        } catch (Exception e) {
            log.warn("字段处理失败，返回原对象", e);
            return object;
        }
    }

    /**
     * 递归处理JsonNode，使用字段处理函数对每个字段进行处理
     *
     * @param node           JSON节点
     * @param fieldProcessor 字段处理函数
     */
    private static void processJsonNode(JsonNode node, BiFunction<String, JsonNode, JsonNode> fieldProcessor) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<String> fieldNames = objectNode.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = objectNode.get(fieldName);

                // 使用处理函数处理字段
                JsonNode processedValue = fieldProcessor.apply(fieldName, fieldValue);
                if (processedValue != null && !processedValue.equals(fieldValue)) {
                    objectNode.set(fieldName, processedValue);
                } else {
                    // 递归处理子节点
                    processJsonNode(fieldValue, fieldProcessor);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode arrayElement : arrayNode) {
                processJsonNode(arrayElement, fieldProcessor);
            }
        }
    }

    /**
     * 创建自定义脱敏处理器 对指定字段进行脱敏，使用自定义的脱敏值
     *
     * @param maskValue       脱敏值
     * @param sensitiveFields 敏感字段名称数组
     * @return 脱敏处理器
     */
    public static BiFunction<String, JsonNode, JsonNode> createCustomMaskProcessor(
            String maskValue, String... sensitiveFields) {
        Set<String> sensitiveFieldSet = new HashSet<>(Arrays.asList(sensitiveFields));
        return (fieldName, fieldValue) -> {
            if (sensitiveFieldSet.contains(fieldName) && fieldValue.isTextual()) {
                return new TextNode(maskValue);
            }
            return null; // 返回null表示不处理，继续递归
        };
    }

    /**
     * 创建字段替换处理器 根据映射关系替换字段值
     *
     * @param replacements 字段名到新值的映射
     * @return 替换处理器
     */
    public static BiFunction<String, JsonNode, JsonNode> createReplaceProcessor(Map<String, String> replacements) {
        return (fieldName, fieldValue) -> {
            if (replacements.containsKey(fieldName) && fieldValue.isTextual()) {
                return new TextNode(replacements.get(fieldName));
            }
            return null; // 返回null表示不处理，继续递归
        };
    }

    /**
     * 创建字段转换处理器 对指定字段的值进行转换
     *
     * @param fieldName   字段名
     * @param transformer 值转换函数
     * @return 转换处理器
     */
    public static BiFunction<String, JsonNode, JsonNode> createTransformProcessor(
            String fieldName, Function<String, String> transformer) {
        return (name, value) -> {
            if (fieldName.equals(name) && value.isTextual()) {
                String transformedValue = transformer.apply(value.asText());
                return new TextNode(transformedValue);
            }
            return null; // 返回null表示不处理，继续递归
        };
    }

    /**
     * 创建组合处理器 将多个处理器组合在一起，按顺序执行
     *
     * @param processors 处理器数组
     * @return 组合处理器
     */
    @SafeVarargs
    public static BiFunction<String, JsonNode, JsonNode> createCompositeProcessor(
            BiFunction<String, JsonNode, JsonNode>... processors) {
        return (fieldName, fieldValue) -> {
            JsonNode currentValue = fieldValue;
            for (BiFunction<String, JsonNode, JsonNode> processor : processors) {
                JsonNode processedValue = processor.apply(fieldName, currentValue);
                if (processedValue != null) {
                    currentValue = processedValue;
                }
            }
            return currentValue.equals(fieldValue) ? null : currentValue;
        };
    }
}

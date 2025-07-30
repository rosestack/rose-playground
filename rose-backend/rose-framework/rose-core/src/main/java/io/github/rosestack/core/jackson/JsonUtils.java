package io.github.rosestack.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public abstract class JsonUtils {
    private JsonUtils() {
    }

    private static volatile ObjectMapper objectMapper;

    private static final ObjectMapper DEFAULT_MAPPER = createDefaultMapper();

    public static final ObjectMapper PRETTY_SORTED_JSON_MAPPER = createPrettySortedMapper();

    public static final ObjectMapper IGNORE_UNKNOWN_PROPERTIES_JSON_MAPPER = createIgnoreUnknownPropertiesMapper();

    private static ObjectMapper createDefaultMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
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

    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper != null ? objectMapper : DEFAULT_MAPPER;
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

    public static Map<String, String> toFlatMap(JsonNode node) {
        HashMap<String, String> map = new HashMap<>();
        toFlatMap(node, "", map);
        return map;
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
}
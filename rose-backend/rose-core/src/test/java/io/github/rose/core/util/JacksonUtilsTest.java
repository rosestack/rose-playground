package io.github.rose.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JacksonUtil测试类
 * 验证优化后的Jackson工具类功能
 */
class JacksonUtilsTest {

    private TestObject testObject;

    @BeforeEach
    void setUp() {
        testObject = new TestObject();
        testObject.setId(1L);
        testObject.setName("Test Name");
        testObject.setActive(true);
        testObject.setCreatedAt(LocalDateTime.now());
        testObject.setTags(Arrays.asList("tag1", "tag2", "tag3"));
    }

    @Test
    void testToString() {
        String json = JacksonUtils.toString(testObject);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Test Name\""));

        // 测试null值
        assertNull(JacksonUtils.toString(null));
    }

    @Test
    void testFromString() {
        String json = JacksonUtils.toString(testObject);
        TestObject result = JacksonUtils.fromString(json, TestObject.class);

        assertNotNull(result);
        assertEquals(testObject.getId(), result.getId());
        assertEquals(testObject.getName(), result.getName());
        assertEquals(testObject.isActive(), result.isActive());

        // 测试null和空字符串
        assertNull(JacksonUtils.fromString(null, TestObject.class));
        assertNull(JacksonUtils.fromString("", TestObject.class));
        assertNull(JacksonUtils.fromString("   ", TestObject.class));
    }

    @Test
    void testFromStringWithTypeReference() {
        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
        String json = JacksonUtils.toString(tags);

        List<String> result = JacksonUtils.fromString(json, new TypeReference<List<String>>() {
        });

        assertNotNull(result);
        assertEquals(tags.size(), result.size());
        assertEquals(tags, result);
    }

    @Test
    void testFromStringIgnoreUnknownFields() {
        String jsonWithExtraField = "{\"id\":1,\"name\":\"Test\",\"unknownField\":\"value\"}";

        // 不忽略未知字段应该抛出异常
        assertThrows(IllegalArgumentException.class,
                () -> JacksonUtils.fromString(jsonWithExtraField, TestObject.class, false));

        // 忽略未知字段应该成功
        TestObject result = JacksonUtils.fromString(jsonWithExtraField, TestObject.class, true);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
    }

    @Test
    void testFromBytes() {
        String json = JacksonUtils.toString(testObject);
        byte[] bytes = json.getBytes();

        TestObject result = JacksonUtils.fromBytes(bytes, TestObject.class);
        assertNotNull(result);
        assertEquals(testObject.getId(), result.getId());

        // 测试null和空数组
        assertNull(JacksonUtils.fromBytes(null, TestObject.class));
        assertNull(JacksonUtils.fromBytes(new byte[0], TestObject.class));
    }

    @Test
    void testWriteValueAsBytes() {
        byte[] bytes = JacksonUtils.writeValueAsBytes(testObject);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        // 测试null值
        assertNull(JacksonUtils.writeValueAsBytes(null));
    }

    @Test
    void testToPrettyString() {
        String prettyJson = JacksonUtils.toPrettyString(testObject);
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n")); // 应该包含换行符
        assertTrue(prettyJson.contains("  ")); // 应该包含缩进

        // 测试null值
        assertNull(JacksonUtils.toPrettyString(null));
    }

    @Test
    void testToPlainText() {
        String quotedString = "\"Hello World\"";
        String result = JacksonUtils.toPlainText(quotedString);
        assertEquals("Hello World", result);

        // 测试非JSON字符串
        String normalString = "Hello World";
        assertEquals(normalString, JacksonUtils.toPlainText(normalString));

        // 测试null和空字符串
        assertNull(JacksonUtils.toPlainText(null));
        assertEquals("", JacksonUtils.toPlainText(""));
    }

    @Test
    void testClone() {
        TestObject cloned = JacksonUtils.clone(testObject);
        assertNotNull(cloned);
        assertNotSame(testObject, cloned); // 不是同一个对象
        assertEquals(testObject.getId(), cloned.getId());
        assertEquals(testObject.getName(), cloned.getName());

        // 测试null值
        assertNull(JacksonUtils.clone(null));
    }

    @Test
    void testValueToTree() {
        JsonNode node = JacksonUtils.valueToTree(testObject);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals(testObject.getId().longValue(), node.get("id").asLong());
        assertEquals(testObject.getName(), node.get("name").asText());

        // 测试null值
        assertNull(JacksonUtils.valueToTree(null));
    }

    @Test
    void testConvertValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "Test");
        map.put("active", true);

        TestObject result = JacksonUtils.convertValue(map, TestObject.class);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
        assertTrue(result.isActive());

        // 测试null值
        assertNull(JacksonUtils.convertValue(null, TestObject.class));
    }

    @Test
    void testIsValidJson() {
        assertTrue(JacksonUtils.isValidJson("{\"name\":\"test\"}"));
        assertTrue(JacksonUtils.isValidJson("[1,2,3]"));
        assertTrue(JacksonUtils.isValidJson("\"string\""));
        assertTrue(JacksonUtils.isValidJson("123"));
        assertTrue(JacksonUtils.isValidJson("true"));

        assertFalse(JacksonUtils.isValidJson("{invalid json}"));
        assertFalse(JacksonUtils.isValidJson(""));
        assertFalse(JacksonUtils.isValidJson(null));
        assertFalse(JacksonUtils.isValidJson("   "));
    }

    @Test
    void testCompactJson() {
        String prettyJson = "{\n  \"name\" : \"test\",\n  \"id\" : 1\n}";
        String compactJson = JacksonUtils.compactJson(prettyJson);

        assertNotNull(compactJson);
        assertFalse(compactJson.contains("\n"));
        assertFalse(compactJson.contains("  "));
        assertTrue(compactJson.contains("\"name\":\"test\""));

        // 测试无效JSON
        assertThrows(IllegalArgumentException.class,
                () -> JacksonUtils.compactJson("{invalid json}"));
    }

    @Test
    void testGetJsonSize() {
        assertEquals(0, JacksonUtils.getJsonSize(null));
        assertEquals(0, JacksonUtils.getJsonSize(""));
        assertEquals(5, JacksonUtils.getJsonSize("hello"));
    }

    @Test
    void testConstructCollectionType() {
        JavaType collectionType = JacksonUtils.constructCollectionType(List.class, String.class);
        assertNotNull(collectionType);
        assertTrue(collectionType.isCollectionLikeType());
    }

    @Test
    void testConstructMapType() {
        JavaType mapType = JacksonUtils.constructMapType(Map.class, String.class, Object.class);
        assertNotNull(mapType);
        assertTrue(mapType.isMapLikeType());
    }

    @Test
    void testSetObjectMapper() {
        ObjectMapper customMapper = JacksonUtils.getObjectMapper();
        JacksonUtils.setObjectMapper(customMapper);

        String result = JacksonUtils.toString(testObject);
        assertNotNull(result);
    }

    // 测试用的内部类
    public static class TestObject {
        private Long id;
        private String name;
        private boolean active;
        private LocalDateTime createdAt;
        private List<String> tags;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
}

package io.github.rosestack.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilsTest {
    @Test
    void testReplaceAll() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("name", "test");
        root.put("age", "25");

        ObjectNode address = mapper.createObjectNode();
        address.put("city", "Beijing");
        address.put("street", "Main St");
        root.set("address", address);

        ArrayNode hobbies = mapper.createArrayNode();
        hobbies.add("reading");
        hobbies.add("swimming");
        root.set("hobbies", hobbies);

        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                path + ":" + value.toUpperCase();

        // 执行方法
        JsonUtils.replaceAll(root, "", processor);

        // 验证结果
        assertEquals("name:TEST", root.get("name").asText());
        assertEquals("age:25", root.get("age").asText());
        assertEquals("address.city:BEIJING", root.get("address").get("city").asText());
        assertEquals("address.street:MAIN ST", root.get("address").get("street").asText());
        assertEquals("hobbies.0:READING", root.get("hobbies").get(0).asText());
        assertEquals("hobbies.1:SWIMMING", root.get("hobbies").get(1).asText());
    }

    @Test
    void testReplaceAllWithPathPrefix() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("key", "value");

        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                path + ":" + value;

        // 执行方法
        JsonUtils.replaceAll(root, "prefix", processor);

        // 验证结果
        assertEquals("prefix.key:value", root.get("key").asText());
    }

    @Test
    void testReplaceAllWithEmptyNode() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                path + ":" + value;

        // 执行方法 - 不应抛出异常
        JsonUtils.replaceAll(root, "", processor);
    }

    @Test
    void testReplaceAllWithNullNode() {
        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                path + ":" + value;

        // 执行方法 - 不应抛出异常
        JsonUtils.replaceAll(null, "", processor);
    }

    @Test
    void testReplaceAllByMappingBasic() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("name", "john");
        root.put("email", "john@example.com");

        ObjectNode profile = mapper.createObjectNode();
        profile.put("title", "developer");
        profile.put("department", "engineering");
        root.set("profile", profile);

        // 准备映射和参数
        Map<String, String> mapping = new HashMap<>();
        mapping.put("name", "user.name");
        mapping.put("profile.title", "user.job.title");

        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器 - 转换为大写
        BiFunction<String, String, String> processor = (path, value) ->
                value.toUpperCase();

        // 执行方法
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果
        assertEquals("JOHN", root.get("name").asText());
        assertEquals("john@example.com", root.get("email").asText()); // 未映射的字段保持不变
        assertEquals("DEVELOPER", root.get("profile").get("title").asText());
        assertEquals("engineering", root.get("profile").get("department").asText()); // 未映射的字段保持不变
    }

    @Test
    void testReplaceAllByMappingWithArray() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ArrayNode tags = mapper.createArrayNode();
        tags.add("java");
        tags.add("spring");
        tags.add("boot");
        root.set("tags", tags);

        // 准备映射和参数
        Map<String, String> mapping = new HashMap<>();
        mapping.put("tags", "tag.$index"); // 使用 $index 占位符

        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器 - 添加前缀和索引
        BiFunction<String, String, String> processor = (path, value) ->
                "[" + path + "] " + value.toUpperCase();

        // 执行方法
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果
        assertEquals("[tag.0] JAVA", root.get("tags").get(0).asText());
        assertEquals("[tag.1] SPRING", root.get("tags").get(1).asText());
        assertEquals("[tag.2] BOOT", root.get("tags").get(2).asText());
    }

    @Test
    void testReplaceAllByMappingWithWildcard() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ObjectNode user1 = mapper.createObjectNode();
        user1.put("name", "alice");
        user1.put("status", "active");

        ObjectNode user2 = mapper.createObjectNode();
        user2.put("name", "bob");
        user2.put("status", "inactive");

        root.set("user1", user1);
        root.set("user2", user2);

        // 准备映射和参数 - 使用通配符匹配所有用户的name字段
        Map<String, String> mapping = new HashMap<>();
        mapping.put("*.name", "user.name");

        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器 - 添加前缀
        BiFunction<String, String, String> processor = (path, value) ->
                "USER:" + value.toUpperCase();

        // 执行方法
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果
        assertEquals("USER:ALICE", root.get("user1").get("name").asText());
        assertEquals("active", root.get("user1").get("status").asText()); // 未映射的字段保持不变
        assertEquals("USER:BOB", root.get("user2").get("name").asText());
        assertEquals("inactive", root.get("user2").get("status").asText()); // 未映射的字段保持不变
    }

    @Test
    void testReplaceAllByMappingWithVariables() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ObjectNode config = mapper.createObjectNode();
        config.put("env", "production");
        config.put("url", "https://api.example.com");

        root.set("config", config);

        // 准备映射和参数 - 简化的变量测试
        Map<String, String> mapping = new HashMap<>();
        mapping.put("config.url", "api.endpoint");

        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("env", "production");

        // 测试处理器 - 添加环境前缀
        BiFunction<String, String, String> processor = (path, value) ->
                "PROD:" + value;

        // 执行方法
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果
        assertEquals("PROD:https://api.example.com", root.get("config").get("url").asText());
        assertEquals("production", root.get("config").get("env").asText()); // env字段保持不变
    }

    @Test
    void testReplaceAllByMappingWithComplexStructure() {
        // 准备测试数据 - 复杂嵌套结构
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ArrayNode users = mapper.createArrayNode();

        ObjectNode user1 = mapper.createObjectNode();
        user1.put("name", "alice");
        ArrayNode skills1 = mapper.createArrayNode();
        skills1.add("java");
        skills1.add("python");
        user1.set("skills", skills1);

        ObjectNode user2 = mapper.createObjectNode();
        user2.put("name", "bob");
        ArrayNode skills2 = mapper.createArrayNode();
        skills2.add("javascript");
        skills2.add("react");
        user2.set("skills", skills2);

        users.add(user1);
        users.add(user2);
        root.set("users", users);

        // 准备映射和参数 - 处理嵌套数组中的技能
        Map<String, String> mapping = new HashMap<>();
        mapping.put("users.*.skills", "user.skill.$index");

        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器 - 技能标准化
        BiFunction<String, String, String> processor = (path, value) ->
                "SKILL:" + value.toUpperCase();

        // 执行方法
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果
        assertEquals("alice", root.get("users").get(0).get("name").asText()); // name字段保持不变
        assertEquals("SKILL:JAVA", root.get("users").get(0).get("skills").get(0).asText());
        assertEquals("SKILL:PYTHON", root.get("users").get(0).get("skills").get(1).asText());

        assertEquals("bob", root.get("users").get(1).get("name").asText()); // name字段保持不变
        assertEquals("SKILL:JAVASCRIPT", root.get("users").get(1).get("skills").get(0).asText());
        assertEquals("SKILL:REACT", root.get("users").get(1).get("skills").get(1).asText());
    }

    @Test
    void testReplaceAllByMappingWithEmptyMapping() {
        // 准备测试数据
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("name", "test");
        root.put("value", "original");

        // 空映射
        Map<String, String> mapping = new HashMap<>();
        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                "MODIFIED:" + value;

        // 执行方法 - 不应抛出异常
        JsonUtils.replaceAllByMapping(root, mapping, templateParams, processor);

        // 验证结果 - 所有字段应保持不变
        assertEquals("test", root.get("name").asText());
        assertEquals("original", root.get("value").asText());
    }

    @Test
    void testReplaceAllByMappingWithNullNode() {
        // 准备映射和参数
        Map<String, String> mapping = new HashMap<>();
        mapping.put("name", "user.name");

        Map<String, String> templateParams = new HashMap<>();

        // 测试处理器
        BiFunction<String, String, String> processor = (path, value) ->
                "MODIFIED:" + value;

        // 执行方法 - 不应抛出异常
        JsonUtils.replaceAllByMapping(null, mapping, templateParams, processor);
    }

}
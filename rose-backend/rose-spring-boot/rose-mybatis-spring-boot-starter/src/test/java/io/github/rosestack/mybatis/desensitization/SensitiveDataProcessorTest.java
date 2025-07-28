package io.github.rosestack.mybatis.desensitization;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.enums.SensitiveType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感数据处理器测试
 * 测试脱敏处理的核心逻辑
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SensitiveDataProcessorTest {

    @Test
    void testDesensitizeObject_SingleEntity() {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setName("张三");
        entity.setPhone("13800138000");
        entity.setIdCard("123456789012345678");
        entity.setEmail("zhangsan@example.com");

        // 执行脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(entity);

        // 验证脱敏效果
        TestEntity resultEntity = (TestEntity) result;
        assertEquals("张*", resultEntity.getName());
        assertEquals("138****8000", resultEntity.getPhone());
        assertEquals("123456****5678", resultEntity.getIdCard());
        assertEquals("zha***@example.com", resultEntity.getEmail());
    }

    @Test
    void testDesensitizeObject_Collection() {
        // 准备测试数据
        TestEntity entity1 = new TestEntity();
        entity1.setName("张三");
        entity1.setPhone("13800138000");

        TestEntity entity2 = new TestEntity();
        entity2.setName("李四");
        entity2.setPhone("13900139000");

        List<TestEntity> entityList = Arrays.asList(entity1, entity2);

        // 执行脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(entityList);

        // 验证脱敏效果
        @SuppressWarnings("unchecked")
        List<TestEntity> resultList = (List<TestEntity>) result;
        
        assertEquals("张*", resultList.get(0).getName());
        assertEquals("138****8000", resultList.get(0).getPhone());
        assertEquals("李*", resultList.get(1).getName());
        assertEquals("139****9000", resultList.get(1).getPhone());
    }

    @Test
    void testDesensitizeObject_NullObject() {
        // 测试null对象
        Object result = SensitiveDataProcessor.desensitizeObject(null);
        assertNull(result);
    }

    @Test
    void testDesensitizeObject_NullFields() {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setName(null);
        entity.setPhone(null);
        entity.setIdCard("123456789012345678");

        // 执行脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(entity);

        // 验证null字段不会被处理
        TestEntity resultEntity = (TestEntity) result;
        assertNull(resultEntity.getName());
        assertNull(resultEntity.getPhone());
        assertEquals("123456****5678", resultEntity.getIdCard());
    }

    @Test
    void testDesensitizeObject_EmptyCollection() {
        // 测试空集合
        List<TestEntity> emptyList = Arrays.asList();
        Object result = SensitiveDataProcessor.desensitizeObject(emptyList);
        
        assertTrue(result instanceof List);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test
    void testDesensitizeObject_NonSensitiveFields() {
        // 准备测试数据
        TestEntityWithoutSensitive entity = new TestEntityWithoutSensitive();
        entity.setName("张三");
        entity.setPhone("13800138000");

        // 执行脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(entity);

        // 验证非敏感字段不会被脱敏
        TestEntityWithoutSensitive resultEntity = (TestEntityWithoutSensitive) result;
        assertEquals("张三", resultEntity.getName());
        assertEquals("13800138000", resultEntity.getPhone());
    }

    @Test
    void testDesensitizeObject_MixedFields() {
        // 准备测试数据
        TestEntityMixed entity = new TestEntityMixed();
        entity.setName("张三");
        entity.setPhone("13800138000");
        entity.setAddress("北京市朝阳区");

        // 执行脱敏
        Object result = SensitiveDataProcessor.desensitizeObject(entity);

        // 验证混合字段的脱敏效果
        TestEntityMixed resultEntity = (TestEntityMixed) result;
        assertEquals("张*", resultEntity.getName());
        assertEquals("138****8000", resultEntity.getPhone());
        assertEquals("北京市朝阳区", resultEntity.getAddress()); // 非敏感字段不变
    }

    /**
     * 测试实体类
     */
    public static class TestEntity {
        @SensitiveField(SensitiveType.NAME)
        private String name;

        @SensitiveField(SensitiveType.PHONE)
        private String phone;

        @SensitiveField(SensitiveType.ID_CARD)
        private String idCard;

        @SensitiveField(SensitiveType.EMAIL)
        private String email;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * 无敏感字段的测试实体类
     */
    public static class TestEntityWithoutSensitive {
        private String name;
        private String phone;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    /**
     * 混合字段的测试实体类
     */
    public static class TestEntityMixed {
        @SensitiveField(SensitiveType.NAME)
        private String name;

        @SensitiveField(SensitiveType.PHONE)
        private String phone;

        // 非敏感字段
        private String address;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}
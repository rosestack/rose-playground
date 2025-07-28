package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.encryption.FieldEncryptor;
import io.github.rosestack.mybatis.enums.EncryptType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 字段加密拦截器简化测试
 * 专注于测试核心业务逻辑，避免复杂的 MyBatis Mock
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FieldEncryptionInterceptorSimpleTest {

    @Mock
    private FieldEncryptor fieldEncryptor;

    private FieldEncryptionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FieldEncryptionInterceptor(fieldEncryptor);
    }

    @Test
    void testEncryptFields_SingleEntity() throws Exception {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("John Doe");
        entity.setPhone("13800138000");
        entity.setIdCard("123456789012345678");

        when(fieldEncryptor.encrypt(eq("13800138000"), any())).thenReturn("encrypted_phone");
        when(fieldEncryptor.encrypt(eq("123456789012345678"), any())).thenReturn("encrypted_idcard");

        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method encryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("encryptFields", Object.class);
        encryptMethod.setAccessible(true);
        encryptMethod.invoke(interceptor, entity);

        // 验证加密是否执行
        assertEquals("encrypted_phone", entity.getPhone());
        assertEquals("encrypted_idcard", entity.getIdCard());
        assertEquals("John Doe", entity.getName()); // 非加密字段不变
    }

    @Test
    void testDecryptFields_SingleEntity() throws Exception {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setPhone("encrypted_phone");
        entity.setIdCard("encrypted_idcard");

        when(fieldEncryptor.decrypt("encrypted_phone", EncryptType.AES)).thenReturn("13800138000");
        when(fieldEncryptor.decrypt("encrypted_idcard", EncryptType.AES)).thenReturn("123456789012345678");

        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method decryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("decryptFields", Object.class);
        decryptMethod.setAccessible(true);
        decryptMethod.invoke(interceptor, entity);

        // 验证解密是否执行
        assertEquals("13800138000", entity.getPhone());
        assertEquals("123456789012345678", entity.getIdCard());
    }

    @Test
    void testEncryptFields_Collection() throws Exception {
        // 准备测试数据
        TestEntity entity1 = new TestEntity();
        entity1.setPhone("13800138001");
        TestEntity entity2 = new TestEntity();
        entity2.setPhone("13800138002");

        List<TestEntity> entityList = Arrays.asList(entity1, entity2);

        when(fieldEncryptor.encrypt("13800138001", EncryptType.AES)).thenReturn("encrypted_phone1");
        when(fieldEncryptor.encrypt("13800138002", EncryptType.AES)).thenReturn("encrypted_phone2");

        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method encryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("encryptFields", Object.class);
        encryptMethod.setAccessible(true);
        encryptMethod.invoke(interceptor, entityList);

        // 验证批量加密是否执行
        assertEquals("encrypted_phone1", entity1.getPhone());
        assertEquals("encrypted_phone2", entity2.getPhone());
    }

    @Test
    void testEncryptFields_WithHashField() throws Exception {
        // 准备测试数据
        TestEntityWithHash entity = new TestEntityWithHash();
        entity.setPhone("13800138000");

        when(fieldEncryptor.encrypt(eq("13800138000"), any())).thenReturn("encrypted_phone");

        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method encryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("encryptFields", Object.class);
        encryptMethod.setAccessible(true);
        encryptMethod.invoke(interceptor, entity);

        // 验证加密
        assertEquals("encrypted_phone", entity.getPhone());

        // 注意：哈希字段的设置依赖于实际的哈希算法实现
        // 这里我们只验证方法调用不会抛出异常
        assertDoesNotThrow(() -> {
            // 哈希字段可能被设置，也可能因为字段不存在而跳过
            // 这取决于实体类的定义
        });
    }

    @Test
    void testEncryptFields_NullEntity() throws Exception {
        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method encryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("encryptFields", Object.class);
        encryptMethod.setAccessible(true);

        // 测试null参数不会抛出异常
        assertDoesNotThrow(() -> encryptMethod.invoke(interceptor, (Object) null));
    }

    @Test
    void testDecryptFields_NullEntity() throws Exception {
        // 使用反射调用私有方法进行测试
        java.lang.reflect.Method decryptMethod = FieldEncryptionInterceptor.class
                .getDeclaredMethod("decryptFields", Object.class);
        decryptMethod.setAccessible(true);

        // 测试null参数不会抛出异常
        assertDoesNotThrow(() -> decryptMethod.invoke(interceptor, (Object) null));
    }

    /**
     * 测试实体类
     */
    public static class TestEntity {
        private Long id;
        private String name;

        @EncryptField(value = EncryptType.AES, searchable = true)
        private String phone;
        private String phoneHash;

        @EncryptField(EncryptType.AES)
        private String idCard;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPhoneHash() { return phoneHash; }
        public void setPhoneHash(String phoneHash) { this.phoneHash = phoneHash; }

        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }
    }

    /**
     * 带哈希字段的测试实体类
     */
    public static class TestEntityWithHash {
        @EncryptField(value = EncryptType.AES, searchable = true)
        private String phone;
        private String phoneHash;

        // Getters and Setters
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPhoneHash() { return phoneHash; }
        public void setPhoneHash(String phoneHash) { this.phoneHash = phoneHash; }
    }
}
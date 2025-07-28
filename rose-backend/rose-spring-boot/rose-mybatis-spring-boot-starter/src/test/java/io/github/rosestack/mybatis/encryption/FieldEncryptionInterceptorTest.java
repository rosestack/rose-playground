package io.github.rosestack.mybatis.encryption;

import io.github.rosestack.mybatis.annotation.EncryptField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 字段加密拦截器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class FieldEncryptionInterceptorTest {

    @Mock
    private FieldEncryptor fieldEncryptor;

    @Mock
    private Executor executor;

    @Mock
    private ResultSetHandler resultSetHandler;

    @Mock
    private MappedStatement mappedStatement;

    @Mock
    private Statement statement;

    private FieldEncryptionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new FieldEncryptionInterceptor(fieldEncryptor);
    }

    @Test
    void testIntercept_ExecutorUpdate_Insert() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("John Doe");
        entity.setPhone("13800138000");
        entity.setIdCard("123456789012345678");

        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        when(fieldEncryptor.encrypt(eq("13800138000"), any())).thenReturn("encrypted_phone");
        when(fieldEncryptor.encrypt(eq("123456789012345678"), any())).thenReturn("encrypted_idcard");

        Object[] args = {mappedStatement, entity};
        Invocation invocation = new Invocation(executor, executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证加密是否执行
        verify(fieldEncryptor).encrypt("13800138000", EncryptType.AES);
        verify(fieldEncryptor).encrypt("123456789012345678", EncryptType.AES);

        // 验证字段是否被加密
        assertEquals("encrypted_phone", entity.getPhone());
        assertEquals("encrypted_idcard", entity.getIdCard());
        assertNotNull(entity.getPhoneHash()); // 哈希字段应该被设置
    }

    @Test
    void testIntercept_ExecutorUpdate_Update() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Jane Doe");
        entity.setPhone("13900139000");

        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
        when(fieldEncryptor.encrypt(eq("13900139000"), any())).thenReturn("encrypted_phone_updated");

        Object[] args = {mappedStatement, entity};
        Invocation invocation = new Invocation(executor, executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证加密是否执行
        verify(fieldEncryptor).encrypt("13900139000", EncryptType.AES);

        // 验证字段是否被加密
        assertEquals("encrypted_phone_updated", entity.getPhone());
    }

    @Test
    void testIntercept_ExecutorUpdate_Select() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setPhone("13800138000");

        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.SELECT);

        Object[] args = {mappedStatement, entity};
        Invocation invocation = new Invocation(executor, executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证不应该执行加密
        verify(fieldEncryptor, never()).encrypt(any(), any());

        // 验证字段没有被修改
        assertEquals("13800138000", entity.getPhone());
    }

    @Test
    void testIntercept_ResultSetHandler() throws Throwable {
        // 准备测试数据
        TestEntity entity1 = new TestEntity();
        entity1.setPhone("encrypted_phone1");
        entity1.setIdCard("encrypted_idcard1");

        TestEntity entity2 = new TestEntity();
        entity2.setPhone("encrypted_phone2");
        entity2.setIdCard("encrypted_idcard2");

        List<TestEntity> resultList = Arrays.asList(entity1, entity2);

        when(fieldEncryptor.decrypt("encrypted_phone1", EncryptType.AES)).thenReturn("13800138001");
        when(fieldEncryptor.decrypt("encrypted_idcard1", EncryptType.AES)).thenReturn("123456789012345671");
        when(fieldEncryptor.decrypt("encrypted_phone2", EncryptType.AES)).thenReturn("13800138002");
        when(fieldEncryptor.decrypt("encrypted_idcard2", EncryptType.AES)).thenReturn("123456789012345672");

        Object[] args = {statement};
        Invocation invocation = new Invocation(resultSetHandler, 
            resultSetHandler.getClass().getMethod("handleResultSets", Statement.class), args);

        // 模拟原始方法返回结果
        when(invocation.proceed()).thenReturn(resultList);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证解密是否执行
        verify(fieldEncryptor).decrypt("encrypted_phone1", EncryptType.AES);
        verify(fieldEncryptor).decrypt("encrypted_idcard1", EncryptType.AES);
        verify(fieldEncryptor).decrypt("encrypted_phone2", EncryptType.AES);
        verify(fieldEncryptor).decrypt("encrypted_idcard2", EncryptType.AES);

        // 验证字段是否被解密
        assertEquals("13800138001", entity1.getPhone());
        assertEquals("123456789012345671", entity1.getIdCard());
        assertEquals("13800138002", entity2.getPhone());
        assertEquals("123456789012345672", entity2.getIdCard());
    }

    @Test
    void testIntercept_CollectionParameter() throws Throwable {
        // 准备测试数据
        TestEntity entity1 = new TestEntity();
        entity1.setPhone("13800138001");
        TestEntity entity2 = new TestEntity();
        entity2.setPhone("13800138002");

        List<TestEntity> entityList = Arrays.asList(entity1, entity2);

        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);
        when(fieldEncryptor.encrypt("13800138001", EncryptType.AES)).thenReturn("encrypted_phone1");
        when(fieldEncryptor.encrypt("13800138002", EncryptType.AES)).thenReturn("encrypted_phone2");

        Object[] args = {mappedStatement, entityList};
        Invocation invocation = new Invocation(executor, executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证批量加密是否执行
        verify(fieldEncryptor).encrypt("13800138001", EncryptType.AES);
        verify(fieldEncryptor).encrypt("13800138002", EncryptType.AES);

        // 验证字段是否被加密
        assertEquals("encrypted_phone1", entity1.getPhone());
        assertEquals("encrypted_phone2", entity2.getPhone());
    }

    @Test
    void testIntercept_NullParameter() throws Throwable {
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);

        Object[] args = {mappedStatement, null};
        Invocation invocation = new Invocation(executor, executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 执行拦截
        assertDoesNotThrow(() -> interceptor.intercept(invocation));

        // 验证不应该执行加密
        verify(fieldEncryptor, never()).encrypt(any(), any());
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
}

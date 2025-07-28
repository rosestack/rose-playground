package io.github.rosestack.mybatis.audit;

import io.github.rosestack.mybatis.annotation.AuditLog;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 审计拦截器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuditInterceptorTest {

    @Mock
    private Executor executor;

    @Mock
    private MappedStatement mappedStatement;

    private AuditInterceptor interceptor;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getAudit().setEnabled(true);
        
        DefaultAuditStorage auditStorage = new DefaultAuditStorage();
        interceptor = new AuditInterceptor(properties, auditStorage);
    }

    @Test
    void testIntercept_WithAuditAnnotation_Insert() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("Test Entity");

        when(mappedStatement.getId()).thenReturn("io.github.rosestack.mybatis.audit.AuditInterceptorTest$TestMapper.insert");
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);

        Object[] args = {mappedStatement, entity, RowBounds.DEFAULT, null};
        Invocation invocation = new Invocation(executor, 
            executor.getClass().getMethod("query", MappedStatement.class, Object.class, RowBounds.class, org.apache.ibatis.session.ResultHandler.class), 
            args);

        // 模拟原始方法执行
        when(invocation.proceed()).thenReturn(1);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证返回结果
        assertEquals(1, result);
    }

    @Test
    void testIntercept_WithoutAuditAnnotation() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setId(1L);

        when(mappedStatement.getId()).thenReturn("io.github.rosestack.mybatis.audit.AuditInterceptorTest$TestMapperWithoutAnnotation.insert");
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);

        Object[] args = {mappedStatement, entity};
        Invocation invocation = new Invocation(executor, 
            executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 模拟原始方法执行
        when(invocation.proceed()).thenReturn(1);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证返回结果
        assertEquals(1, result);
    }

    @Test
    void testIntercept_SelectOperation() throws Throwable {
        // 准备测试数据
        when(mappedStatement.getId()).thenReturn("io.github.rosestack.mybatis.audit.AuditInterceptorTest$TestMapper.selectById");
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.SELECT);

        Object[] args = {mappedStatement, 1L, RowBounds.DEFAULT, null};
        Invocation invocation = new Invocation(executor, 
            executor.getClass().getMethod("query", MappedStatement.class, Object.class, RowBounds.class, org.apache.ibatis.session.ResultHandler.class), 
            args);

        // 模拟原始方法执行
        when(invocation.proceed()).thenReturn(new TestEntity());

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证返回结果
        assertNotNull(result);
    }

    @Test
    void testIntercept_NullParameter() throws Throwable {
        when(mappedStatement.getId()).thenReturn("io.github.rosestack.mybatis.audit.AuditInterceptorTest$TestMapper.insert");
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);

        Object[] args = {mappedStatement, null};
        Invocation invocation = new Invocation(executor, 
            executor.getClass().getMethod("update", MappedStatement.class, Object.class), args);

        // 模拟原始方法执行
        when(invocation.proceed()).thenReturn(0);

        // 执行拦截
        assertDoesNotThrow(() -> interceptor.intercept(invocation));
    }

    /**
     * 测试实体类
     */
    public static class TestEntity {
        private Long id;
        private String name;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    /**
     * 带审计注解的测试 Mapper
     */
    public interface TestMapper {
        @AuditLog(module = "USER", operation = "INSERT")
        int insert(TestEntity entity);

        @AuditLog(module = "USER", operation = "UPDATE")
        int updateById(TestEntity entity);

        @AuditLog(module = "USER", operation = "DELETE")
        int deleteById(Long id);

        @AuditLog(module = "USER", operation = "SELECT")
        TestEntity selectById(Long id);
    }

    /**
     * 不带审计注解的测试 Mapper
     */
    public interface TestMapperWithoutAnnotation {
        int insert(TestEntity entity);
    }
}
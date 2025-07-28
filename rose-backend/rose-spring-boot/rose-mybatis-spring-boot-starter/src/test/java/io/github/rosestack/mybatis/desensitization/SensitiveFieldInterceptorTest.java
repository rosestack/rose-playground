package io.github.rosestack.mybatis.desensitization;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
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
import static org.mockito.Mockito.when;

/**
 * 敏感字段拦截器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class SensitiveFieldInterceptorTest {

    @Mock
    private ResultSetHandler resultSetHandler;

    @Mock
    private Statement statement;

    private SensitiveFieldInterceptor interceptor;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getDesensitization().setEnabled(true);
        
        interceptor = new SensitiveFieldInterceptor(properties);
    }

    @Test
    void testIntercept_SingleEntity() throws Throwable {
        // 准备测试数据
        TestEntity entity = new TestEntity();
        entity.setName("张三");
        entity.setPhone("13800138000");
        entity.setIdCard("123456789012345678");
        entity.setEmail("zhangsan@example.com");

        Object[] args = {statement};
        Invocation invocation = new Invocation(resultSetHandler, 
            resultSetHandler.getClass().getMethod("handleResultSets", Statement.class), args);

        // 模拟原始方法返回结果
        when(invocation.proceed()).thenReturn(entity);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证脱敏效果
        TestEntity resultEntity = (TestEntity) result;
        assertEquals("张*", resultEntity.getName());
        assertEquals("138****8000", resultEntity.getPhone());
        assertEquals("123456*********678", resultEntity.getIdCard());
        assertEquals("zhan***@example.com", resultEntity.getEmail());
    }

    @Test
    void testIntercept_NullResult() throws Throwable {
        Object[] args = {statement};
        Invocation invocation = new Invocation(resultSetHandler, 
            resultSetHandler.getClass().getMethod("handleResultSets", Statement.class), args);

        // 模拟原始方法返回null
        when(invocation.proceed()).thenReturn(null);

        // 执行拦截
        Object result = interceptor.intercept(invocation);

        // 验证结果为null
        assertNull(result);
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

        // 非敏感字段
        private String address;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getIdCard() { return idCard; }
        public void setIdCard(String idCard) { this.idCard = idCard; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}
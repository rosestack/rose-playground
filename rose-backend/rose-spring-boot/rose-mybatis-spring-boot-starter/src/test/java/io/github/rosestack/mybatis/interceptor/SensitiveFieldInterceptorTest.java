package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.annotation.SensitiveField;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.desensitization.SensitiveType;
import lombok.Data;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 敏感字段脱敏拦截器测试
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

    @Mock
    private Invocation invocation;

    private SensitiveFieldInterceptor interceptor;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getDesensitization().setEnabled(true);
        properties.getDesensitization().setEnvironments("prod");

        interceptor = new SensitiveFieldInterceptor(properties);
    }

    @Test
    void shouldDesensitizeSingleObject() throws Throwable {
        // Given
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");
        user.setPhone("13800138000");
        user.setEmail("zhang@example.com");
        user.setIdCard("110101199001011234");

        when(invocation.proceed()).thenReturn(user);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        assertThat(result).isInstanceOf(TestUser.class);
        TestUser desensitizedUser = (TestUser) result;

        assertThat(desensitizedUser.getName()).isEqualTo("张三"); // 非敏感字段不变
        assertThat(desensitizedUser.getPhone()).isEqualTo("138****8000"); // 手机号脱敏
        assertThat(desensitizedUser.getEmail()).isEqualTo("zha***@example.com"); // 邮箱脱敏
        assertThat(desensitizedUser.getIdCard()).isEqualTo("110101****1234"); // 身份证脱敏
    }

    @Test
    void shouldDesensitizeListOfObjects() throws Throwable {
        // Given
        TestUser user1 = new TestUser();
        user1.setId(1L);
        user1.setName("张三");
        user1.setPhone("13800138000");
        user1.setEmail("zhang@example.com");

        TestUser user2 = new TestUser();
        user2.setId(2L);
        user2.setName("李四");
        user2.setPhone("13900139000");
        user2.setEmail("li@example.com");

        List<TestUser> users = Arrays.asList(user1, user2);
        when(invocation.proceed()).thenReturn(users);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        assertThat(result).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<TestUser> desensitizedUsers = (List<TestUser>) result;

        assertThat(desensitizedUsers).hasSize(2);

        TestUser firstUser = desensitizedUsers.get(0);
        assertThat(firstUser.getPhone()).isEqualTo("138****8000");
        assertThat(firstUser.getEmail()).isEqualTo("zha***@example.com");

        TestUser secondUser = desensitizedUsers.get(1);
        assertThat(secondUser.getPhone()).isEqualTo("139****9000");
        assertThat(secondUser.getEmail()).isEqualTo("l***@example.com");
    }

    @Test
    void shouldNotDesensitizeWhenDisabled() throws Throwable {
        // Given
        properties.getDesensitization().setEnabled(false);

        TestUser user = new TestUser();
        user.setPhone("13800138000");
        user.setEmail("zhang@example.com");

        when(invocation.proceed()).thenReturn(user);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        TestUser resultUser = (TestUser) result;
        assertThat(resultUser.getPhone()).isEqualTo("13800138000"); // 未脱敏
        assertThat(resultUser.getEmail()).isEqualTo("zhang@example.com"); // 未脱敏
    }

    @Test
    void shouldHandleNullResult() throws Throwable {
        // Given
        when(invocation.proceed()).thenReturn(null);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleNullSensitiveFields() throws Throwable {
        // Given
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");
        user.setPhone(null);
        user.setEmail(null);

        when(invocation.proceed()).thenReturn(user);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        TestUser resultUser = (TestUser) result;
        assertThat(resultUser.getPhone()).isNull();
        assertThat(resultUser.getEmail()).isNull();
    }

    @Test
    void shouldSkipDesensitizationForFieldsWithoutAnnotation() throws Throwable {
        // Given
        TestUserWithDisabledField user = new TestUserWithDisabledField();
        user.setId(1L);
        user.setPhone("13800138000");

        when(invocation.proceed()).thenReturn(user);

        // When
        Object result = interceptor.intercept(invocation);

        // Then
        TestUserWithDisabledField resultUser = (TestUserWithDisabledField) result;
        assertThat(resultUser.getPhone()).isEqualTo("13800138000"); // 未脱敏，因为没有敏感字段注解
    }

    /**
     * 测试用户实体
     */
    @Data
    public static class TestUser {
        private Long id;
        private String name;

        @SensitiveField(SensitiveType.PHONE)
        private String phone;

        @SensitiveField(SensitiveType.EMAIL)
        private String email;

        @SensitiveField(SensitiveType.ID_CARD)
        private String idCard;
    }

    /**
     * 无敏感字段注解的测试实体
     */
    @Data
    public static class TestUserWithDisabledField {
        private Long id;

        // 没有敏感字段注解，不会脱敏
        private String phone;
    }
}

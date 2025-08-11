package io.github.rosestack.iam.entity;

import io.github.rosestack.iam.infra.mybatis.entity.UserEntity;
import io.github.rosestack.core.entity.BaseEntity;
import io.github.rosestack.core.entity.BaseTenantEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserEntity继承关系测试
 * <p>
 * 验证UserEntity是否正确继承了BaseTenantEntity，没有重复定义字段
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
class UserEntityInheritanceTest {

    @Test
    void testUserEntityInheritance() {
        // 验证继承关系
        assertTrue(BaseTenantEntity.class.isAssignableFrom(UserEntity.class));
        assertTrue(BaseEntity.class.isAssignableFrom(UserEntity.class));
    }

    @Test
    void testNoDuplicateFields() {
        // 获取UserEntity的所有字段
        List<String> userEntityFields = Arrays.stream(UserEntity.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        // 获取BaseTenantEntity的所有字段
        List<String> baseTenantEntityFields = Arrays.stream(BaseTenantEntity.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        // 获取BaseEntity的所有字段
        List<String> baseEntityFields = Arrays.stream(BaseEntity.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        // 验证没有重复定义继承的字段
        List<String> inheritedFields = new java.util.ArrayList<>();
        inheritedFields.addAll(baseEntityFields);
        inheritedFields.addAll(baseTenantEntityFields);

        for (String inheritedField : inheritedFields) {
            assertFalse(userEntityFields.contains(inheritedField),
                    "UserEntity不应该重复定义继承的字段: " + inheritedField);
        }
    }

    @Test
    void testUserEntityHasBusinessFields() {
        // 验证UserEntity包含业务字段
        List<String> userEntityFields = Arrays.stream(UserEntity.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());

        // 验证包含必要的业务字段
        assertTrue(userEntityFields.contains("id"), "UserEntity应该包含id字段");
        assertTrue(userEntityFields.contains("username"), "UserEntity应该包含username字段");
        assertTrue(userEntityFields.contains("email"), "UserEntity应该包含email字段");
        assertTrue(userEntityFields.contains("phone"), "UserEntity应该包含phone字段");
        assertTrue(userEntityFields.contains("password"), "UserEntity应该包含password字段");
        assertTrue(userEntityFields.contains("status"), "UserEntity应该包含status字段");
    }

    @Test
    void testUserEntityCanBeInstantiated() {
        // 验证可以创建UserEntity实例（通过反射，因为构造函数可能私有）
        assertDoesNotThrow(() -> {
            UserEntity userEntity = UserEntity.class.getDeclaredConstructor().newInstance();
            assertNotNull(userEntity);
        });
    }
} 
package io.github.rosestack.mybatis.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.util.ContextUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Rose 元数据处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RoseMetaObjectHandlerTest {

    @Mock
    private MetaObject metaObject;

    private RoseMetaObjectHandler handler;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getFieldFill().setDefaultUser("system");
        
        handler = new RoseMetaObjectHandler(properties);
    }

    @Test
    void testInsertFill_WithValidUser() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟当前用户
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user123");
            mockedContextUtils.when(ContextUtils::getCurrentTenantId).thenReturn("tenant123");

            // 模拟字段存在
            when(metaObject.hasSetter("createdTime")).thenReturn(true);
            when(metaObject.hasSetter("updatedTime")).thenReturn(true);
            when(metaObject.hasSetter("createdBy")).thenReturn(true);
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);
            when(metaObject.hasSetter("tenantId")).thenReturn(true);
            when(metaObject.hasSetter("deleted")).thenReturn(true);
            when(metaObject.hasSetter("version")).thenReturn(true);

            // 模拟字段值为空
            when(metaObject.getValue("createdTime")).thenReturn(null);
            when(metaObject.getValue("updatedTime")).thenReturn(null);
            when(metaObject.getValue("createdBy")).thenReturn(null);
            when(metaObject.getValue("updatedBy")).thenReturn(null);
            when(metaObject.getValue("tenantId")).thenReturn(null);
            when(metaObject.getValue("deleted")).thenReturn(null);
            when(metaObject.getValue("version")).thenReturn(null);

            // 执行插入填充
            handler.insertFill(metaObject);

            // 验证时间字段被设置
            verify(metaObject).setValue(eq("createdTime"), any(LocalDateTime.class));
            verify(metaObject).setValue(eq("updatedTime"), any(LocalDateTime.class));

            // 验证用户字段被设置
            verify(metaObject).setValue("createdBy", "user123");
            verify(metaObject).setValue("updatedBy", "user123");

            // 验证租户字段被设置
            verify(metaObject).setValue("tenantId", "tenant123");

            // 验证其他字段被设置
            verify(metaObject).setValue("deleted", false);
            verify(metaObject).setValue("version", 1);
        }
    }

    @Test
    void testInsertFill_WithNullUser() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟空用户
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn(null);
            mockedContextUtils.when(ContextUtils::getCurrentTenantId).thenReturn("tenant123");

            // 模拟字段存在且为空
            when(metaObject.hasSetter("createdBy")).thenReturn(true);
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);
            when(metaObject.getValue("createdBy")).thenReturn(null);
            when(metaObject.getValue("updatedBy")).thenReturn(null);

            // 执行插入填充
            handler.insertFill(metaObject);

            // 验证使用默认用户
            verify(metaObject).setValue("createdBy", "system");
            verify(metaObject).setValue("updatedBy", "system");
        }
    }

    @Test
    void testInsertFill_WithExistingValues() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user123");

            // 模拟字段存在但已有值
            when(metaObject.hasSetter("createdBy")).thenReturn(true);
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);
            when(metaObject.getValue("createdBy")).thenReturn("existing_user");
            when(metaObject.getValue("updatedBy")).thenReturn("existing_user");

            // 执行插入填充
            handler.insertFill(metaObject);

            // 验证不会覆盖已有值
            verify(metaObject, never()).setValue(eq("createdBy"), any());
            verify(metaObject, never()).setValue(eq("updatedBy"), any());
        }
    }

    @Test
    void testInsertFill_WithMissingFields() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user123");

            // 模拟字段不存在
            when(metaObject.hasSetter("createdBy")).thenReturn(false);
            when(metaObject.hasSetter("updatedBy")).thenReturn(false);

            // 执行插入填充
            assertDoesNotThrow(() -> handler.insertFill(metaObject));

            // 验证不会设置不存在的字段
            verify(metaObject, never()).setValue(eq("createdBy"), any());
            verify(metaObject, never()).setValue(eq("updatedBy"), any());
        }
    }

    @Test
    void testUpdateFill_WithValidUser() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟当前用户
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user456");

            // 模拟字段存在
            when(metaObject.hasSetter("updatedTime")).thenReturn(true);
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);

            // 模拟字段值为空
            when(metaObject.getValue("updatedTime")).thenReturn(null);
            when(metaObject.getValue("updatedBy")).thenReturn(null);

            // 执行更新填充
            handler.updateFill(metaObject);

            // 验证更新时间被设置
            verify(metaObject).setValue(eq("updatedTime"), any(LocalDateTime.class));

            // 验证更新用户被设置
            verify(metaObject).setValue("updatedBy", "user456");
        }
    }

    @Test
    void testUpdateFill_WithNullUser() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            // 模拟空用户
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn(null);

            // 模拟字段存在且为空
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);
            when(metaObject.getValue("updatedBy")).thenReturn(null);

            // 执行更新填充
            handler.updateFill(metaObject);

            // 验证使用默认用户
            verify(metaObject).setValue("updatedBy", "system");
        }
    }

    @Test
    void testUpdateFill_WithExistingValues() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user456");

            // 模拟字段存在但已有值
            when(metaObject.hasSetter("updatedBy")).thenReturn(true);
            when(metaObject.getValue("updatedBy")).thenReturn("existing_user");

            // 执行更新填充
            handler.updateFill(metaObject);

            // 验证不会覆盖已有值
            verify(metaObject, never()).setValue(eq("updatedBy"), any());
        }
    }

    @Test
    void testUpdateFill_DoesNotUpdateCreatedFields() {
        try (MockedStatic<ContextUtils> mockedContextUtils = mockStatic(ContextUtils.class)) {
            mockedContextUtils.when(ContextUtils::getCurrentUserId).thenReturn("user456");

            // 模拟所有字段存在
            when(metaObject.hasSetter(any())).thenReturn(true);
            when(metaObject.getValue(any())).thenReturn(null);

            // 执行更新填充
            handler.updateFill(metaObject);

            // 验证不会更新创建相关字段
            verify(metaObject, never()).setValue(eq("createdTime"), any());
            verify(metaObject, never()).setValue(eq("createdBy"), any());
            verify(metaObject, never()).setValue(eq("tenantId"), any());
            verify(metaObject, never()).setValue(eq("deleted"), any());
            verify(metaObject, never()).setValue(eq("version"), any());
        }
    }
}

package io.github.rosestack.mybatis.datapermission;

import io.github.rosestack.mybatis.annotation.DataPermission;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MyBatis Plus 数据权限处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RoseDataPermissionHandlerTest {

    private RoseDataPermissionHandler mybatisPlusHandler;
    private RoseMybatisProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        mybatisPlusHandler = new RoseDataPermissionHandler();
    }

    @Test
    void testGetSqlSegment_WithoutAnnotation() {
        // 测试没有注解的情况
        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.NonExistentMapper.selectList";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNull(result, "没有注解时应该返回 null");
    }

    @Test
    void testGetSqlSegment_WithEmptyPermissionValues() {
        // 模拟权限值为空的情况
        when(mybatisPlusHandler.needPermissionControl(any())).thenReturn(true);
        when(mybatisPlusHandler.getPermissionValues(any())).thenReturn(Collections.emptyList());

        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.TestMapper.selectWithPermission";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNull(result, "权限值为空时应该返回 null");
    }

    @Test
    void testGetSqlSegment_WithSinglePermissionValue() {
        // 模拟单个权限值的情况
        when(mybatisPlusHandler.needPermissionControl(any())).thenReturn(true);
        when(mybatisPlusHandler.getPermissionValues(any())).thenReturn(Arrays.asList("user123"));

        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.TestMapper.selectWithPermission";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "单个权限值时应该返回表达式");
        assertTrue(result.toString().contains("user123"), "表达式应该包含权限值");
    }

    @Test
    void testGetSqlSegment_WithMultiplePermissionValues() {
        // 模拟多个权限值的情况
        when(mybatisPlusHandler.needPermissionControl(any())).thenReturn(true);
        when(mybatisPlusHandler.getPermissionValues(any())).thenReturn(Arrays.asList("user123", "user456", "user789"));

        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.TestMapper.selectWithPermission";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "多个权限值时应该返回表达式");
        String resultStr = result.toString();
        assertTrue(resultStr.contains("IN"), "多个权限值应该使用 IN 条件");
        assertTrue(resultStr.contains("user123"), "表达式应该包含第一个权限值");
        assertTrue(resultStr.contains("user456"), "表达式应该包含第二个权限值");
        assertTrue(resultStr.contains("user789"), "表达式应该包含第三个权限值");
    }

    @Test
    void testGetSqlSegment_WithTableAlias() {
        // 测试带表别名的情况
        when(mybatisPlusHandler.needPermissionControl(any())).thenReturn(true);
        when(mybatisPlusHandler.getPermissionValues(any())).thenReturn(Arrays.asList("user123"));

        Table table = new Table("user");
        table.setAlias(new Alias("u", false));
        String mappedStatementId = "com.example.mapper.TestMapper.selectWithPermission";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "带别名的表应该返回表达式");
        assertTrue(result.toString().contains("u."), "表达式应该包含表别名");
    }

    @Test
    void testGetSqlSegment_NoPermissionControlNeeded() {
        // 测试不需要权限控制的情况
        when(mybatisPlusHandler.needPermissionControl(any())).thenReturn(false);

        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.TestMapper.selectWithPermission";

        Expression result = mybatisPlusHandler.getSqlSegment(table, null, mappedStatementId);

        assertNull(result, "不需要权限控制时应该返回 null");
    }

    /**
     * 测试用的 Mapper 接口
     */
    public interface TestMapper {
        @DataPermission(field = "user_id", type = DataPermissionType.USER, scope = DataScope.SELF)
        void selectWithPermission();
    }
}

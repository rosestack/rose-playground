package io.github.rosestack.mybatis.datapermission;

import io.github.rosestack.mybatis.annotation.DataPermission;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Rose 数据权限处理器测试
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class RoseDataPermissionHandlerTest {

    private RoseDataPermissionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RoseDataPermissionHandler();
    }

    @Test
    void testGetSqlSegment_WithoutAnnotation() {
        // 测试没有注解的情况
        Table table = new Table("user");
        String mappedStatementId = "com.example.mapper.NonExistentMapper.selectList";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNull(result, "没有注解时应该返回 null");
    }

    @Test
    void testGetSqlSegment_WithValidAnnotation() {
        // 测试有效注解的情况
        Table table = new Table("user");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapper.selectWithPermission";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "有效注解时应该返回表达式");
        assertTrue(result.toString().contains("user_id"), "表达式应该包含权限字段");
    }

    @Test
    void testGetSqlSegment_WithTableAlias() {
        // 测试带表别名的情况
        Table table = new Table("user");
        table.setAlias(new Alias("u", false));
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapper.selectWithPermission";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "带别名的表应该返回表达式");
        assertTrue(result.toString().contains("u."), "表达式应该包含表别名");
    }

    @Test
    void testGetSqlSegment_WithAllScope() {
        // 测试全部权限范围
        Table table = new Table("user");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapper.selectWithAllScope";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNull(result, "全部权限范围应该返回 null");
    }

    @Test
    void testGetSqlSegment_WithDeptPermission() {
        // 测试部门权限
        Table table = new Table("employee");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapper.selectWithDeptPermission";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "部门权限应该返回表达式");
        assertTrue(result.toString().contains("dept_id"), "表达式应该包含部门字段");
    }

    @Test
    void testGetSqlSegment_WithMultipleValues() {
        // 测试多个权限值的情况
        Table table = new Table("order");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapper.selectWithMultipleValues";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "多个权限值应该返回表达式");
        String resultStr = result.toString();
        assertTrue(resultStr.contains("IN"), "多个权限值应该使用 IN 条件");
    }

    @Test
    void testGetSqlSegment_WithClassLevelAnnotation() {
        // 测试类级别注解
        Table table = new Table("product");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapperWithClassAnnotation.selectProducts";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "类级别注解应该返回表达式");
        assertTrue(result.toString().contains("org_id"), "表达式应该包含组织字段");
    }

    @Test
    void testGetSqlSegment_MethodOverridesClass() {
        // 测试方法级别注解覆盖类级别注解
        Table table = new Table("product");
        String mappedStatementId = "io.github.rosestack.mybatis.datapermission.RoseDataPermissionHandlerTest$TestMapperWithClassAnnotation.selectProductsWithMethodAnnotation";

        Expression result = handler.getSqlSegment(table, null, mappedStatementId);

        assertNotNull(result, "方法级别注解应该覆盖类级别注解");
        assertTrue(result.toString().contains("user_id"), "表达式应该包含用户字段而不是组织字段");
        assertFalse(result.toString().contains("org_id"), "表达式不应该包含组织字段");
    }

    /**
     * 测试用的 Mapper 接口
     */
    public interface TestMapper {
        @DataPermission(field = "user_id", type = DataPermissionType.USER, scope = DataScope.SELF)
        void selectWithPermission();

        @DataPermission(field = "user_id", type = DataPermissionType.USER, scope = DataScope.ALL)
        void selectWithAllScope();

        @DataPermission(field = "dept_id", type = DataPermissionType.DEPT, scope = DataScope.DEPT)
        void selectWithDeptPermission();

        @DataPermission(field = "org_id", type = DataPermissionType.ORG, scope = DataScope.ORG_AND_CHILD)
        void selectWithMultipleValues();
    }

    /**
     * 带类级别注解的测试 Mapper
     */
    @DataPermission(field = "org_id", type = DataPermissionType.ORG, scope = DataScope.ORG)
    public interface TestMapperWithClassAnnotation {
        void selectProducts();

        @DataPermission(field = "user_id", type = DataPermissionType.USER, scope = DataScope.SELF)
        void selectProductsWithMethodAnnotation();
    }
}

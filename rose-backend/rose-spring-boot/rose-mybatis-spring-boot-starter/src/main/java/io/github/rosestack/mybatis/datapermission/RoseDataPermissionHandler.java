package io.github.rosestack.mybatis.datapermission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import io.github.rosestack.mybatis.annotation.DataPermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MyBatis Plus 数据权限处理器适配器
 * <p>
 * 将现有的 Rose 数据权限处理逻辑适配到 MyBatis Plus 的 MultiDataPermissionHandler 接口。
 * 这样可以使用 MyBatis Plus 自带的 DataPermissionInterceptor，获得更好的性能和兼容性。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RoseDataPermissionHandler implements MultiDataPermissionHandler {
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        try {
            // 获取数据权限注解
            DataPermission dataPermission = getDataPermissionAnnotation(mappedStatementId);
            if (dataPermission == null) {
                log.debug("方法 {} 没有数据权限注解，跳过权限控制", mappedStatementId);
                return null;
            }

            // 检查是否需要权限控制
            if (!needPermissionControl(dataPermission)) {
                log.debug("方法 {} 不需要权限控制，跳过", mappedStatementId);
                return null;
            }

            // 获取权限值
            List<String> permissionValues = getPermissionValues(dataPermission);
            if (CollectionUtils.isEmpty(permissionValues)) {
                log.debug("方法 {} 权限值为空，不限制数据访问", mappedStatementId);
                return null;
            }

            // 构建权限过滤条件
            Expression permissionExpression = buildPermissionExpression(table, dataPermission, permissionValues);

            if (permissionExpression != null) {
                log.debug("为表 {} 添加数据权限条件: {}", table.getName(), permissionExpression);
                return permissionExpression;
            }

            return null;
        } catch (Exception e) {
            log.error("处理数据权限时发生错误，mappedStatementId: {}, table: {}",
                    mappedStatementId, table.getName(), e);
            return null;
        }
    }

    boolean needPermissionControl(DataPermission dataPermission) {
        return dataPermission != null && dataPermission.scope() != DataScope.ALL;
    }

    /**
     * 构建权限过滤表达式
     */
    private Expression buildPermissionExpression(Table table, DataPermission dataPermission, List<String> permissionValues) {
        String fieldName = dataPermission.field();

        // 处理表别名
        Column column = new Column(table, fieldName);

        if (permissionValues.size() == 1) {
            // 单个值使用等号
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(new StringValue(permissionValues.get(0)));
            return equalsTo;
        } else {
            // 多个值使用 IN 条件
            InExpression inExpression = new InExpression();
            inExpression.setLeftExpression(column);

            ExpressionList expressionList = new ExpressionList();
            List<Expression> expressions = permissionValues.stream()
                    .map(StringValue::new)
                    .collect(Collectors.toList());
            expressionList.setExpressions(expressions);

            inExpression.setRightExpression(expressionList);
            return inExpression;
        }
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermissionAnnotation(String mappedStatementId) {
        try {
            // 解析 mappedStatementId 获取类名和方法名
            int lastDotIndex = mappedStatementId.lastIndexOf('.');
            if (lastDotIndex == -1) {
                return null;
            }

            String className = mappedStatementId.substring(0, lastDotIndex);
            String methodName = mappedStatementId.substring(lastDotIndex + 1);

            // 获取类
            Class<?> mapperClass  = Class.forName(className);

            // 先检查类级别的注解
            DataPermission classAnnotation = mapperClass.getAnnotation(DataPermission.class);

            // 再检查方法级别的注解
            Method[] methods = mapperClass.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    DataPermission methodAnnotation = method.getAnnotation(DataPermission.class);
                    if (methodAnnotation != null) {
                        return methodAnnotation; // 方法级别注解优先
                    }
                }
            }

            return classAnnotation; // 返回类级别注解
        } catch (ClassNotFoundException e) {
            log.warn("无法找到 Mapper 类: {}", mappedStatementId, e);
            return null;
        } catch (Exception e) {
            log.error("获取数据权限注解时发生错误: {}", mappedStatementId, e);
            return null;
        }
    }

    public List<String> getPermissionValues(DataPermission dataPermission) {
        // 这里应该从当前用户上下文中获取权限值
        // 实际项目中需要集成具体的权限系统

        switch (dataPermission.type()) {
            case USER:
                return getUserPermissionValues(dataPermission);
            case DEPT:
                return getDeptPermissionValues(dataPermission);
            case ORG:
                return getOrgPermissionValues(dataPermission);
            case ROLE:
                return getRolePermissionValues(dataPermission);
            case CUSTOM:
                return getCustomPermissionValues(dataPermission);
            default:
                log.warn("不支持的数据权限类型: {}", dataPermission.type());
                return Collections.emptyList();
        }
    }

    /**
     * 获取用户级权限值
     */
    private List<String> getUserPermissionValues(DataPermission dataPermission) {
        // 从用户上下文获取当前用户ID
        String currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            switch (dataPermission.scope()) {
                case SELF:
                    return Arrays.asList(currentUserId);
                case ALL:
                    return Collections.emptyList(); // 空列表表示不限制
                default:
                    log.warn("用户级权限不支持的数据范围: {}", dataPermission.scope());
                    return Arrays.asList(currentUserId);
            }
        }

        log.warn("未找到当前用户信息，使用默认权限控制");
        return Arrays.asList("UNKNOWN_USER");
    }

    /**
     * 获取部门级权限值
     */
    private List<String> getDeptPermissionValues(DataPermission dataPermission) {
        String currentDeptId = getCurrentDeptId();
        if (currentDeptId != null) {
            switch (dataPermission.scope()) {
                case DEPT:
                    return List.of(currentDeptId);
                case DEPT_AND_CHILD:
                    return getDeptAndChildIds(currentDeptId);
                case ALL:
                    return Collections.emptyList();
                default:
                    return List.of(currentDeptId);
            }
        }

        log.warn("未找到当前部门信息，使用默认权限控制");
        return Arrays.asList("UNKNOWN_DEPT");
    }

    /**
     * 获取组织级权限值
     */
    private List<String> getOrgPermissionValues(DataPermission dataPermission) {
        String currentOrgId = getCurrentOrgId();
        if (currentOrgId != null) {
            switch (dataPermission.scope()) {
                case ORG:
                    return List.of(currentOrgId);
                case ORG_AND_CHILD:
                    return getOrgAndChildIds(currentOrgId);
                case ALL:
                    return Collections.emptyList();
                default:
                    return List.of(currentOrgId);
            }
        }

        log.warn("未找到当前组织信息，使用默认权限控制");
        return List.of("UNKNOWN_ORG");
    }

    /**
     * 获取角色级权限值
     */
    private List<String> getRolePermissionValues(DataPermission dataPermission) {
        List<String> currentRoleIds = getCurrentRoleIds();
        if (!currentRoleIds.isEmpty()) {
            return currentRoleIds;
        }

        log.warn("未找到当前角色信息，使用默认权限控制");
        return List.of("UNKNOWN_ROLE");
    }

    /**
     * 获取自定义权限值
     */
    private List<String> getCustomPermissionValues(DataPermission dataPermission) {
        // 自定义权限逻辑，可以通过扩展点实现
        log.info("使用自定义数据权限逻辑，字段: {}", dataPermission.field());
        return Collections.emptyList();
    }

    /**
     * 获取当前用户ID
     * 实际项目中应该从 SecurityContext 或其他用户上下文中获取
     */
    private String getCurrentUserId() {
        // TODO: 集成实际的用户上下文
        return "DEFAULT_USER";
    }

    /**
     * 获取当前部门ID
     */
    private String getCurrentDeptId() {
        // TODO: 集成实际的部门上下文
        return "DEFAULT_DEPT";
    }

    /**
     * 获取当前组织ID
     */
    private String getCurrentOrgId() {
        // TODO: 集成实际的组织上下文
        return "DEFAULT_ORG";
    }

    /**
     * 获取当前用户的角色ID列表
     */
    private List<String> getCurrentRoleIds() {
        // TODO: 集成实际的角色上下文
        return Arrays.asList("DEFAULT_ROLE");
    }

    /**
     * 获取部门及其子部门ID列表
     */
    private List<String> getDeptAndChildIds(String deptId) {
        // TODO: 集成实际的部门层级查询
        return Arrays.asList(deptId, deptId + "_CHILD1", deptId + "_CHILD2");
    }

    /**
     * 获取组织及其子组织ID列表
     */
    private List<String> getOrgAndChildIds(String orgId) {
        // TODO: 集成实际的组织层级查询
        return Arrays.asList(orgId, orgId + "_CHILD1", orgId + "_CHILD2");
    }
}

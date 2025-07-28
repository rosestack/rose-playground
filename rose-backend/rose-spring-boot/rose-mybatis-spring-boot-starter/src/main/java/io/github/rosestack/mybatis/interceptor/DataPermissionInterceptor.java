package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.annotation.DataPermission;
import io.github.rosestack.mybatis.datapermission.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 * <p>
 * 拦截 MyBatis 的查询操作，根据 @DataPermission 注解自动添加数据权限条件。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataPermissionInterceptor implements Interceptor {

    private final DataPermissionHandler dataPermissionHandler;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[0];
        Object parameter = args[1];

        // 只处理查询操作
        if (mappedStatement.getSqlCommandType() != SqlCommandType.SELECT) {
            return invocation.proceed();
        }

        // 检查是否有数据权限注解
        DataPermission dataPermission = getDataPermissionAnnotation(mappedStatement);
        if (dataPermission == null) {
            return invocation.proceed();
        }

        // 检查是否需要权限控制
        if (!dataPermissionHandler.needPermissionControl(dataPermission)) {
            return invocation.proceed();
        }

        // 获取权限值
        List<String> permissionValues = dataPermissionHandler.getPermissionValues(dataPermission);
        if (permissionValues.isEmpty()) {
            // 空列表表示不限制，直接执行
            return invocation.proceed();
        }

        // 修改SQL添加权限条件
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String originalSql = boundSql.getSql();
        String modifiedSql = addDataPermissionCondition(originalSql, dataPermission, permissionValues);

        if (!originalSql.equals(modifiedSql)) {
            log.debug("原始SQL: {}", originalSql);
            log.debug("权限SQL: {}", modifiedSql);
            
            // 创建新的 MappedStatement
            MappedStatement newMappedStatement = copyMappedStatement(mappedStatement, modifiedSql);
            args[0] = newMappedStatement;
        }

        return invocation.proceed();
    }



    /**
     * 添加数据权限条件到SQL
     */
    private String addDataPermissionCondition(String originalSql, DataPermission dataPermission, List<String> permissionValues) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select) {
                Select select = (Select) statement;
                PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

                // 解析权限字段，支持表别名
                String permissionField = resolvePermissionField(plainSelect, dataPermission);
                Expression permissionCondition = createPermissionCondition(permissionField, permissionValues);

                Expression whereExpression = plainSelect.getWhere();
                if (whereExpression != null) {
                    plainSelect.setWhere(new AndExpression(whereExpression, permissionCondition));
                } else {
                    plainSelect.setWhere(permissionCondition);
                }

                return select.toString();
            }
        } catch (JSQLParserException e) {
            log.error("解析SQL失败，跳过数据权限控制: {}", e.getMessage());
        }

        return originalSql;
    }

    /**
     * 解析权限字段，支持多表 JOIN
     */
    private String resolvePermissionField(PlainSelect plainSelect, DataPermission dataPermission) {
        String field = dataPermission.field();
        String tableAlias = dataPermission.tableAlias();

        // 如果字段已经包含表前缀，直接返回
        if (field.contains(".")) {
            return field;
        }

        // 如果指定了表别名，使用指定的别名
        if (!tableAlias.isEmpty()) {
            return tableAlias + "." + field;
        }

        // 尝试自动识别主表别名
        String mainTableAlias = detectMainTableAlias(plainSelect);
        if (mainTableAlias != null && !mainTableAlias.isEmpty()) {
            return mainTableAlias + "." + field;
        }

        // 如果无法识别表别名，直接返回字段名
        log.warn("无法识别表别名，使用字段名: {}", field);
        return field;
    }

    /**
     * 检测主表别名
     */
    private String detectMainTableAlias(PlainSelect plainSelect) {
        try {
            if (plainSelect.getFromItem() instanceof Table) {
                Table mainTable = (Table) plainSelect.getFromItem();
                if (mainTable.getAlias() != null) {
                    return mainTable.getAlias().getName();
                }

                // 如果主表没有别名，但有 JOIN，尝试使用主表名的首字母
                if (plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
                    String tableName = mainTable.getName();
                    return tableName.substring(0, 1).toLowerCase();
                }
            }
        } catch (Exception e) {
            log.debug("检测主表别名失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 创建权限条件表达式
     */
    private Expression createPermissionCondition(String permissionField, List<String> permissionValues) {
        Column column = new Column(permissionField);
        
        if (permissionValues.size() == 1) {
            // 单个值使用等号
            EqualsTo equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(column);
            equalsTo.setRightExpression(new StringValue(permissionValues.get(0)));
            return equalsTo;
        } else {
            // 多个值使用IN
            InExpression inExpression = new InExpression();
            inExpression.setLeftExpression(column);
            
            List<Expression> expressions = permissionValues.stream()
                .map(StringValue::new)
                .collect(Collectors.toList());
            
            ExpressionList expressionList = new ExpressionList(expressions);
            inExpression.setRightExpression(expressionList);
            return inExpression;
        }
    }

    /**
     * 复制 MappedStatement 并修改SQL
     */
    private MappedStatement copyMappedStatement(MappedStatement original, String newSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
            original.getConfiguration(),
            original.getId(),
            parameter -> {
                BoundSql originalBoundSql = original.getBoundSql(parameter);
                return new BoundSql(
                    original.getConfiguration(),
                    newSql,
                    originalBoundSql.getParameterMappings(),
                    parameter
                );
            },
            original.getSqlCommandType()
        );
        
        builder.resource(original.getResource());
        builder.fetchSize(original.getFetchSize());
        builder.timeout(original.getTimeout());
        builder.statementType(original.getStatementType());
        builder.keyGenerator(original.getKeyGenerator());
        builder.keyProperty(original.getKeyProperties() != null ? String.join(",", original.getKeyProperties()) : null);
        builder.keyColumn(original.getKeyColumns() != null ? String.join(",", original.getKeyColumns()) : null);
        builder.databaseId(original.getDatabaseId());
        builder.lang(original.getLang());
        builder.resultOrdered(original.isResultOrdered());
        builder.resultSets(original.getResultSets() != null ? String.join(",", original.getResultSets()) : null);
        builder.resultMaps(original.getResultMaps());
        builder.cache(original.getCache());
        builder.flushCacheRequired(original.isFlushCacheRequired());
        builder.useCache(original.isUseCache());
        
        return builder.build();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以从配置中读取属性
    }

    /**
     * 获取数据权限注解
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement mappedStatement) {
        try {
            String className = mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf('.'));
            String methodName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf('.') + 1);

            Class<?> mapperClass = Class.forName(className);

            // 先检查方法级注解
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }

            // 再检查类级注解
            return mapperClass.getAnnotation(DataPermission.class);
        } catch (Exception e) {
            log.debug("获取数据权限注解失败: {}", e.getMessage());
            return null;
        }
    }
}

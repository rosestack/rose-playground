package io.github.rosestack.billing.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import io.github.rosestack.billing.service.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * MyBatis Plus 多租户处理器
 *
 * @author rose
 */
@Slf4j
public class BillingTenantLineHandler implements TenantLineHandler {

    /**
     * 租户字段名
     */
    private static final String TENANT_ID_COLUMN = "tenant_id";

    @Override
    public Expression getTenantId() {
        String tenantId = TenantContextHolder.getCurrentTenantId();
        if (tenantId != null) {
            return new StringValue(tenantId);
        }
        // 如果没有租户上下文，返回一个不存在的租户ID，确保数据隔离
        return new StringValue("__NO_TENANT__");
    }

    @Override
    public String getTenantIdColumn() {
        return TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 忽略不需要多租户隔离的表
        return "subscription_plan".equals(tableName) || 
               "payment_method".equals(tableName) ||
               tableName.startsWith("sys_") ||
               tableName.startsWith("config_");
    }
}

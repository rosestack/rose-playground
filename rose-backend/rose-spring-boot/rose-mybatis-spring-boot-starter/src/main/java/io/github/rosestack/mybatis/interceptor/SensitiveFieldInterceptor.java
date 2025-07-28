package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.desensitization.SensitiveDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;

/**
 * 敏感字段脱敏拦截器
 * <p>
 * 在查询结果返回时自动对标记了 @SensitiveField 的字段进行脱敏处理。
 * 使用简单直接的脱敏方式，无复杂的策略和规则。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class SensitiveFieldInterceptor implements Interceptor {

    private final RoseMybatisProperties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 执行原始查询
        Object result = invocation.proceed();

        // 检查是否启用脱敏
        if (!properties.getDesensitization().isEnabled()) {
            return result;
        }

        // 直接使用 SensitiveDataProcessor 进行脱敏处理
        try {
            return SensitiveDataProcessor.desensitizeObject(result);
        } catch (Exception e) {
            log.error("脱敏处理失败: {}", e.getMessage(), e);
            return result; // 失败时返回原始结果
        }
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以从配置中读取属性
    }
}

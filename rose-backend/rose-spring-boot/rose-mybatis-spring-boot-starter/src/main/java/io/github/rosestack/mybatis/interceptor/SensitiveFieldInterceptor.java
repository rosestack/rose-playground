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

        // 对结果进行脱敏处理
        return SensitiveDataProcessor.desensitizeObject(result);
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以从配置中读取属性
    }
}

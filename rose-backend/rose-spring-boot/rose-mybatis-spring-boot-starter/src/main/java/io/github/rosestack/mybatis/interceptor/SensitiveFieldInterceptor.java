package io.github.rosestack.mybatis.interceptor;

import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.desensitization.SensitiveDataProcessor;
import io.github.rosestack.mybatis.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

        // 检查环境配置
        if (!isDesensitizationEnvironment()) {
            return result;
        }

        // 对结果进行脱敏处理
        return SensitiveDataProcessor.desensitizeObject(result);
    }

    /**
     * 检查是否在脱敏环境中
     */
    private boolean isDesensitizationEnvironment() {
        String environments = properties.getDesensitization().getEnvironments();
        if (environments == null || environments.trim().isEmpty()) {
            return true; // 默认启用
        }

        // 使用工具类获取当前环境
        String currentEnv = ContextUtils.getCurrentEnvironment();
        Set<String> enabledEnvs = new HashSet<>(Arrays.asList(environments.split(",")));

        return enabledEnvs.contains(currentEnv.trim());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(java.util.Properties properties) {
        // 可以从配置中读取属性
    }
}

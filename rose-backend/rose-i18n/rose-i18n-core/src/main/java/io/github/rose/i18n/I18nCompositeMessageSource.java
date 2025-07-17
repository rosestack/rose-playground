package io.github.rose.i18n;

import java.util.Locale;
import java.util.Set;

/**
 * 组合消息源接口
 * 
 * <p>将所有I18n功能接口组合在一起，提供完整的国际化消息源功能。
 * 这是一个便利接口，实现类可以选择实现这个接口来获得所有功能，
 * 或者根据需要只实现特定的功能接口。</p>
 * 
 * <p>包含的功能：</p>
 * <ul>
 *   <li>基本消息获取 - {@link I18nMessageSource}</li>
 *   <li>批量消息操作 - {@link I18nBatchMessageSource}</li>
 *   <li>消息源元数据 - {@link I18nMessageSourceMetadata}</li>
 *   <li>生命周期管理 - {@link I18nMessageSourceLifecycle}</li>
 *   <li>查询功能 - {@link I18nMessageSourceQuery}</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * I18nCompositeMessageSource messageSource = new MyMessageSourceImpl();
 * 
 * // 基本消息获取
 * String message = messageSource.getMessage("welcome", null, Locale.ENGLISH);
 * 
 * // 批量操作
 * Map&lt;String, String&gt; messages = messageSource.getMessages(
 *     Set.of("welcome", "goodbye"), Locale.ENGLISH);
 * 
 * // 元数据查询
 * Set&lt;Locale&gt; supportedLocales = messageSource.getSupportedLocales();
 * 
 * // 生命周期管理
 * messageSource.init();
 * messageSource.refresh();
 * messageSource.destroy();
 * 
 * // 查询功能
 * boolean exists = messageSource.containsMessage("welcome", Locale.ENGLISH);
 * </pre>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nCompositeMessageSource extends
        I18nMessageSource,
        I18nBatchMessageSource,
        I18nMessageSourceMetadata,
        I18nMessageSourceLifecycle,
        I18nMessageSourceQuery {

    /**
     * 解决接口冲突：明确实现containsMessages方法
     *
     * @param codes 消息键集合
     * @param locale 语言环境
     * @return 消息键到存在状态的映射
     */
    @Override
    default java.util.Map<String, Boolean> containsMessages(Set<String> codes, Locale locale) {
        if (codes == null || codes.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        java.util.Map<String, Boolean> result = new java.util.HashMap<>();
        for (String code : codes) {
            result.put(code, containsMessage(code, locale));
        }
        return result;
    }

    /**
     * 获取消息源的优先级
     * 
     * <p>用于多个消息源的排序，数值越小优先级越高。
     * 这个方法从原来的I18nMessageSource接口移到这里，
     * 因为它更适合作为组合接口的一部分。</p>
     * 
     * @return 优先级数值，默认为0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 检查消息源是否健康
     * 
     * <p>健康检查可以包括：</p>
     * <ul>
     *   <li>是否已正确初始化</li>
     *   <li>底层数据源是否可访问</li>
     *   <li>是否有足够的消息数据</li>
     *   <li>最近是否有错误发生</li>
     * </ul>
     * 
     * @return 如果消息源健康返回true，否则返回false
     */
    default boolean isHealthy() {
        try {
            return isInitialized() && 
                   !getSupportedLocales().isEmpty() &&
                   getDefaultLocale() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取健康检查详细信息
     * 
     * @return 健康检查结果
     */
    default HealthCheckResult getHealthCheckResult() {
        HealthCheckResult.Builder builder = HealthCheckResult.builder();
        
        try {
            builder.healthy(isHealthy());
            
            if (isInitialized()) {
                builder.addCheck("initialization", true, "Message source is initialized");
            } else {
                builder.addCheck("initialization", false, "Message source is not initialized");
            }
            
            Set<Locale> supportedLocales = getSupportedLocales();
            if (!supportedLocales.isEmpty()) {
                builder.addCheck("locales", true, "Supported locales: " + supportedLocales.size());
            } else {
                builder.addCheck("locales", false, "No supported locales found");
            }
            
            if (getDefaultLocale() != null) {
                builder.addCheck("defaultLocale", true, "Default locale: " + getDefaultLocale());
            } else {
                builder.addCheck("defaultLocale", false, "No default locale configured");
            }
            
        } catch (Exception e) {
            builder.healthy(false)
                   .addCheck("exception", false, "Error during health check: " + e.getMessage());
        }
        
        return builder.build();
    }

    /**
     * 健康检查结果
     */
    class HealthCheckResult {
        private boolean healthy;
        private java.util.Map<String, CheckResult> checks = new java.util.HashMap<>();
        private long checkTime = System.currentTimeMillis();

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public java.util.Map<String, CheckResult> getChecks() {
            return checks;
        }

        public void setChecks(java.util.Map<String, CheckResult> checks) {
            this.checks = checks;
        }

        public long getCheckTime() {
            return checkTime;
        }

        public void setCheckTime(long checkTime) {
            this.checkTime = checkTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final HealthCheckResult result = new HealthCheckResult();

            public Builder healthy(boolean healthy) {
                result.setHealthy(healthy);
                return this;
            }

            public Builder addCheck(String name, boolean passed, String message) {
                result.getChecks().put(name, new CheckResult(passed, message));
                return this;
            }

            public HealthCheckResult build() {
                return result;
            }
        }

        public static class CheckResult {
            private boolean passed;
            private String message;

            public CheckResult(boolean passed, String message) {
                this.passed = passed;
                this.message = message;
            }

            public boolean isPassed() {
                return passed;
            }

            public String getMessage() {
                return message;
            }
        }
    }
}

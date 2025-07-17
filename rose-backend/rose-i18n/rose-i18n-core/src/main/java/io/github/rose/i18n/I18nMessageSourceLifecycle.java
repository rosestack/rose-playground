package io.github.rose.i18n;

/**
 * 消息源生命周期接口
 * 
 * <p>定义消息源的生命周期管理方法，包括初始化、销毁、刷新等操作。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageSourceLifecycle {

    /**
     * 初始化消息源
     * 
     * <p>在消息源被使用前调用，用于加载配置、建立连接等初始化操作。
     * 实现类应该确保此方法的幂等性，即多次调用不会产生副作用。</p>
     * 
     * @throws I18nInitializationException 初始化失败时抛出
     */
    void init() throws I18nInitializationException;

    /**
     * 销毁消息源
     * 
     * <p>在应用关闭时调用，用于清理资源、关闭连接等清理操作。
     * 实现类应该确保此方法的幂等性，即多次调用不会产生副作用。</p>
     */
    void destroy();

    /**
     * 刷新消息缓存
     * 
     * <p>清除缓存并重新加载消息，用于热重载场景。
     * 如果实现类不支持刷新，可以提供空实现。</p>
     * 
     * @throws I18nRefreshException 刷新失败时抛出
     */
    default void refresh() throws I18nRefreshException {
        // 默认实现为空，由具体实现类决定是否支持刷新
    }

    /**
     * 检查消息源是否已初始化
     * 
     * @return 如果已初始化返回true，否则返回false
     */
    default boolean isInitialized() {
        return true; // 默认认为已初始化
    }

    /**
     * 检查消息源是否支持刷新
     * 
     * @return 如果支持刷新返回true，否则返回false
     */
    default boolean supportsRefresh() {
        return false; // 默认不支持刷新
    }

    /**
     * 获取消息源状态
     * 
     * @return 消息源状态
     */
    default I18nMessageSourceStatus getStatus() {
        return I18nMessageSourceStatus.builder()
                .initialized(isInitialized())
                .supportsRefresh(supportsRefresh())
                .healthy(true)
                .build();
    }

    /**
     * 消息源状态信息
     */
    class I18nMessageSourceStatus {
        private boolean initialized;
        private boolean supportsRefresh;
        private boolean healthy;
        private String errorMessage;
        private long lastRefreshTime;

        public boolean isInitialized() {
            return initialized;
        }

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }

        public boolean isSupportsRefresh() {
            return supportsRefresh;
        }

        public void setSupportsRefresh(boolean supportsRefresh) {
            this.supportsRefresh = supportsRefresh;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getLastRefreshTime() {
            return lastRefreshTime;
        }

        public void setLastRefreshTime(long lastRefreshTime) {
            this.lastRefreshTime = lastRefreshTime;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final I18nMessageSourceStatus status = new I18nMessageSourceStatus();

            public Builder initialized(boolean initialized) {
                status.setInitialized(initialized);
                return this;
            }

            public Builder supportsRefresh(boolean supportsRefresh) {
                status.setSupportsRefresh(supportsRefresh);
                return this;
            }

            public Builder healthy(boolean healthy) {
                status.setHealthy(healthy);
                return this;
            }

            public Builder errorMessage(String errorMessage) {
                status.setErrorMessage(errorMessage);
                return this;
            }

            public Builder lastRefreshTime(long lastRefreshTime) {
                status.setLastRefreshTime(lastRefreshTime);
                return this;
            }

            public I18nMessageSourceStatus build() {
                return status;
            }
        }
    }
}

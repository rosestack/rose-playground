package io.github.rose.i18n.spi;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

/**
 * 国际化提供者状态信息
 * 
 * <p>用于描述I18nMessageProvider的当前状态和统计信息。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public class I18nProviderStatus {

    private String name;
    private int priority;
    private boolean supportsHotReload;
    private boolean supportsWrite;
    private Set<Locale> supportedLocales;
    private LocalDateTime lastLoadTime;
    private LocalDateTime lastUpdateTime;
    private long messageCount;
    private boolean healthy = true;
    private String errorMessage;

    /**
     * 获取提供者名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置提供者名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 是否支持热重载
     */
    public boolean isSupportsHotReload() {
        return supportsHotReload;
    }

    /**
     * 设置是否支持热重载
     */
    public void setSupportsHotReload(boolean supportsHotReload) {
        this.supportsHotReload = supportsHotReload;
    }

    /**
     * 是否支持写入
     */
    public boolean isSupportsWrite() {
        return supportsWrite;
    }

    /**
     * 设置是否支持写入
     */
    public void setSupportsWrite(boolean supportsWrite) {
        this.supportsWrite = supportsWrite;
    }

    /**
     * 获取支持的语言环境
     */
    public Set<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    /**
     * 设置支持的语言环境
     */
    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    /**
     * 获取最后加载时间
     */
    public LocalDateTime getLastLoadTime() {
        return lastLoadTime;
    }

    /**
     * 设置最后加载时间
     */
    public void setLastLoadTime(LocalDateTime lastLoadTime) {
        this.lastLoadTime = lastLoadTime;
    }

    /**
     * 获取最后更新时间
     */
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * 设置最后更新时间
     */
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * 获取消息数量
     */
    public long getMessageCount() {
        return messageCount;
    }

    /**
     * 设置消息数量
     */
    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * 是否健康
     */
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * 设置是否健康
     */
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    /**
     * 获取错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 设置错误消息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final I18nProviderStatus status = new I18nProviderStatus();

        public Builder name(String name) {
            status.setName(name);
            return this;
        }

        public Builder priority(int priority) {
            status.setPriority(priority);
            return this;
        }

        public Builder supportsHotReload(boolean supportsHotReload) {
            status.setSupportsHotReload(supportsHotReload);
            return this;
        }

        public Builder supportsWrite(boolean supportsWrite) {
            status.setSupportsWrite(supportsWrite);
            return this;
        }

        public Builder supportedLocales(Set<Locale> supportedLocales) {
            status.setSupportedLocales(supportedLocales);
            return this;
        }

        public Builder lastLoadTime(LocalDateTime lastLoadTime) {
            status.setLastLoadTime(lastLoadTime);
            return this;
        }

        public Builder lastUpdateTime(LocalDateTime lastUpdateTime) {
            status.setLastUpdateTime(lastUpdateTime);
            return this;
        }

        public Builder messageCount(long messageCount) {
            status.setMessageCount(messageCount);
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

        public I18nProviderStatus build() {
            return status;
        }
    }
}

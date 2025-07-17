package io.github.rose.i18n;

import java.util.Locale;
import java.util.Set;

/**
 * 消息源查询接口
 * 
 * <p>提供消息存在性检查和其他查询功能。</p>
 * 
 * @author Rose Framework Team
 * @since 1.0.0
 */
public interface I18nMessageSourceQuery {

    /**
     * 检查消息键是否存在
     * 
     * @param code 消息键
     * @param locale 语言环境
     * @return 如果消息存在返回true，否则返回false
     */
    boolean containsMessage(String code, Locale locale);

    /**
     * 批量检查消息键是否存在
     * 
     * @param codes 消息键集合
     * @param locale 语言环境
     * @return 消息键到存在状态的映射
     */
    default java.util.Map<String, Boolean> containsMessages(Set<String> codes, Locale locale) {
        return codes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        code -> code,
                        code -> containsMessage(code, locale)
                ));
    }

    /**
     * 获取指定语言环境下的所有消息键
     * 
     * @param locale 语言环境
     * @return 消息键集合
     */
    default Set<String> getMessageCodes(Locale locale) {
        // 默认实现返回空集合，具体实现类可以重写
        return java.util.Collections.emptySet();
    }

    /**
     * 搜索消息键
     * 
     * @param pattern 搜索模式（支持通配符）
     * @param locale 语言环境
     * @return 匹配的消息键集合
     */
    default Set<String> searchMessageCodes(String pattern, Locale locale) {
        Set<String> allCodes = getMessageCodes(locale);
        if (pattern == null || pattern.isEmpty()) {
            return allCodes;
        }

        // 简单的通配符匹配实现
        String regex = pattern.replace("*", ".*").replace("?", ".");
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);
        
        return allCodes.stream()
                .filter(code -> compiledPattern.matcher(code).matches())
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 统计消息数量
     * 
     * @param locale 语言环境
     * @return 消息数量
     */
    default long getMessageCount(Locale locale) {
        return getMessageCodes(locale).size();
    }

    /**
     * 获取消息统计信息
     * 
     * @return 统计信息
     */
    default I18nMessageStatistics getStatistics() {
        I18nMessageStatistics.Builder builder = I18nMessageStatistics.builder();
        
        if (this instanceof I18nMessageSourceMetadata) {
            I18nMessageSourceMetadata metadata = (I18nMessageSourceMetadata) this;
            Set<Locale> supportedLocales = metadata.getSupportedLocales();
            
            long totalMessages = 0;
            for (Locale locale : supportedLocales) {
                long count = getMessageCount(locale);
                totalMessages += count;
                builder.addLocaleCount(locale, count);
            }
            
            builder.totalMessages(totalMessages)
                   .supportedLocales(supportedLocales.size());
        }
        
        return builder.build();
    }

    /**
     * 消息统计信息
     */
    class I18nMessageStatistics {
        private long totalMessages;
        private int supportedLocales;
        private java.util.Map<Locale, Long> messageCountByLocale = new java.util.HashMap<>();

        public long getTotalMessages() {
            return totalMessages;
        }

        public void setTotalMessages(long totalMessages) {
            this.totalMessages = totalMessages;
        }

        public int getSupportedLocales() {
            return supportedLocales;
        }

        public void setSupportedLocales(int supportedLocales) {
            this.supportedLocales = supportedLocales;
        }

        public java.util.Map<Locale, Long> getMessageCountByLocale() {
            return messageCountByLocale;
        }

        public void setMessageCountByLocale(java.util.Map<Locale, Long> messageCountByLocale) {
            this.messageCountByLocale = messageCountByLocale;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final I18nMessageStatistics statistics = new I18nMessageStatistics();

            public Builder totalMessages(long totalMessages) {
                statistics.setTotalMessages(totalMessages);
                return this;
            }

            public Builder supportedLocales(int supportedLocales) {
                statistics.setSupportedLocales(supportedLocales);
                return this;
            }

            public Builder addLocaleCount(Locale locale, long count) {
                statistics.getMessageCountByLocale().put(locale, count);
                return this;
            }

            public I18nMessageStatistics build() {
                return statistics;
            }
        }
    }
}

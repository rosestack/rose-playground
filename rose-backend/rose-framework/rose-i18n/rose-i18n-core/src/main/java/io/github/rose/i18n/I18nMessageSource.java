package io.github.rose.i18n;

import io.github.rose.i18n.cache.MessageSourceStats;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface I18nMessageSource extends Lifecycle {
    String COMMON_SOURCE = "common";

    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    @Nullable
    Map<String, String> getMessages(Locale locale);

    default String getMessage(String code, Object... args) {
        return getMessage(code, getLocale(), args);
    }

    /**
     * 批量获取消息
     */
    default Map<String, String> getMessages(Set<String> codes, Locale locale) {
        Map<String, String> result = new HashMap<>();
        for (String code : codes) {
            String message = getMessage(code, locale);
            if (message != null) {
                result.put(code, message);
            }
        }
        return result;
    }

    /**
     * 异步获取消息
     */
    default CompletableFuture<String> getMessageAsync(String code, Locale locale, Object... args) {
        return CompletableFuture.supplyAsync(() -> getMessage(code, locale, args));
    }

    /**
     * 检查消息是否存在
     */
    default boolean hasMessage(String code, Locale locale) {
        return getMessage(code, locale) != null;
    }

    /**
     * 获取支持的消息代码
     */
    default Set<String> getMessageCodes(Locale locale) {
        Map<String, String> messages = getMessages(locale);
        return messages != null ? messages.keySet() : Collections.emptySet();
    }

    @NonNull
    Locale getLocale();

    @NonNull
    default Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    default Set<Locale> getSupportedLocales() {
        return Set.of(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }

    /**
     * 获取消息源统计信息
     */
    default MessageSourceStats getStats() {
        return MessageSourceStats.builder().build();
    }
}

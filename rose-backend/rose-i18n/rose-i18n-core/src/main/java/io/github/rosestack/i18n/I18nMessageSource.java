package io.github.rosestack.i18n;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface I18nMessageSource extends Lifecycle {
    String COMMON_SOURCE = "common";

    @Nullable String getMessage(String code, Locale locale, Object... args);

    @Nullable Map<String, String> getMessages(Locale locale);

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
     * 异步批量获取消息
     */
    default CompletableFuture<Map<String, String>> getMessagesAsync(Set<String> codes, Locale locale) {
        return CompletableFuture.supplyAsync(() -> getMessages(codes, locale));
    }

    /**
     * 批量检查消息是否存在
     */
    default Map<String, Boolean> hasMessages(Set<String> codes, Locale locale) {
        return codes.stream().collect(Collectors.toMap(code -> code, code -> hasMessage(code, locale)));
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

    @NonNull Locale getLocale();

    @NonNull default Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    default List<Locale> getSupportedLocales() {
        return List.of(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}

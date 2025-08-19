package io.github.rosestack.i18n.cache;

import io.github.rosestack.i18n.MessageCacheLoader;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * 默认的空缓存实现，不进行任何缓存操作
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */
public class NoOpMessageCacheLoader implements MessageCacheLoader {

    @Override
    public String getFromCache(String code, Locale locale) {
        return null;
    }

    @Override
    public void putToCache(String code, Locale locale, String message) {
        // 空实现，不进行缓存
    }

    @Override
    public Map<String, String> getFromCache(String[] codes, Locale locale) {
        return Collections.emptyMap();
    }

    @Override
    public void putToCache(Map<String, String> messages, Locale locale) {
        // 空实现，不进行缓存
    }

    @Override
    public void evictCache(Locale locale) {
        // 空实现，不进行缓存
    }

    @Override
    public void clearCache() {
        // 空实现，不进行缓存
    }
}

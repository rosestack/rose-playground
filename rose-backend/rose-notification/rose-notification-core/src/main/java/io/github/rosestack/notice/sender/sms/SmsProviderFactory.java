package io.github.rosestack.notice.sender.sms;

import io.github.rosestack.notice.NoticeException;
import io.github.rosestack.notice.SenderConfiguration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 自动发现所有 SmsProvider 实现，按 providerName 注册。
 * 扩展新服务商只需实现 SmsProvider 并配置 SPI 文件。
 */
public class SmsProviderFactory {
    private static final Map<String, Class<? extends SmsProvider>> PROVIDER_CLASSES = new ConcurrentHashMap<>();
    private static final Cache<String, SmsProvider> PROVIDER_CACHE = Caffeine.newBuilder()
            .maximumSize(getMaxCacheSize())
            .expireAfterAccess(getExpireAfterAccessSeconds())
            .removalListener((String key, SmsProvider value, RemovalCause cause) -> {
                if (value != null) {
                    value.destroy();
                }
            })
            .build();

    static {
        ServiceLoader.load(SmsProvider.class).forEach(provider -> PROVIDER_CLASSES.put(provider.getProviderType(), provider.getClass()));
    }

    public static SmsProvider getProvider(String provider, SenderConfiguration config) {
        if (provider == null) {
            throw new NoticeException("短信服务商为空");
        }
        Class<? extends SmsProvider> clazz = PROVIDER_CLASSES.get(provider);
        if (clazz == null) {
            throw new NoticeException("不支持的短信服务商: " + provider);
        }
        String cfgKey = buildConfigKey(provider, config);
        return PROVIDER_CACHE.get(cfgKey, k -> {
            try {
                java.lang.reflect.Constructor<? extends SmsProvider> ctor = clazz.getDeclaredConstructor();
                if (!ctor.canAccess(null)) {
                    ctor.setAccessible(true);
                }
                SmsProvider instance = ctor.newInstance();
                instance.configure(config);
                return instance;
            } catch (Exception e) {
                throw new NoticeException("创建 SmsProvider 实例失败: " + clazz.getName(), e);
            }
        });
    }

    public static void destroy() {
        // 显式销毁缓存中的实例
        PROVIDER_CACHE.asMap().values().forEach(SmsProvider::destroy);
        PROVIDER_CACHE.invalidateAll();
        PROVIDER_CACHE.cleanUp();
        PROVIDER_CLASSES.clear();
    }

    public static void register(SmsProvider render) {
        if (render == null) {
            return;
        }
        PROVIDER_CLASSES.put(render.getProviderType(), render.getClass());
    }

    private static String buildConfigKey(String provider, SenderConfiguration config) {
        StringBuilder sb = new StringBuilder(provider.toLowerCase());
        sb.append("|");
        if (config != null && config.getConfig() != null) {
            config.getConfig().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append(e.getKey()).append('=').append(String.valueOf(e.getValue())).append('&'));
        }
        return sb.toString();
    }

    private static long getMaxCacheSize() {
        String val = System.getProperty("rose.notification.smsProvider.cache.maxSize", "1000");
        try { return Math.max(100L, Long.parseLong(val)); } catch (Exception ignored) { return 1000L; }
    }

    private static java.time.Duration getExpireAfterAccessSeconds() {
        String val = System.getProperty("rose.notification.smsProvider.cache.expireAfterAccessSeconds", "1800");
        try { long sec = Math.max(60L, Long.parseLong(val)); return java.time.Duration.ofSeconds(sec); } catch (Exception ignored) { return java.time.Duration.ofMinutes(30); }
    }
}

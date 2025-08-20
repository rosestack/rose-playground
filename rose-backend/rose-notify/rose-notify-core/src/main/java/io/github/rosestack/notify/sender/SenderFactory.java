package io.github.rosestack.notify.sender;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.github.rosestack.notify.NotifyException;
import io.github.rosestack.notify.SenderConfiguration;
import io.github.rosestack.notify.spi.Sender;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 Sender 工厂/注册表，支持 SPI 自动发现与运行时动态注册。<br>
 *
 * <ul>
 *   <li>所有 Sender 实现通过 SPI 自动加载并以 getType() 作为 key 注册。
 *   <li>支持 register() 方法运行时动态注册/替换 sender。
 *   <li>getSender() 按字符串 key 获取对应 sender。
 * </ul>
 *
 * <b>扩展方式：</b> 实现 Sender 并配置 SPI 文件，无需手动注册。
 */
public class SenderFactory {
    private static final ConsoleSender CONSOLE_SENDER = new ConsoleSender();
    private static final Map<String, Class<? extends Sender>> SENDER_CLASSES = new ConcurrentHashMap<>();
    private static final Cache<String, Sender> SENDER_CACHE = Caffeine.newBuilder()
            .maximumSize(getMaxCacheSize())
            .expireAfterAccess(getExpireAfterAccessSeconds())
            .removalListener((String key, Sender value, RemovalCause cause) -> {
                if (value != null) {
                    value.destroy();
                }
            })
            .build();

    static {
        ServiceLoader.load(Sender.class)
                .forEach(sender -> SENDER_CLASSES.put(sender.getChannelType().toLowerCase(), sender.getClass()));
    }

    public static Sender getSender(String channel, SenderConfiguration config) {
        if (channel == null) {
            return CONSOLE_SENDER;
        }
        String key = channel.toLowerCase();
        Class<? extends Sender> clazz = SENDER_CLASSES.get(key);
        if (clazz == null) {
            throw new NotifyException("不支持的通知渠道: " + channel);
        }
        String cfgKey = buildConfigKey(key, config);
        return SENDER_CACHE.get(cfgKey, k -> {
            try {
                java.lang.reflect.Constructor<? extends Sender> ctor = clazz.getDeclaredConstructor();
                if (!ctor.canAccess(null)) {
                    ctor.setAccessible(true);
                }
                Sender instance = ctor.newInstance();
                instance.configure(config);
                return instance;
            } catch (Exception e) {
                throw new NotifyException("创建 Sender 实例失败: " + clazz.getName(), e);
            }
        });
    }

    public static void destroy() {
        // 先显式销毁缓存中的实例，再清理缓存
        SENDER_CACHE.asMap().values().forEach(Sender::destroy);
        SENDER_CACHE.invalidateAll();
        SENDER_CACHE.cleanUp();
        SENDER_CLASSES.clear();
    }

    public static void register(String channel, Sender sender) {
        if (channel == null || sender == null) {
            return;
        }
        SENDER_CLASSES.put(channel.toLowerCase(), sender.getClass());
    }

    private static String buildConfigKey(String channel, SenderConfiguration config) {
        StringBuilder sb = new StringBuilder(channel.toLowerCase());
        sb.append("|");
        if (config != null && config.getConfig() != null) {
            config.getConfig().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> sb.append(e.getKey())
                            .append('=')
                            .append(String.valueOf(e.getValue()))
                            .append('&'));
        }
        return sb.toString();
    }

    private static long getMaxCacheSize() {
        String val = System.getProperty("rose.notify.sender.cache.maxSize", "1000");
        try {
            return Math.max(100L, Long.parseLong(val));
        } catch (Exception ignored) {
            return 1000L;
        }
    }

    private static java.time.Duration getExpireAfterAccessSeconds() {
        String val = System.getProperty("rose.notify.sender.cache.expireAfterAccessSeconds", "1800");
        try {
            long sec = Math.max(60L, Long.parseLong(val));
            return java.time.Duration.ofSeconds(sec);
        } catch (Exception ignored) {
            return java.time.Duration.ofMinutes(30);
        }
    }
}

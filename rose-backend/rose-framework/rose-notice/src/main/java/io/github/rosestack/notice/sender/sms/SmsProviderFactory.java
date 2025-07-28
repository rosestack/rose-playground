package io.github.rosestack.notice.sender.sms;

import io.github.rosestack.notice.NoticeException;
import io.github.rosestack.notice.SenderConfiguration;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 自动发现所有 SmsProvider 实现，按 providerName 注册。
 * 扩展新服务商只需实现 SmsProvider 并配置 SPI 文件。
 */
public class SmsProviderFactory {
    private static final Map<String, SmsProvider> PROVIDERS = new ConcurrentHashMap<>();
    private static final Map<String, Map<SenderConfiguration, SmsProvider>> PROVIDER_MAP = new ConcurrentHashMap<>();

    static {
        ServiceLoader.load(SmsProvider.class).forEach(provider -> PROVIDERS.put(provider.getProviderType(), provider));
    }

    public static SmsProvider getProvider(String provider, SenderConfiguration config) {
        if (provider == null) {
            throw new NoticeException("短信服务商为空");
        }
        if (PROVIDER_MAP.containsKey(provider)) {
            SmsProvider sender = PROVIDER_MAP.get(provider).get(config);
            if (sender == null) {
                sender = PROVIDERS.get(provider);
                sender.configure(config);
            }
            return sender;
        } else {
            SmsProvider sender = PROVIDERS.get(provider);
            if (sender == null) {
                throw new NoticeException("不支持的通知渠道: " + provider);
            }
            sender.configure(config);
            Map<SenderConfiguration, SmsProvider> senderMap = new ConcurrentHashMap<>();
            senderMap.put(config, sender);
            PROVIDER_MAP.put(provider, senderMap);

            return sender;
        }
    }

    public static void destroy() {
        PROVIDER_MAP.values().forEach(senderMap -> senderMap.values().forEach(SmsProvider::destroy));
        PROVIDERS.clear();
        PROVIDER_MAP.clear();
    }

    public static void register(SmsProvider render) {
        PROVIDERS.put(render.getProviderType(), render);
    }
}

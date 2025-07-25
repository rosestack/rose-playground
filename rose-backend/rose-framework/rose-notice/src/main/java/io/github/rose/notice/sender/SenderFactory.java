package io.github.rose.notice.sender;

import io.github.rose.notice.NoticeException;
import io.github.rose.notice.SenderConfiguration;
import io.github.rose.notice.spi.Sender;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 Sender 工厂/注册表，支持 SPI 自动发现与运行时动态注册。<br>
 * <ul>
 *   <li>所有 Sender 实现通过 SPI 自动加载并以 getType() 作为 key 注册。</li>
 *   <li>支持 register() 方法运行时动态注册/替换 sender。</li>
 *   <li>getSender() 按字符串 key 获取对应 sender。</li>
 * </ul>
 * <b>扩展方式：</b> 实现 Sender 并配置 SPI 文件，无需手动注册。
 */
public class SenderFactory {
    private static final ConsoleSender CONSOLE_SENDER = new ConsoleSender();
    private static final Map<String, Sender> SENDERS = new ConcurrentHashMap<>();
    private static final Map<String, Map<SenderConfiguration, Sender>> SENDER_MAP = new ConcurrentHashMap<>();

    static {
        ServiceLoader.load(Sender.class)
                .forEach(sender -> SENDERS.put(sender.getChannelType().toLowerCase(), sender));
    }

    public static Sender getSender(String channel, SenderConfiguration config) {
        if (channel == null) {
            return CONSOLE_SENDER;
        }
        if (SENDER_MAP.containsKey(channel.toLowerCase())) {
            Sender sender = SENDER_MAP.get(channel.toLowerCase()).get(config);
            if (sender == null) {
                sender = SENDERS.get(channel.toLowerCase());
                sender.configure(config);
            }
            return sender;
        } else {
            Sender sender = SENDERS.get(channel.toLowerCase());
            if (sender == null) {
                throw new NoticeException("不支持的通知渠道: " + channel);
            }
            sender.configure(config);
            Map<SenderConfiguration, Sender> senderMap = new ConcurrentHashMap<>();
            senderMap.put(config, sender);
            SENDER_MAP.put(channel.toLowerCase(), senderMap);

            return sender;
        }
    }

    public static void destroy() {
        SENDER_MAP.values().forEach(senderMap -> senderMap.values().forEach(Sender::destroy));
        SENDERS.clear();
        SENDER_MAP.clear();
    }

    public static void register(String channel, Sender sender) {
        SENDERS.put(channel.toLowerCase(), sender);
    }
}

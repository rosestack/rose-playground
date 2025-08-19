package io.github.rosestack.notice.spi;

import io.github.rosestack.notice.SendRequest;

/**
 * 通用通知发送渠道 SPI 扩展点。<br>
 * 实现类需在 <b>META-INF/services/io.github.rose.notice.spi.Sender</b> 配置 SPI 文件。<br>
 *
 * <ul>
 *   <li>getType() 用于唯一标识渠道（如 "email"、"sms"、"dingtalk"），支持动态注册与运行时替换。
 *   <li>send() 负责发送通知，参数为 SendRequest。
 *   <li>recall() 默认不支持撤回，部分渠道可重写。
 * </ul>
 *
 * <b>扩展方式：</b> 实现本接口并配置 SPI 文件，无需手动注册。
 */
public interface Sender extends Configurable {
    /**
     * 常用渠道类型 key
     */
    String EMAIL = "email";

    String SMS = "sms";
    String CONSOLE = "console";

    /**
     * 渠道唯一标识（如 "email"、"sms"、"dingtalk"）
     */
    String getChannelType();

    String send(SendRequest sendRequest);

    void destroy();
}

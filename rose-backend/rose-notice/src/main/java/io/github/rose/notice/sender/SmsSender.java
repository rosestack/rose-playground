package io.github.rose.notice.sender;

/**
 * 短信渠道统一入口，自动通过 SPI 发现并分发到具体 SmsProvider 实现。
 * 扩展新服务商只需实现 SmsProvider 并配置 SPI 文件。
 */
import io.github.rose.notice.NoticeException;
import io.github.rose.notice.NoticeRetryableException;
import io.github.rose.notice.SendRequest;
import io.github.rose.notice.SenderConfiguration;
import io.github.rose.notice.sender.sms.SmsProvider;
import io.github.rose.notice.sender.sms.SmsProviderFactory;
import io.github.rose.notice.spi.AbstractConfigure;
import io.github.rose.notice.spi.Sender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class SmsSender extends AbstractConfigure implements Sender {
    private SmsProvider smsProvider;

    @Override
    public String getChannelType() {
        return SMS;
    }

    @Override
    public void doConfigure(SenderConfiguration config) {
        String provider = config != null && config.getConfig().get("sms.provider") != null
                ? config.getConfig().get("sms.provider").toString()
                : "tencent";
        smsProvider = SmsProviderFactory.getProvider(provider, config);
    }

    @Override
    public String send(SendRequest request) {
        try {
            return smsProvider.send(request);
        } catch (NoticeException e) {
            throw e;
        } catch (Exception e) {
            // 其它异常统一封装为可重试异常
            throw new NoticeRetryableException("短信发送异常", e);
        }
    }

    @Override
    public void destroy() {
        SmsProviderFactory.destroy();
    }
}

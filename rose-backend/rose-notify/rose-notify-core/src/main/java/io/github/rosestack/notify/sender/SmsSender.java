package io.github.rosestack.notify.sender;

/**
 * 短信渠道统一入口，自动通过 SPI 发现并分发到具体 SmsProvider 实现。 扩展新服务商只需实现 SmsProvider 并配置 SPI 文件。
 */
import io.github.rosestack.notify.NotifyException;
import io.github.rosestack.notify.NotifyRetryableException;
import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.SenderConfiguration;
import io.github.rosestack.notify.sender.sms.SmsProvider;
import io.github.rosestack.notify.sender.sms.SmsProviderFactory;
import io.github.rosestack.notify.spi.AbstractConfigure;
import io.github.rosestack.notify.spi.Sender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsSender extends AbstractConfigure implements Sender {
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
        } catch (NotifyException e) {
            throw e;
        } catch (Exception e) {
            // 其它异常统一封装为可重试异常
            throw new NotifyRetryableException("短信发送异常", e);
        }
    }

    @Override
    public void destroy() {
        SmsProviderFactory.destroy();
    }
}

package io.github.rose.notice.sender.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import io.github.rose.notice.NoticeException;
import io.github.rose.notice.SendRequest;
import io.github.rose.notice.SenderConfiguration;
import io.github.rose.notice.spi.AbstractConfigure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * 阿里云短信服务商实现。
 * channelConfig 需包含 aliyun.sms.accessKeyId/accessKeySecret/signName/templateCode。
 * content 建议为 JSON 字符串。
 */
public class AliyunSmsProvider extends AbstractConfigure implements SmsProvider {
    private volatile Client client;

    @Override
    public String getProviderType() {
        return ALIYUN;
    }

    public void doConfigure(SenderConfiguration config) throws Exception {
        String accessKeyId = config.getConfig().get("aliyun.sms.accessKeyId").asText();
        String accessKeySecret =
                config.getConfig().get("aliyun.sms.accessKeySecret").asText();
        String endpoint = config.getConfig().get("aliyun.sms.endpoint").asText();

        if (isAnyBlank(accessKeyId, accessKeySecret, endpoint)) {
            throw new NoticeException("阿里云短信配置或参数不完整");
        }

        Config aliyunConfig = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(endpoint);
        client = new Client(aliyunConfig);
    }

    @Override
    public String send(SendRequest request) throws Exception {
        String signName = config.getConfig().get("aliyun.sms.signName").asText();
        String templateCode = config.getConfig().get("aliyun.sms.templateCode").asText();
        String phoneNumber = request.getTarget();
        String templateParam = request.getTemplateContent(); // 建议为 JSON 字符串
        if (isAnyBlank(signName, templateCode, phoneNumber)) {
            throw new NoticeException("阿里云短信配置或参数不完整");
        }
        SendSmsRequest smsRequest = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam);
        SendSmsResponse response = client.sendSms(smsRequest);
        String code = response.getBody().getCode();

        if ("OK".equalsIgnoreCase(code)) {
            return response.getBody().getRequestId();
        } else {
            throw new NoticeException("短信发送失败: " + response.getBody().getMessage());
        }
    }

    @Override
    public void destroy() {}
}

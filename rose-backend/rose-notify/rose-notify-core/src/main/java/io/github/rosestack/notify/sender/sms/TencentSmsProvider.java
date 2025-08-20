package io.github.rosestack.notify.sender.sms;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import io.github.rosestack.core.util.StringPool;
import io.github.rosestack.notify.NotifyException;
import io.github.rosestack.notify.SendRequest;
import io.github.rosestack.notify.SenderConfiguration;
import io.github.rosestack.notify.spi.AbstractConfigure;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/** 腾讯云短信服务商实现。 channelConfig 需包含 tencent.sms.secretId/secretKey/sdkAppId/signName/templateId。 */
public class TencentSmsProvider extends AbstractConfigure implements SmsProvider {
    private volatile SmsClient client;

    @Override
    public String getProviderType() {
        return TENCENT;
    }

    @Override
    public void doConfigure(SenderConfiguration config) throws Exception {
        String secretId = config.getConfig().get("tencent.sms.secretId").toString();
        String secretKey = config.getConfig().get("tencent.sms.secretKey").toString();
        String region = config.getConfig().get("tencent.sms.region").toString();

        Credential cred = new Credential(secretId, secretKey);
        client = new SmsClient(cred, region);
    }

    @Override
    public String send(SendRequest request) throws TencentCloudSDKException {
        String sdkAppId = config.getConfig().get("tencent.sms.sdkAppId").toString();
        String signName = config.getConfig().get("tencent.sms.signName").toString();
        String templateId = config.getConfig().get("tencent.sms.templateId").toString();
        String[] templateParams = request.getTemplateContent() != null
                ? request.getTemplateContent().split(StringPool.COMMA)
                : new String[] {};
        String phoneNumber = request.getTarget();
        if (isAnyBlank(sdkAppId, signName, templateId, phoneNumber)) {
            throw new NotifyException("腾讯云短信配置或参数不完整");
        }

        SendSmsRequest req = new SendSmsRequest();
        req.setSmsSdkAppId(sdkAppId);
        req.setSignName(signName);
        req.setTemplateId(templateId);
        req.setPhoneNumberSet(new String[] {phoneNumber});
        req.setTemplateParamSet(templateParams);

        SendSmsResponse resp = client.SendSms(req);
        if (resp.getSendStatusSet() != null
                && resp.getSendStatusSet().length > 0
                && "Ok".equalsIgnoreCase(resp.getSendStatusSet()[0].getCode())) {
            return resp.getRequestId();
        } else {
            String msg = resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0
                    ? resp.getSendStatusSet()[0].getMessage()
                    : "未知错误";
            throw new NotifyException("短信发送失败: " + msg);
        }
    }

    @Override
    public void destroy() {}
}

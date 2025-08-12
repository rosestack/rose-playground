package io.github.rosestack.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rose.billing.notification")
public class NotificationProperties {
    /** 若没有从 IAM 获取到联系人，使用该固定邮箱作为降级 */
    private String fallbackEmail;

    /** 若未配置固定邮箱，则使用 tenantId + "@" + emailDomain 生成占位邮箱 */
    private String emailDomain = "example.com";
}

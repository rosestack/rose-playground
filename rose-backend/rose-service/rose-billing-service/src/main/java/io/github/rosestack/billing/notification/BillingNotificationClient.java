package io.github.rosestack.billing.notification;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingNotificationClient {

    private final RestTemplate restTemplate;

    @Value("${rose.notification.base-url:http://localhost:18086}")
    private String baseUrl;

    /**
     * 调用通知服务发送通知
     */
    public void send(String target, String targetType, String templateId, Map<String, Object> variables) {
        String url = baseUrl + "/api/notifications/send";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "requestId", UUID.randomUUID().toString(),
                "target", target,
                "targetType", targetType,
                "templateId", templateId,
                "variables", variables);
        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            log.error("调用通知服务失败: url={}, target={}, templateId={}, err={}", url, target, templateId, e.getMessage(), e);
        }
    }
}

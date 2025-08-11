package io.github.rosestack.billing.notification;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillingNotificationClientTest {

    @Test
    void send_postsToNotificationService() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        BillingNotificationClient client = new BillingNotificationClient(restTemplate);
        // 注入 baseUrl 通过反射（简化处理）
        try {
            var f = BillingNotificationClient.class.getDeclaredField("baseUrl");
            f.setAccessible(true);
            f.set(client, "http://localhost:18086");
        } catch (Exception ignored) {}

        client.send("user@example.com", "EMAIL", "TPL_ID", Map.of("k","v"));

        verify(restTemplate, times(1)).postForEntity(eq("http://localhost:18086/api/notifications/send"), any(HttpEntity.class), eq(Void.class));
    }
}


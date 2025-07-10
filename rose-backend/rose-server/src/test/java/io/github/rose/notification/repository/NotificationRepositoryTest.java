package io.github.rose.notification.repository;

import io.github.rose.notification.domain.model.Notification;
import io.github.rose.notification.domain.repository.NotificationRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.domain.value.NotificationStatus;
import io.github.rose.server.RoseServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RoseServerApplication.class)
public class NotificationRepositoryTest {
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void testInsertAndSelect() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setTenantId("tenant-1");
        notification.setChannelType(NotificationChannelType.EMAIL);
        notification.setTarget("user@example.com");
        notification.setContent("Test content");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setSendTime(LocalDateTime.now());
        notificationRepository.save(notification);

        Notification loaded = notificationRepository.findById(notification.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

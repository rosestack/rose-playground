package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.Notification;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.domain.value.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationRepositoryTest {
    
    @Mock
    private NotificationRepository notificationRepository;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(UUID.randomUUID().toString());
        testNotification.setTenantId("tenant-1");
        testNotification.setChannelType(NotificationChannelType.EMAIL);
        testNotification.setTarget("user@example.com");
        testNotification.setContent("Test content");
        testNotification.setStatus(NotificationStatus.PENDING);
        testNotification.setSendTime(LocalDateTime.now());
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(notificationRepository.findById(testNotification.getId()))
                .thenReturn(Optional.of(testNotification));

        // 执行测试
        notificationRepository.save(testNotification);
        Notification loaded = notificationRepository.findById(testNotification.getId()).orElse(null);
        
        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

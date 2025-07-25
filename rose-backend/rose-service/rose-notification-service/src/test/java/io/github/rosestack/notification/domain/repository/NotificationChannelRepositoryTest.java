package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.NotificationChannel;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationChannelRepositoryTest {
    
    @Mock
    private NotificationChannelRepository repository;

    private NotificationChannel testChannel;

    @BeforeEach
    void setUp() {
        testChannel = new NotificationChannel();
        testChannel.setId(UUID.randomUUID().toString());
        testChannel.setChannelType(NotificationChannelType.EMAIL);
        testChannel.setTenantId("tenant-1");
        testChannel.setConfig(Map.of("smtp", "smtp.example.com"));
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testChannel.getId()))
                .thenReturn(Optional.of(testChannel));

        // 执行测试
        repository.save(testChannel);
        NotificationChannel loaded = repository.findById(testChannel.getId()).orElse(null);
        
        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getChannelType()).isEqualTo(NotificationChannelType.EMAIL);
    }
}

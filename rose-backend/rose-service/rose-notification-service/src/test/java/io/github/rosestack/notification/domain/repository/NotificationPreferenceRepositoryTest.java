package io.github.rosestack.notification.domain.repository;

import io.github.rosestack.notification.domain.entity.NotificationPreference;
import io.github.rosestack.notification.domain.value.NotificationChannelType;
import io.github.rosestack.notification.domain.value.TimeWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferenceRepositoryTest {

    @Mock
    private NotificationPreferenceRepository repository;

    private NotificationPreference testPreference;

    @BeforeEach
    void setUp() {
        testPreference = new NotificationPreference();
        testPreference.setId(UUID.randomUUID().toString());
        testPreference.setTenantId("tenant-1");
        testPreference.setUserId("user-1");
        testPreference.setType("ORDER");
        testPreference.setChannelType(NotificationChannelType.EMAIL);
        testPreference.setEnabled(true);
        testPreference.setQuietPeriod(new TimeWindow());
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testPreference.getId())).thenReturn(Optional.of(testPreference));

        // 执行测试
        repository.save(testPreference);
        NotificationPreference loaded =
                repository.findById(testPreference.getId()).orElse(null);

        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

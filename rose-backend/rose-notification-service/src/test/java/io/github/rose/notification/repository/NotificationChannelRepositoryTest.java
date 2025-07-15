package io.github.rose.notification.repository;

import io.github.rose.notification.domain.model.NotificationChannel;
import io.github.rose.notification.domain.repository.NotificationChannelRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationChannelRepositoryTest {
    private NotificationChannelRepository repository;

    @Test
    void testInsertAndSelect() {
        NotificationChannel channel = new NotificationChannel();
        channel.setTenantId("tenant-1");
        channel.setChannelType(NotificationChannelType.EMAIL);
        channel.setEnabled(true);
        channel.setConfig(new HashMap<>());
        repository.save(channel);

        NotificationChannel loaded = repository.findById(channel.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

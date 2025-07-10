package io.github.rose.notification.repository;

import io.github.rose.notification.domain.model.NotificationChannel;
import io.github.rose.notification.domain.repository.NotificationChannelRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.server.RoseServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RoseServerApplication.class)
public class NotificationChannelRepositoryTest {
    @Autowired
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

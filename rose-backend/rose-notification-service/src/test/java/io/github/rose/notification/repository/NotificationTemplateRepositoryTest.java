package io.github.rose.notification.repository;

import io.github.rose.notification.domain.model.NotificationTemplate;
import io.github.rose.notification.domain.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NotificationTemplateRepositoryTest {
    private NotificationTemplateRepository repository;

    @Test
    void testInsertAndSelect() {
        NotificationTemplate template = new NotificationTemplate();
        template.setTenantId("tenant-1");
        template.setName("订单通知");
        template.setType("ORDER");
        template.setContent("Hello, ${name}!");
        template.setEnabled(true);
        template.setVersion(1);
        repository.save(template);

        NotificationTemplate loaded = repository.findById(template.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

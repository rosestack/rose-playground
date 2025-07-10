package io.github.rose.notification.repository;

import io.github.rose.notification.domain.model.NotificationPreference;
import io.github.rose.notification.domain.repository.NotificationPreferenceRepository;
import io.github.rose.notification.domain.value.NotificationChannelType;
import io.github.rose.notification.domain.value.TimeWindow;
import io.github.rose.server.RoseServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RoseServerApplication.class)
public class NotificationPreferenceRepositoryTest {
    @Autowired
    private NotificationPreferenceRepository repository;

    @Test
    void testInsertAndSelect() {
        NotificationPreference pref = new NotificationPreference();
        pref.setTenantId("tenant-1");
        pref.setUserId("user-1");
        pref.setType("ORDER");
        pref.setChannelType(NotificationChannelType.EMAIL);
        pref.setEnabled(true);
        pref.setQuietPeriod(new TimeWindow());
        repository.save(pref);

        NotificationPreference loaded = repository.findById(pref.getId());
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

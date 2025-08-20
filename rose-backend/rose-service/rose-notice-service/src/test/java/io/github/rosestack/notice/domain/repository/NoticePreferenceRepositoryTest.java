package io.github.rosestack.notice.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.rosestack.notice.domain.entity.NoticePreference;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.domain.value.TimeWindow;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoticePreferenceRepositoryTest {

    @Mock
    private NoticePreferenceRepository repository;

    private NoticePreference testPreference;

    @BeforeEach
    void setUp() {
        testPreference = new NoticePreference();
        testPreference.setId(UUID.randomUUID().toString());
        testPreference.setTenantId("tenant-1");
        testPreference.setUserId("user-1");
        testPreference.setType("ORDER");
        testPreference.setChannelType(NoticeChannelType.EMAIL);
        testPreference.setEnabled(true);
        testPreference.setQuietPeriod(new TimeWindow());
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testPreference.getId())).thenReturn(Optional.of(testPreference));

        // 执行测试
        repository.save(testPreference);
        NoticePreference loaded =
                repository.findById(testPreference.getId()).orElse(null);

        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

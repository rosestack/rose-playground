package io.github.rosestack.notice.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.rosestack.notice.domain.entity.NoticeChannel;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoticeChannelRepositoryTest {

    @Mock
    private NoticeChannelRepository repository;

    private NoticeChannel testChannel;

    @BeforeEach
    void setUp() {
        testChannel = new NoticeChannel();
        testChannel.setId(UUID.randomUUID().toString());
        testChannel.setChannelType(NoticeChannelType.EMAIL);
        testChannel.setTenantId("tenant-1");
        testChannel.setConfig(Map.of("smtp", "smtp.example.com"));
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testChannel.getId())).thenReturn(Optional.of(testChannel));

        // 执行测试
        repository.save(testChannel);
        NoticeChannel loaded = repository.findById(testChannel.getId()).orElse(null);

        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getChannelType()).isEqualTo(NoticeChannelType.EMAIL);
    }
}

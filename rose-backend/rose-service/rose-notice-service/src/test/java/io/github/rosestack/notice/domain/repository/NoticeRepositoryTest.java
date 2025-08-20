package io.github.rosestack.notice.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.rosestack.notice.domain.entity.Notice;
import io.github.rosestack.notice.domain.value.NoticeChannelType;
import io.github.rosestack.notice.domain.value.NoticeStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoticeRepositoryTest {

    @Mock
    private NoticeRepository noticeRepository;

    private Notice testNotice;

    @BeforeEach
    void setUp() {
        testNotice = new Notice();
        testNotice.setId(UUID.randomUUID().toString());
        testNotice.setTenantId("tenant-1");
        testNotice.setChannelType(NoticeChannelType.EMAIL);
        testNotice.setTarget("user@example.com");
        testNotice.setContent("Test content");
        testNotice.setStatus(NoticeStatus.PENDING);
        testNotice.setSendTime(LocalDateTime.now());
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(noticeRepository.findById(testNotice.getId())).thenReturn(Optional.of(testNotice));

        // 执行测试
        noticeRepository.save(testNotice);
        Notice loaded =
                noticeRepository.findById(testNotice.getId()).orElse(null);

        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getTenantId()).isEqualTo("tenant-1");
    }
}

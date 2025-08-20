package io.github.rosestack.notice.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.github.rosestack.notice.domain.entity.NoticeTemplate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NoticeTemplateRepositoryTest {

    @Mock
    private NoticeTemplateRepository repository;

    private NoticeTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new NoticeTemplate();
        testTemplate.setId(UUID.randomUUID().toString());
        testTemplate.setName("test-template");
        testTemplate.setContent("Hello {{name}}");
        testTemplate.setLang("en");
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testTemplate.getId())).thenReturn(Optional.of(testTemplate));

        // 执行测试
        repository.save(testTemplate);
        NoticeTemplate loaded = repository.findById(testTemplate.getId()).orElse(null);

        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("test-template");
    }
}

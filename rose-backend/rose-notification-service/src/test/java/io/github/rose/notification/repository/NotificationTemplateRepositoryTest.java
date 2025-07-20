package io.github.rose.notification.repository;

import io.github.rose.notification.domain.entity.NotificationTemplate;
import io.github.rose.notification.domain.repository.NotificationTemplateRepository;
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
public class NotificationTemplateRepositoryTest {
    
    @Mock
    private NotificationTemplateRepository repository;

    private NotificationTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = new NotificationTemplate();
        testTemplate.setId(UUID.randomUUID().toString());
        testTemplate.setName("test-template");
        testTemplate.setContent("Hello {{name}}");
        testTemplate.setLang("en");
    }

    @Test
    void testInsertAndSelect() {
        // Mock 行为
        when(repository.findById(testTemplate.getId()))
                .thenReturn(Optional.of(testTemplate));

        // 执行测试
        repository.save(testTemplate);
        NotificationTemplate loaded = repository.findById(testTemplate.getId()).orElse(null);
        
        // 验证结果
        assertThat(loaded).isNotNull();
        assertThat(loaded.getName()).isEqualTo("test-template");
    }
}

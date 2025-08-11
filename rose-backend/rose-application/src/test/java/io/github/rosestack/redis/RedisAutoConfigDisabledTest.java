package io.github.rosestack.redis;

import io.github.rosestack.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class RedisAutoConfigDisabledTest {

    @Autowired
    ApplicationContext context;

    @Test
    void redisTemplateShouldBeAbsentWhenDisabled() {
        assertThat(context.getBeanNamesForType(RedisTemplate.class)).isEmpty();
    }
}


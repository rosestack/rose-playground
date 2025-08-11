package io.github.rosestack.mybatis;

import io.github.rosestack.TestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class MybatisAutoConfigSmokeTest {

    @Autowired
    ApplicationContext context;

    @Test
    void shouldLoadMybatisPlusInterceptorWhenEnabled() {
        String[] beanNames = context.getBeanNamesForType(com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor.class);
        assertThat(beanNames).isNotEmpty();
    }
}


package io.github.rosestack.web;

import io.github.rosestack.RoseServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RoseServerApplication.class)
@ActiveProfiles("test")
class WebLayerSmokeTest {

    @Autowired
    ApplicationContext context;

    @Test
    void shouldLoadWebAdvicesAndExceptionHandlers() {
        assertThat(context.getBeanNamesForType(org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice.class))
                .anySatisfy(name -> assertThat(name).contains("ApiResponseBodyAdvice"));

        assertThat(context.getBeansWithAnnotation(org.springframework.web.bind.annotation.RestControllerAdvice.class))
                .containsKey("globalExceptionHandler");
    }
}


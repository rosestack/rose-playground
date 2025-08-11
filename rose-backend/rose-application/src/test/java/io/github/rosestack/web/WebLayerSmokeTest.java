package io.github.rosestack.web;

import io.github.rosestack.TestApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestApplication.class)
@ActiveProfiles("test")
class WebLayerSmokeTest {

    @Autowired
    ApplicationContext context;

    @Test
    void shouldLoadWebAdvicesAndExceptionHandlers() {
        assertThat(context.getBeanNamesForType(ResponseBodyAdvice.class))
                .anySatisfy(name -> assertThat(name).contains("ApiResponseBodyAdvice"));

        assertThat(context.getBeansWithAnnotation(RestControllerAdvice.class))
                .containsKey("io.github.rosestack.spring.boot.web.exception.GlobalExceptionHandler");
    }
}


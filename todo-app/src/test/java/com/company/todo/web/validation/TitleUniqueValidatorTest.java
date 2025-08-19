package com.company.todo.web.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.todo.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.HandlerMapping;

class TitleUniqueValidatorTest {

    @SuppressWarnings("unchecked")
    private ConstraintValidator<?, String> newValidator(TodoService svc, HttpServletRequest req) throws Exception {
        // 通过反射创建包可见的 UserNameUniqueValidator
        Class<?> clazz = Class.forName("com.company.todo.web.validation.UserNameUniqueValidator");
        Constructor<?> c = clazz.getDeclaredConstructors()[0];
        c.setAccessible(true);
        return (ConstraintValidator<?, String>) c.newInstance(svc, req);
    }

    @Test
    void should_pass_when_same_id_and_title_unchanged() throws Exception {
        TodoService svc = Mockito.mock(TodoService.class);
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Map<String, String> vars = new HashMap<>();
        vars.put("id", "1");
        Mockito.when(req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(vars);
        Mockito.when(svc.existsTitleExcludeId("t1", 1L)).thenReturn(false);

        var v = newValidator(svc, req);
        boolean ok = v.isValid("t1", Mockito.mock(ConstraintValidatorContext.class));
        assertThat(ok).isTrue();
    }

    @Test
    void should_fail_when_duplicate_other_record() throws Exception {
        TodoService svc = Mockito.mock(TodoService.class);
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(null);
        Mockito.when(svc.existsTitleExcludeId("t1", null)).thenReturn(true);

        var v = newValidator(svc, req);
        boolean ok = v.isValid("t1", Mockito.mock(ConstraintValidatorContext.class));
        assertThat(ok).isFalse();
    }
}

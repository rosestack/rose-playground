package com.company.todo.web.validation;

import com.company.todo.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;

class TitleUniqueValidator implements ConstraintValidator<TitleUnique, String> {

    private final TodoService todoService;
    private final HttpServletRequest request;

    public TitleUniqueValidator(TodoService todoService, HttpServletRequest request) {
        this.todoService = todoService;
        this.request = request;
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
        if (value == null) {
            // no value present
            return true;
        }
        @SuppressWarnings("unchecked")
        final Map<String, String> pathVariables = (Map<String, String>)
                (request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) != null
                        ? request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
                        : Collections.emptyMap());
        final String currentId = pathVariables.get("id");
        Long id = null;
        try {
            if (currentId != null) {
                id = Long.parseLong(currentId);
            }
        } catch (NumberFormatException ignore) {
            id = null;
        }

        return !todoService.existsTitleExcludeId(value, id);
    }
}

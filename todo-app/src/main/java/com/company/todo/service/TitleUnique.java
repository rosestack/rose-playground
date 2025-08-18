package com.company.todo.service;

import static java.lang.annotation.ElementType.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Validate that the name value isn't taken yet.
 */
@Target({FIELD, METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TitleUnique.UserNameUniqueValidator.class)
public @interface TitleUnique {

    String message() default "{Exists.user.name}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class UserNameUniqueValidator implements ConstraintValidator<TitleUnique, String> {

        private final TodoService todoService;
        private final HttpServletRequest request;

        public UserNameUniqueValidator(TodoService todoService, HttpServletRequest request) {
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
            final Map<String, String> pathVariables =
                    ((Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("id");
            if (currentId != null
                    && value.equalsIgnoreCase(
                            todoService.getById(Long.parseLong(currentId)).getTitle())) {
                // value hasn't changed
                return true;
            }
            return !todoService.existsTitle(value);
        }
    }
}

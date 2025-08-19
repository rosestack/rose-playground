package com.company.todo.web.validation;

import static java.lang.annotation.ElementType.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validate that the todo title is unique.
 */
@Target({FIELD, METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UserNameUniqueValidator.class)
public @interface TitleUnique {

    String message() default "{todo.title.exists}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

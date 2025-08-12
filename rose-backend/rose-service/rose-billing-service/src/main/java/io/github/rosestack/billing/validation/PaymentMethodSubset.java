package io.github.rosestack.billing.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.github.rosestack.billing.payment.PaymentMethod;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PaymentMethodSubsetValidator.class)
@Target({FIELD})
@Retention(RUNTIME)
public @interface PaymentMethodSubset {
    PaymentMethod[] anyOf();

    String message() default "invalid payment method";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

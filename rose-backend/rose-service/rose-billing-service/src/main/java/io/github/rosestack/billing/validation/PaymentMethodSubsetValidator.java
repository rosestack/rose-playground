package io.github.rosestack.billing.validation;

import io.github.rosestack.billing.payment.PaymentMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaymentMethodSubsetValidator implements ConstraintValidator<PaymentMethodSubset, String> {
    private Set<String> accepted;

    @Override
    public void initialize(PaymentMethodSubset constraintAnnotation) {
        accepted = Stream.of(constraintAnnotation.anyOf()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true; // 用 @NotBlank 控制必填
        return accepted.contains(value);
    }
}


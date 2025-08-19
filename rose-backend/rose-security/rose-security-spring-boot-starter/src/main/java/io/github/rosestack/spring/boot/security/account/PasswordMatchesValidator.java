package io.github.rosestack.spring.boot.security.account;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordMatchable> {

	@Override
	public void initialize(final PasswordMatches constraintAnnotation) {
		//
	}

	@Override
	public boolean isValid(final PasswordMatchable passwordMatchable, final ConstraintValidatorContext context) {
		return passwordMatchable.getPassword().equals(passwordMatchable.getPasswordAgain());
	}
}

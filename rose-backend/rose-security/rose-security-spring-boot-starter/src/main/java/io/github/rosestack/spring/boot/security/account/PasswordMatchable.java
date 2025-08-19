package io.github.rosestack.spring.boot.security.account;

public interface PasswordMatchable {
	String getPassword();

	String getPasswordAgain();
}

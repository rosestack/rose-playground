package io.github.rosestack.spring.boot.security.core;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 仅用于开发环境的内存用户服务。业务侧可声明自己的 UserDetailsService 进行覆盖。
 */
public class InMemoryUserDetailsService implements UserDetailsService {

	private final UserDetails defaultUser;

	public InMemoryUserDetailsService(PasswordEncoder passwordEncoder) {
		this.defaultUser = User.withUsername("user")
			.password(passwordEncoder.encode("123"))
			.roles("USER")
			.build();
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (defaultUser.getUsername().equals(username)) {
			return defaultUser;
		}
		throw new UsernameNotFoundException("User not found: " + username);
	}
}

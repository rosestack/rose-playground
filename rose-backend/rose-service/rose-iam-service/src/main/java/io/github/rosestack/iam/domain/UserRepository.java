package io.github.rosestack.iam.domain;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);
}

package io.github.rosestack.iam.domain;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
}

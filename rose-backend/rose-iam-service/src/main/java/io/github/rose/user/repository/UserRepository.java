package io.github.rose.user.repository;

import io.github.rose.user.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    void save(User user);

    void deleteAll();
}

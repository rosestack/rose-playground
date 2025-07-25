package io.github.rose.user.application;

import io.github.rose.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@Service
@RequiredArgsConstructor
public class UserCommandService {
    private final UserQueryService userQueryService;
    private final UserRepository userRepository;
}

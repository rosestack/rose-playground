package com.company.usermodulith.user.application;

import com.company.usermodulith.user.domain.UserRepository;
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
public class UserQueryService {
    private final UserRepository userRepository;
}

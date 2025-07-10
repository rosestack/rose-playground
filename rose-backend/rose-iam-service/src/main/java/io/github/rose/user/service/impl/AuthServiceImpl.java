package io.github.rose.user.service.impl;

import io.github.rose.common.exception.BusinessException;
import io.github.rose.user.dto.UserLoginDTO;
import io.github.rose.user.dto.UserRegisterDTO;
import io.github.rose.user.entity.User;
import io.github.rose.user.repository.UserRepository;
import io.github.rose.user.service.AuthService;
import io.github.rose.user.service.CaptchaService;
import io.github.rose.user.service.DistributedLock;
import io.github.rose.user.service.JwtBlacklistService;
import io.github.rose.user.util.JwtUtils;
import io.github.rose.user.vo.LoginVO;
import io.github.rose.user.vo.RegisterVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_TIME_MILLIS = 10 * 60 * 1000L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtBlacklistService jwtBlacklistService;
    private final CaptchaService captchaService;
    private final DistributedLock distributedLock;
//    private final NotificationEventPublisher eventPublisher;

    @Override
    public RegisterVO register(UserRegisterDTO dto) {
        String lockKey = "register:" + dto.getUsername();
        distributedLock.lock(lockKey);
        try {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new BusinessException("user.name.exists");
            }
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new BusinessException("user.email.exists");
            }
            if (userRepository.findByPhone(dto.getPhone()).isPresent()) {
                throw new BusinessException("user.phone.exists");
            }
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            user.setStatus(1);
            userRepository.save(user);
            log.info("[REGISTER] user={}, email={}, phone={}", user.getUsername(), user.getEmail(), user.getPhone());
            // 注册成功后发布增强事件
            String messageId = java.util.UUID.randomUUID().toString();
            String tenantId = null; // TODO: 从上下文获取租户ID
//            eventPublisher.publishEvent(new UserRegisterEvent(messageId, tenantId, user.getUsername(), user.getEmail(), user.getPhone()));
            RegisterVO vo = new RegisterVO();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            return vo;
        } finally {
            distributedLock.unlock(lockKey);
        }
    }

    @Override
    public LoginVO login(UserLoginDTO dto) {
        if (!captchaService.validate(dto.getCaptcha())) {
            throw new BusinessException("captcha.invalid");
        }
        Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
        if (!userOpt.isPresent()) {
            recordLoginAttempt(dto.getUsername(), false);
            throw new BusinessException("login.fail");
        }
        User user = userOpt.get();
        String lockKey = "lock:" + user.getUsername();
        boolean isLocked = Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
        if (isLocked) {
            throw new BusinessException("account.locked");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordLoginAttempt(dto.getUsername(), false);
            throw new BusinessException("login.fail");
        }
        resetLoginAttempts(user.getUsername());
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());
        if (jwtBlacklistService.isBlacklisted(token)) {
            throw new BusinessException("token.blacklisted");
        }
        // 登录成功后发布增强事件
        String traceId2 = java.util.UUID.randomUUID().toString();
        String tenantId2 = null; // TODO: 从上下文获取租户ID
//        eventPublisher.publishEvent(new UserLoginEvent(traceId2, tenantId2, user.getUsername(), user.getEmail(), user.getPhone()));
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setToken(token);
        log.info("[LOGIN] user={}, success, time={}", user.getUsername(), System.currentTimeMillis());
        return vo;
    }

    private void recordLoginAttempt(String username, boolean success) {
        String attemptsKey = "login:attempts:" + username;
        String lockKey = "lock:" + username;
        if (success) {
            resetLoginAttempts(username);
            return;
        }
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            redisTemplate.opsForValue().set(lockKey, "1", LOCK_TIME_MILLIS, java.util.concurrent.TimeUnit.MILLISECONDS);
        }
    }

    private void resetLoginAttempts(String username) {
        String attemptsKey = "login:attempts:" + username;
        String lockKey = "lock:" + username;
        redisTemplate.delete(attemptsKey);
        redisTemplate.delete(lockKey);
    }

    @Override
    public void logout(Long userId) {
        // 实际应获取当前用户 token，这里 mock
        String token = "mock-token";
        jwtBlacklistService.add(token);
    }
}

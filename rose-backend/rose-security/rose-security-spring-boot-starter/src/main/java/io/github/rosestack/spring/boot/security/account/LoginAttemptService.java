package io.github.rosestack.spring.boot.security.account;

import java.time.Duration;

/**
 * 登录尝试与锁定服务 SPI
 */
public interface LoginAttemptService {

    /**
     * 当前用户是否处于锁定状态
     */
    boolean isLocked(String username);

    /**
     * 记录一次成功登录，通常用于清理失败计数与解锁
     */
    void recordSuccess(String username);

    /**
     * 记录一次失败登录，如达到阈值则执行锁定
     */
    void recordFailure(String username);

    /**
     * 获取剩余锁定时长
     */
    Duration getRemainingLockDuration(String username);

    /**
     * 获取当前失败次数
     */
    int getFailureCount(String username);
}

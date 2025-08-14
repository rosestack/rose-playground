package io.github.rosestack.spring.boot.security.extension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 认证钩子接口
 *
 * <p>在认证流程的关键节点提供扩展钩子，允许业务系统注入自定义逻辑
 *
 * @author Rose Team
 * @since 1.0.0
 */
public interface AuthenticationHook {

    /**
     * 登录前钩子
     *
     * @param username 用户名
     * @param password 密码
     * @return true-继续执行，false-中断登录流程
     */
    default boolean beforeLogin(String username, String password) {
        return true;
    }

    /**
     * 登录成功后钩子
     *
     * @param username       用户名
     * @param authentication 认证对象
     */
    default void onLoginSuccess(String username, Authentication authentication) {
        // 默认空实现
    }

    /**
     * 登录失败后钩子
     *
     * @param username  用户名
     * @param exception 认证异常
     */
    default void onLoginFailure(String username, AuthenticationException exception) {
        // 默认空实现
    }

    /**
     * 注销前钩子
     *
     * @param username 用户名
     */
    default void beforeLogout(String username) {
        // 默认空实现
    }

    /**
     * 注销后钩子
     *
     * @param username 用户名
     */
    default void onLogoutSuccess(String username) {
        // 默认空实现
    }

    default void onTokenExpired(String token) {
        // 默认空实现
    }

    default void onTokenRevoked(String token) {
        // 默认空实现
    }

    default void onLockOut(String username) {
        // 默认空实现
    }

    default void onRevoked(String username) {
        // 默认空实现
    }

    /**
     * Token刷新前钩子
     *
     * @param refreshToken 刷新Token
     * @return true-继续执行，false-中断刷新流程
     */
    default boolean beforeTokenRefresh(String refreshToken) {
        return true;
    }

    /**
     * Token刷新成功后钩子
     *
     * @param username       用户名
     * @param newAccessToken 新的访问Token
     */
    default void onTokenRefreshSuccess(String username, String newAccessToken) {
        // 默认空实现
    }
}

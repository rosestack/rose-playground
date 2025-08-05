package io.github.rosestack.spring.boot.web.exception;

import io.github.rosestack.core.exception.BusinessException;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.core.util.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 异常处理助手类
 * <p>
 * 统一处理各种异常，减少重复代码，提供一致的异常处理逻辑
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandlerHelper {

    private final MessageSource messageSource;

    /**
     * 处理异常并返回统一的响应格式
     *
     * @param exception   异常实例
     * @param request     HTTP 请求
     * @param locale      语言环境
     * @param statusCode  HTTP 状态码
     * @param messageKey  消息键
     * @param messageArgs 消息参数
     * @param <T>         异常类型
     * @return 统一的异常响应
     */
    public <T extends Exception> ResponseEntity<ApiResponse<Void>> handleException(
            T exception, HttpServletRequest request, Locale locale,
            int statusCode, String messageKey, Object... messageArgs) {

        // 设置异常属性到请求中，便于后续处理
        request.setAttribute("exception", exception);
        request.setAttribute("exceptionType", exception.getClass().getSimpleName());

        // 获取本地化消息
        String localizedMessage = getLocalizedMessage(messageKey, messageArgs, locale);

        // 统一日志格式
        logException(exception, request, localizedMessage, statusCode);

        // 构建响应
        ApiResponse<Void> response = ApiResponse.error(statusCode, localizedMessage);
        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * 处理业务异常
     *
     * @param exception 业务异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 400,
                exception.getMessageKey(), exception.getMessageArgs());
    }

    /**
     * 处理验证异常
     *
     * @param exception 验证异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 400,
                "validation.error", exception.getMessage());
    }

    /**
     * 处理认证异常
     *
     * @param exception 认证异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 401,
                "authentication.error", exception.getMessage());
    }

    /**
     * 处理授权异常
     *
     * @param exception 授权异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 403,
                "authorization.error", exception.getMessage());
    }

    /**
     * 处理资源未找到异常
     *
     * @param exception 异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 404,
                "resource.not.found", exception.getMessage());
    }

    /**
     * 处理限流异常
     *
     * @param exception 限流异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 429,
                "rate.limit.exceeded", exception.getMessage());
    }

    /**
     * 处理服务器内部错误
     *
     * @param exception 异常
     * @param request   HTTP 请求
     * @param locale    语言环境
     * @return 异常响应
     */
    public ResponseEntity<ApiResponse<Void>> handleInternalServerError(
            Exception exception, HttpServletRequest request, Locale locale) {
        return handleException(exception, request, locale, 500,
                "internal.server.error", "服务器内部错误");
    }

    /**
     * 获取本地化消息
     *
     * @param messageKey  消息键
     * @param messageArgs 消息参数
     * @param locale      语言环境
     * @return 本地化消息
     */
    private String getLocalizedMessage(String messageKey, Object[] messageArgs, Locale locale) {
        try {
            if (StringUtils.hasText(messageKey)) {
                return messageSource.getMessage(messageKey, messageArgs, locale);
            }
        } catch (Exception e) {
            log.error("获取本地化消息失败: messageKey={}, locale={}", messageKey, locale, e);
        }

        // 如果获取本地化消息失败，返回默认消息
        if (messageArgs != null && messageArgs.length > 0 && messageArgs[0] != null) {
            return messageArgs[0].toString();
        }

        return "未知错误";
    }

    /**
     * 记录异常日志
     *
     * @param exception        异常
     * @param request          HTTP 请求
     * @param localizedMessage 本地化消息
     * @param statusCode       状态码
     */
    private void logException(Exception exception, HttpServletRequest request,
                              String localizedMessage, int statusCode) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = ServletUtils.getUserAgent();
        String remoteAddr = ServletUtils.getClientIpAddress();

        // 根据状态码选择日志级别
        if (statusCode >= 500) {
            log.error("服务器异常: {} {} - {}, 消息: {}, 客户端 IP: {}, User-Agent: {}",
                    method, uri, statusCode, localizedMessage, remoteAddr, userAgent, exception);
        } else if (statusCode >= 400) {
            log.warn("客户端异常: {} {} - {}, 消息: {}, 客户端 IP: {}, User-Agent: {}",
                    method, uri, statusCode, localizedMessage, remoteAddr, userAgent, exception);
        } else {
            log.info("异常: {} {} - 消息: {}, 客户端 IP: {}, User-Agent: {}",
                    method, uri, statusCode, localizedMessage, remoteAddr, userAgent, exception);
        }
    }
}

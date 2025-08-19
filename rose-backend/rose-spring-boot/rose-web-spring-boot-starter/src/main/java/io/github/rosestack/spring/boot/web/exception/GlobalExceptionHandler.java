package io.github.rosestack.spring.boot.web.exception;

import io.github.rosestack.core.exception.BusinessException;
import io.github.rosestack.core.exception.RateLimitException;
import io.github.rosestack.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Locale;

/**
 * 优化的全局异常处理器
 *
 * <p>使用 ExceptionHandlerHelper 统一处理各种异常，减少重复代码
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final ExceptionHandlerHelper exceptionHelper;

	/**
	 * 处理业务异常
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(
		BusinessException e, HttpServletRequest request, Locale locale) {
		return exceptionHelper.handleBusinessException(e, request, locale);
	}

	/**
	 * 处理限流异常
	 */
	@ExceptionHandler(RateLimitException.class)
	public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
		RateLimitException e, HttpServletRequest request, Locale locale) {
		return exceptionHelper.handleRateLimitException(e, request, locale);
	}

	/**
	 * 处理参数验证异常
	 */
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
	public ResponseEntity<ApiResponse<Void>> handleValidationException(
		Exception e, HttpServletRequest request, Locale locale) {
		return exceptionHelper.handleValidationException(e, request, locale);
	}

	/** 处理认证和授权异常 注意：需要在项目中添加 Spring Security 依赖后才能使用具体的异常类型 */
	// @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
	// public ResponseEntity<ApiResponse<Void>> handleSecurityException(
	//         Exception e, HttpServletRequest request, Locale locale) {
	//     return exceptionHelper.handleAuthenticationException(e, request, locale);
	// }

	/**
	 * 处理资源未找到异常
	 */
	@ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
	public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
		Exception e, HttpServletRequest request, Locale locale) {
		return exceptionHelper.handleNotFoundException(e, request, locale);
	}

	/**
	 * 处理其他未捕获的异常
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleGenericException(
		Exception e, HttpServletRequest request, Locale locale) {
		return exceptionHelper.handleInternalServerError(e, request, locale);
	}
}

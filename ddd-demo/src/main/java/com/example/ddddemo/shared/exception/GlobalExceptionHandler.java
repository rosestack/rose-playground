package com.example.ddddemo.shared.exception;

import com.example.ddddemo.shared.application.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理应用中的各种异常，并返回标准的API响应格式
 *
 * @author DDD Demo
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return ApiResponse.error("BUSINESS_ERROR", e.getMessage());
    }

    /**
     * 处理领域异常
     */
    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleDomainException(DomainException e) {
        logger.warn("领域异常: {}", e.getMessage());
        return ApiResponse.error("DOMAIN_ERROR", e.getMessage());
    }

    /**
     * 处理基础设施异常
     */
    @ExceptionHandler(InfrastructureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleInfrastructureException(InfrastructureException e) {
        logger.error("基础设施异常: {}", e.getMessage(), e);
        return ApiResponse.error("INFRASTRUCTURE_ERROR", "系统内部错误");
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        logger.warn("参数验证失败: {}", message);
        return ApiResponse.error("VALIDATION_ERROR", message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        logger.warn("参数绑定失败: {}", message);
        return ApiResponse.error("BIND_ERROR", message);
    }

    /**
     * 处理IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("参数异常: {}", e.getMessage());
        return ApiResponse.error("INVALID_ARGUMENT", e.getMessage());
    }

    /**
     * 处理其他运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        return ApiResponse.error("RUNTIME_ERROR", "系统运行时错误");
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        logger.error("未知异常: {}", e.getMessage(), e);
        return ApiResponse.error("UNKNOWN_ERROR", "系统未知错误");
    }
} 
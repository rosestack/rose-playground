package io.github.rose.common.exception;

import io.github.rose.common.Result;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        // 统一国际化处理
        String msg = messageSource.getMessage(
                ex.getMessage() != null ? ex.getMessage() : "server.error",
                null,
                LocaleContextHolder.getLocale()
        );
        return Result.<Void>error(msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        // 国际化全局异常
        String msg = messageSource.getMessage("server.error", null, LocaleContextHolder.getLocale());
        return Result.<Void>error("SERVER_ERROR", msg);
    }
}

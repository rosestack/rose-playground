package io.github.rose.common.exception;

import io.github.rose.common.Result;
import io.github.rose.common.util.MessageUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.<Void>error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        String msg = MessageUtils.getMessage("server.error", null);
        return Result.<Void>error("SERVER_ERROR", msg);
    }
}

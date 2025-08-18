package com.company.todo.exception;

import com.company.todo.web.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidation(Exception ex) {
        String msg = "validation failed";
        return ApiResponse.error("VALIDATION_ERROR", msg);
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        return ApiResponse.error("CONSTRAINT_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleBadBody(HttpMessageNotReadableException ex) {
        return ApiResponse.error("BAD_REQUEST", "request body error");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOthers(Exception ex) {
        return ApiResponse.error("INTERNAL_ERROR", "internal error");
    }
}

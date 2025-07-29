package io.github.rosestack.web.advice;

import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.web.annotation.ResponseIgnore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体包装器
 * <p>
 * 自动将控制器返回的数据包装为统一的 ApiResponse 格式
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查方法或类是否标记了 @ResponseIgnore
        if (returnType.hasMethodAnnotation(ResponseIgnore.class) ||
                returnType.getContainingClass().isAnnotationPresent(ResponseIgnore.class)) {
            return false;
        }

        // 如果返回值已经是 ApiResponse 类型，不需要再次包装
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 如果返回值为 null，包装为成功响应
        if (body == null) {
            return ApiResponse.success();
        }

        // 如果返回值已经是 ApiResponse 类型，直接返回
        if (body instanceof ApiResponse) {
            return body;
        }

        // 包装为成功响应
        ApiResponse<Object> apiResponse = ApiResponse.success(body);

        log.debug("响应体包装完成，数据类型: {}", body.getClass().getSimpleName());

        return apiResponse;
    }
}
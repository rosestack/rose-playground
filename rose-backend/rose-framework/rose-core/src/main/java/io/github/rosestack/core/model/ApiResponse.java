package io.github.rosestack.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一 API 响应对象
 * <p>
 * 提供标准化的 API 响应格式，包含状态码、消息、数据和时间戳
 * </p>
 *
 * @param <T> 响应数据类型
 * @author rosestack
 * @since 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 响应时间戳 */
    private String timestamp;

    /** 请求追踪ID */
    private String traceId;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 创建成功响应
     *
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 创建成功响应（自定义消息）
     *
     * @param message 成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建错误响应
     *
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建错误响应（默认错误码500）
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    /**
     * 创建业务异常响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return error(400, message);
    }

    /**
     * 创建参数验证错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return error(422, message);
    }

    /**
     * 创建未授权错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 创建禁止访问错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 创建资源不存在错误响应
     *
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 设置请求追踪ID
     *
     * @param traceId 追踪ID
     * @return 当前响应对象
     */
    public ApiResponse<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return code == 200;
    }

    /**
     * 判断是否失败
     *
     * @return 是否失败
     */
    public boolean isError() {
        return code != 200;
    }
} 
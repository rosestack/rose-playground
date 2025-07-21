package com.example.ddddemo.shared.application.dto;

import lombok.Data;

/**
 * 统一API响应对象
 * <p>
 * 用于统一API接口的响应格式
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
@Data
public class ApiResponse<T> {

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ApiResponse(String code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("SUCCESS", "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code, message);
    }
}
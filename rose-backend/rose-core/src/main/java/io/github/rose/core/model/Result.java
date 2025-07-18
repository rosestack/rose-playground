package io.github.rose.core.model;

import lombok.Data;

@Data
public class Result<T> {
    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    public static final String SERVER_ERROR = "server.error";
    public static final String SERVER_SUCCESS = "server.success";

    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public static <T> Result<T> success() {
        return build(SUCCESS, SERVER_SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return build(SUCCESS, SERVER_SUCCESS, data);
    }

    public static <T> Result<T> fail() {
        return build(FAIL, "error", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return build(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return build(FAIL, message, null);
    }

    public static <T> Result<T> fail(String message, T data) {
        return build(FAIL, message, data);
    }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }

    public static <T> Boolean isFail(Result<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(Result<T> ret) {
        return Result.SUCCESS == ret.getCode();
    }
}

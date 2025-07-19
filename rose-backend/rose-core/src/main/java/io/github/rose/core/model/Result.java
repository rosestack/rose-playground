package io.github.rose.core.model;

import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * API 响应结果（不可变）
 *
 * @param <T> 数据类型
 * @author rose
 */
@Getter
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

    /**
     * 无参构造函数（支持 Jackson 反序列化）
     */
    public Result() {
    }

    /**
     * 全参构造函数
     */
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result(SUCCESS, SERVER_SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return new Result(SUCCESS, SERVER_SUCCESS, data);
    }

    public static <T> Result<T> fail() {
        return new Result(FAIL, "error", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result(FAIL, message, null);
    }

    public static <T> Result<T> fail(String message, T data) {
        return new Result(FAIL, message, data);
    }

    public static <T> Boolean isFail(Result<T> ret) {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(Result<T> ret) {
        return Result.SUCCESS == ret.getCode();
    }

    /**
     * 判断当前结果是否成功
     */
    public boolean isSuccess() {
        return SUCCESS == this.code;
    }

    /**
     * 判断当前结果是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }

    // ==================== 函数式方法 ====================

    /**
     * 转换数据
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            try {
                R newData = mapper.apply(data);
                return success(newData);
            } catch (Exception e) {
                return fail("数据转换失败: " + e.getMessage());
            }
        } else {
            return fail(this.message);
        }
    }

    /**
     * 扁平化转换
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess()) {
            try {
                return mapper.apply(data);
            } catch (Exception e) {
                return fail("数据转换失败: " + e.getMessage());
            }
        } else {
            return fail(this.message);
        }
    }

    /**
     * 过滤数据
     */
    public Result<T> filter(Predicate<T> predicate, String errorMessage) {
        if (isSuccess() && data != null) {
            try {
                if (predicate.test(data)) {
                    return this;
                } else {
                    return fail(errorMessage);
                }
            } catch (Exception e) {
                return fail("数据过滤失败: " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * 如果成功则执行操作
     */
    public Result<T> onSuccess(Consumer<T> action) {
        if (isSuccess() && data != null) {
            try {
                action.accept(data);
            } catch (Exception e) {
                // 忽略异常，不改变结果状态
            }
        }
        return this;
    }

    /**
     * 如果失败则执行操作
     */
    public Result<T> onError(Consumer<String> action) {
        if (!isSuccess()) {
            try {
                action.accept(message);
            } catch (Exception e) {
                // 忽略异常，不改变结果状态
            }
        }
        return this;
    }

    /**
     * 获取数据，如果失败则返回默认值
     */
    public T getOrElse(T defaultValue) {
        return isSuccess() ? data : defaultValue;
    }

    /**
     * 获取数据，如果失败则使用供应者提供默认值
     */
    public T getOrElseGet(Supplier<T> supplier) {
        return isSuccess() ? data : supplier.get();
    }
}

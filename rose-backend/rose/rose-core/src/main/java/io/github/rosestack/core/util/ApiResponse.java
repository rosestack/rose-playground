package io.github.rosestack.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 统一API响应格式
 *
 * @param <T> 数据类型
 * @author rose
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private Integer code;
    private String message;
    private T data;

    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    /**
     * 创建成功响应（带数据）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("success")
                .data(data)
                .build();
    }

    /**
     * 创建错误响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }

    /**
     * 创建错误响应（带错误码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    // ========== 函数式操作方法 ==========

    /**
     * 转换响应数据
     *
     * @param mapper 数据转换函数
     * @param <R> 转换后的数据类型
     * @return 转换后的响应
     */
    public <R> ApiResponse<R> map(Function<T, R> mapper) {
        if (this.isSuccess()) {
            try {
                R newData = mapper.apply(this.getData());
                return ApiResponse.ok(newData);
            } catch (Exception e) {
                return ApiResponse.error("Data transformation failed: " + e.getMessage());
            }
        } else {
            return ApiResponse.error(this.getCode(), this.getMessage());
        }
    }

    /**
     * 扁平化转换响应
     *
     * @param mapper 转换函数，返回新的 ApiResponse
     * @param <R> 转换后的数据类型
     * @return 转换后的响应
     */
    public <R> ApiResponse<R> flatMap(Function<T, ApiResponse<R>> mapper) {
        if (this.isSuccess()) {
            try {
                return mapper.apply(this.getData());
            } catch (Exception e) {
                return ApiResponse.error("Data transformation failed: " + e.getMessage());
            }
        } else {
            return ApiResponse.error(this.getCode(), this.getMessage());
        }
    }

    /**
     * 过滤响应数据
     *
     * @param predicate 过滤条件
     * @param errorMessage 过滤失败时的错误消息
     * @return 过滤后的响应
     */
    public ApiResponse<T> filter(Predicate<T> predicate, String errorMessage) {
        if (this.isSuccess() && this.getData() != null) {
            try {
                if (predicate.test(this.getData())) {
                    return this;
                } else {
                    return ApiResponse.error(errorMessage);
                }
            } catch (Exception e) {
                return ApiResponse.error("Data filtering failed: " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * 成功时执行操作
     *
     * @param action 要执行的操作
     * @return 当前响应（用于链式调用）
     */
    public ApiResponse<T> onSuccess(Consumer<T> action) {
        if (this.isSuccess() && this.getData() != null) {
            try {
                action.accept(this.getData());
            } catch (Exception e) {
                // 忽略异常以保持响应状态不变
            }
        }
        return this;
    }

    /**
     * 失败时执行操作
     *
     * @param action 要执行的操作
     * @return 当前响应（用于链式调用）
     */
    public ApiResponse<T> onError(Consumer<String> action) {
        if (!this.isSuccess()) {
            try {
                action.accept(this.getMessage());
            } catch (Exception e) {
                // 忽略异常以保持响应状态不变
            }
        }
        return this;
    }

    /**
     * 获取数据或默认值
     *
     * @param defaultValue 默认值
     * @return 数据或默认值
     */
    public T getOrElse(T defaultValue) {
        return this.isSuccess() ? this.getData() : defaultValue;
    }

    /**
     * 获取数据或通过供应商计算默认值
     *
     * @param supplier 默认值供应商
     * @return 数据或计算的默认值
     */
    public T getOrElseGet(Supplier<T> supplier) {
        return this.isSuccess() ? this.getData() : supplier.get();
    }
}

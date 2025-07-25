package io.github.rose.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 不可变的API响应结果包装器，用于一致的响应处理。
 * <p>
 * 该类提供了一种标准化的方式来包装API响应，包含成功/失败状态、错误消息和数据负载。
 * 它遵循函数式编程模式，采用不可变设计，并提供流畅的API方法进行结果处理。
 *
 * <h3>设计原则：</h3>
 * <ul>
 *   <li><strong>不可变性：</strong> 所有实例在创建后都是不可变的</li>
 *   <li><strong>类型安全：</strong> 泛型类型参数确保类型安全的数据处理</li>
 *   <li><strong>函数式风格：</strong> 提供map、flatMap、filter操作</li>
 *   <li><strong>一致的API：</strong> 所有端点的标准化响应格式</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 成功响应带数据
 * ApiResponse<User> userResult = ApiResponse.success(user);
 *
 * // 失败响应带消息
 * ApiResponse<User> errorResult = ApiResponse.error("用户未找到");
 *
 * // 函数式处理
 * ApiResponse<String> nameResult = userResult
 *     .map(User::getName)
 *     .filter(name -> name.length() > 0, "姓名不能为空");
 *
 * // 安全数据提取
 * String userName = nameResult.getOrElse("匿名");
 * }</pre>
 *
 * <h3>响应结构：</h3>
 * <ul>
 *   <li><strong>code:</strong> 类HTTP状态码（200表示成功，500表示失败）</li>
 *   <li><strong>message:</strong> 人类可读的消息或错误描述</li>
 *   <li><strong>data:</strong> 实际的负载数据（可以为null）</li>
 * </ul>
 *
 * @param <T> 此结果中包含的数据类型
 * @author chensoul
 * @since 1.0.0
 */
@Data
public class ApiResponse<T> {

    /**
     * 成功状态码，表示操作成功。
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码，表示操作失败。
     */
    public static final int ERROR = 500;

    /**
     * 此结果的状态码（SUCCESS或FAIL）。
     */
    private Integer code;

    /**
     * 与此结果关联的消息（成功消息或错误描述）。
     */
    private String message;

    /**
     * 此结果的数据负载（可以为null）。
     */
    private T data;

    /**
     * 响应时间戳 - 使用 LocalDateTime 更易读
     * 格式化为 ISO 8601 标准格式
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a successful result without data.
     *
     * @param <T> The type parameter for the result
     * @return A successful ApiResponse instance with no data
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * Creates a successful result with data.
     *
     * @param <T>  The type parameter for the result
     * @param data The data to include in the successful result
     * @return A successful ApiResponse instance containing the provided data
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> result = new ApiResponse<>();
        result.setCode(SUCCESS);
        result.setMessage("Success");
        result.setData(data);
        return result;
    }

    /**
     * Creates a failure result with custom code and message.
     *
     * @param <T>     The type parameter for the result
     * @param code    The custom error code
     * @param message The error message
     * @return A failure ApiResponse instance with the specified code and message
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> result = new ApiResponse<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * Creates a failure result with custom message and default failure code.
     *
     * @param <T>     The type parameter for the result
     * @param message The error message
     * @return A failure ApiResponse instance with the specified message
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(ERROR, message);
    }

    /**
     * Checks if the given result represents a failure.
     *
     * @param <T> The type parameter for the result
     * @param ret The result to check
     * @return true if the result is a failure, false otherwise
     */
    public static <T> Boolean isError(ApiResponse<T> ret) {
        return !isSuccess(ret);
    }

    /**
     * Checks if this result represents a failed operation.
     *
     * @return true if this result is a failure, false otherwise
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * Checks if the given result represents a success.
     *
     * @param <T> The type parameter for the result
     * @param ret The result to check
     * @return true if the result is successful, false otherwise
     */
    public static <T> Boolean isSuccess(ApiResponse<T> ret) {
        return ApiResponse.SUCCESS == ret.getCode();
    }

    /**
     * Checks if this result represents a successful operation.
     *
     * @return true if this result is successful, false otherwise
     */
    public boolean isSuccess() {
        return SUCCESS == this.code;
    }


    /**
     * Transforms the data in this result using the provided mapper function.
     * <p>
     * If this result is successful, applies the mapper function to the data and
     * returns a new successful result with the transformed data. If this result
     * is a failure, returns a new failure result with the same error message.
     *
     * @param <R>    The type of the transformed data
     * @param mapper The function to transform the data
     * @return A new ApiResponse containing the transformed data or the original error
     */
    public <R> ApiResponse<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            try {
                R newData = mapper.apply(data);
                return success(newData);
            } catch (Exception e) {
                return error("Data transformation failed: " + e.getMessage());
            }
        } else {
            return error(this.message);
        }
    }

    /**
     * Performs a flat map transformation on this result.
     * <p>
     * If this result is successful, applies the mapper function to the data and
     * returns the result directly (flattening nested Results). If this result
     * is a failure, returns a new failure result with the same error message.
     *
     * @param <R>    The type of the data in the returned ApiResponse
     * @param mapper The function that transforms data to another ApiResponse
     * @return The ApiResponse returned by the mapper function or the original error
     */
    public <R> ApiResponse<R> flatMap(Function<T, ApiResponse<R>> mapper) {
        if (isSuccess()) {
            try {
                return mapper.apply(data);
            } catch (Exception e) {
                return error("Data transformation failed: " + e.getMessage());
            }
        } else {
            return error(this.message);
        }
    }

    /**
     * Filters the data in this result using the provided predicate.
     * <p>
     * If this result is successful and the data satisfies the predicate,
     * returns this result unchanged. If the predicate fails, returns a
     * failure result with the provided error message.
     *
     * @param predicate    The condition to test the data against
     * @param errorMessage The error message to use if the predicate fails
     * @return This result if the predicate passes, or a failure result if it fails
     */
    public ApiResponse<T> filter(Predicate<T> predicate, String errorMessage) {
        if (isSuccess() && data != null) {
            try {
                if (predicate.test(data)) {
                    return this;
                } else {
                    return error(errorMessage);
                }
            } catch (Exception e) {
                return error("Data filtering failed: " + e.getMessage());
            }
        }
        return this;
    }

    /**
     * Executes the provided action if this result is successful.
     * <p>
     * This method allows for side effects to be performed on successful results
     * without changing the result itself. The action is only executed if the
     * result is successful and contains non-null data.
     *
     * @param action The action to execute with the data if successful
     * @return This result unchanged (for method chaining)
     */
    public ApiResponse<T> onSuccess(Consumer<T> action) {
        if (isSuccess() && data != null) {
            try {
                action.accept(data);
            } catch (Exception e) {
                // Ignore exceptions to maintain result state immutability
            }
        }
        return this;
    }

    /**
     * Executes the provided action if this result is a failure.
     * <p>
     * This method allows for side effects to be performed on failed results
     * without changing the result itself. The action is only executed if the
     * result represents a failure.
     *
     * @param action The action to execute with the error message if failed
     * @return This result unchanged (for method chaining)
     */
    public ApiResponse<T> onError(Consumer<String> action) {
        if (!isSuccess()) {
            try {
                action.accept(message);
            } catch (Exception e) {
                // Ignore exceptions to maintain result state immutability
            }
        }
        return this;
    }

    /**
     * Returns the data if successful, or the provided default value if failed.
     * <p>
     * This method provides a safe way to extract data from a ApiResponse with a
     * fallback value for failure cases.
     *
     * @param defaultValue The value to return if this result is a failure
     * @return The data if successful, or defaultValue if failed
     */
    public T getOrElse(T defaultValue) {
        return isSuccess() ? data : defaultValue;
    }

    /**
     * Returns the data if successful, or computes a default value using the supplier if failed.
     * <p>
     * This method provides lazy evaluation of the default value, which is useful
     * when the default value is expensive to compute or should only be computed
     * when actually needed.
     *
     * @param supplier The supplier to compute the default value if this result is a failure
     * @return The data if successful, or the value provided by the supplier if failed
     */
    public T getOrElseGet(Supplier<T> supplier) {
        return isSuccess() ? data : supplier.get();
    }
}

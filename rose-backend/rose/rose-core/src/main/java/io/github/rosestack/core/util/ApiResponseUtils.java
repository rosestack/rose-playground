package io.github.rosestack.core.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.rosestack.core.util.ApiResponse.error;

public class ApiResponseUtils {
	private ApiResponseUtils() {
	}

	/**
	 * Transforms the data in this result using the provided mapper function.
	 *
	 * <p>If this result is successful, applies the mapper function to the data and returns a new
	 * successful result with the transformed data. If this result is a failure, returns a new failure
	 * result with the same error message.
	 *
	 * @param <R>    The type of the transformed data
	 * @param mapper The function to transform the data
	 * @return A new ApiResponse containing the transformed data or the original error
	 */
	public static <T, R> ApiResponse<R> map(ApiResponse<T> old, Function<T, R> mapper) {
		if (old.isSuccess()) {
			try {
				R newData = mapper.apply(old.getData());
				return ApiResponse.ok(newData);
			} catch (Exception e) {
				return ApiResponse.error("Data transformation failed: " + e.getMessage());
			}
		} else {
			return ApiResponse.error(old.getMessage());
		}
	}

	/**
	 * Performs a flat map transformation on this result.
	 *
	 * <p>If this result is successful, applies the mapper function to the data and returns the result
	 * directly (flattening nested Results). If this result is a failure, returns a new failure result
	 * with the same error message.
	 *
	 * @param <R>    The type of the data in the returned ApiResponse
	 * @param mapper The function that transforms data to another ApiResponse
	 * @return The ApiResponse returned by the mapper function or the original error
	 */
	public static <T, R> ApiResponse<R> flatMap(ApiResponse<T> old, Function<T, ApiResponse<R>> mapper) {
		if (old.isSuccess()) {
			try {
				return mapper.apply(old.getData());
			} catch (Exception e) {
				return error("Data transformation failed: " + e.getMessage());
			}
		} else {
			return error(old.getMessage());
		}
	}

	/**
	 * Filters the data in this result using the provided predicate.
	 *
	 * <p>If this result is successful and the data satisfies the predicate, returns this result
	 * unchanged. If the predicate fails, returns a failure result with the provided error message.
	 *
	 * @param predicate    The condition to test the data against
	 * @param errorMessage The error message to use if the predicate fails
	 * @return This result if the predicate passes, or a failure result if it fails
	 */
	public static <T> ApiResponse<T> filter(ApiResponse<T> old, Predicate<T> predicate, String errorMessage) {
		if (old.isSuccess() && old.getData() != null) {
			try {
				if (predicate.test(old.getData())) {
					return old;
				} else {
					return error(errorMessage);
				}
			} catch (Exception e) {
				return error("Data filtering failed: " + e.getMessage());
			}
		}
		return old;
	}

	/**
	 * Executes the provided action if this result is successful.
	 *
	 * <p>This method allows for side effects to be performed on successful results without changing
	 * the result itself. The action is only executed if the result is successful and contains
	 * non-null data.
	 *
	 * @param action The action to execute with the data if successful
	 * @return This result unchanged (for method chaining)
	 */
	public static <T> ApiResponse<T> onSuccess(ApiResponse<T> old, Consumer<T> action) {
		if (old.isSuccess() && old.getData() != null) {
			try {
				action.accept(old.getData());
			} catch (Exception e) {
				// Ignore exceptions to maintain result state immutability
			}
		}
		return old;
	}

	/**
	 * Executes the provided action if this result is a failure.
	 *
	 * <p>This method allows for side effects to be performed on failed results without changing the
	 * result itself. The action is only executed if the result represents a failure.
	 *
	 * @param action The action to execute with the error message if failed
	 * @return This result unchanged (for method chaining)
	 */
	public static <T> ApiResponse<T> onError(ApiResponse<T> old, Consumer<String> action) {
		if (!old.isSuccess()) {
			try {
				action.accept(old.getMessage());
			} catch (Exception e) {
				// Ignore exceptions to maintain result state immutability
			}
		}
		return old;
	}

	/**
	 * Returns the data if successful, or the provided default value if failed.
	 *
	 * <p>This method provides a safe way to extract data from a ApiResponse with a fallback value for
	 * failure cases.
	 *
	 * @param defaultValue The value to return if this result is a failure
	 * @return The data if successful, or defaultValue if failed
	 */
	public static <T> T getOrElse(ApiResponse<T> old, T defaultValue) {
		return old.isSuccess() ? old.getData() : defaultValue;
	}

	/**
	 * Returns the data if successful, or computes a default value using the supplier if failed.
	 *
	 * <p>This method provides lazy evaluation of the default value, which is useful when the default
	 * value is expensive to compute or should only be computed when actually needed.
	 *
	 * @param supplier The supplier to compute the default value if this result is a failure
	 * @return The data if successful, or the value provided by the supplier if failed
	 */
	public static <T> T getOrElseGet(ApiResponse<T> old, Supplier<T> supplier) {
		return old.isSuccess() ? old.getData() : supplier.get();
	}
}

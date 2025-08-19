package io.github.rosestack.core.lang.function.checked;

import java.util.function.BiFunction;

/**
 * A {@link BiFunction} that allows for checked exceptions.
 */
@FunctionalInterface
public interface CheckedTriFunction<T, U, V, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u, V v) throws Exception;
}

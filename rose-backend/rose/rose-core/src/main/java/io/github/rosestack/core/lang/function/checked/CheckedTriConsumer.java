package io.github.rosestack.core.lang.function.checked;

@FunctionalInterface
public interface CheckedTriConsumer<T, U, V> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     */
    void accept(T t, U u, V v) throws Exception;
}

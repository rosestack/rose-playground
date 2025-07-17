package io.github.rose.core.util;

/**
 * A marker interface serving as a base for utility classes within the Rose framework.
 * <p>
 * This interface is intended to be extended by utility classes that provide static methods
 * for common operations. It helps in organizing utility classes under a common type hierarchy.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 * public final class MyUtils implements Utils {
 *     private MyUtils() {}
 *
 *     public static void doSomething() {
 *         // Utility logic here
 *     }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
public interface Utils {
}

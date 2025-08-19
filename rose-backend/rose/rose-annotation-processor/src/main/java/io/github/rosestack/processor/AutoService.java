package io.github.rosestack.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An annotation for service providers as described in {@link java.util.ServiceLoader}. The
 * annotation processor generates the configuration files that allow the annotated class to be
 * loaded with {@link java.util.ServiceLoader#load(Class)}.
 *
 * <p>The annotated class must conform to the service provider specification. Specifically, it must:
 *
 * <ul>
 *   <li>be a non-inner, non-anonymous, concrete class
 *   <li>have a publicly accessible no-arg constructor
 *   <li>implement the interface type returned by {@code value()}
 * </ul>
 */
@Retention(SOURCE)
@Documented
@Target(TYPE)
public @interface AutoService {
	/**
	 * @return
	 */
	Class<?>[] value() default void.class;
}

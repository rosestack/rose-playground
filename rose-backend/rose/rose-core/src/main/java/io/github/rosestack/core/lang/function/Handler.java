package io.github.rosestack.core.lang.function;

/**
 * @param <I>
 * @param <O>
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 */
@FunctionalInterface
public interface Handler<I, O> {

    O process(I input);
}

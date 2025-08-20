package io.github.rosestack.core.util.function;

import java.util.function.Predicate;

/**
 * @param <I>
 * @param <O>
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 */
public class Pipeline<I, O> {

    private final Handler<I, O> currentHandler;

    public Pipeline(Handler<I, O> currentHandler) {
        this.currentHandler = currentHandler;
    }

    public <K> Pipeline<I, K> addHandler(Handler<O, K> newHandler) {
        return new Pipeline<>(input -> newHandler.process(currentHandler.process(input)));
    }

    public Pipeline<I, O> filter(Predicate<O> predicate) {
        return addHandler(input -> predicate.test(input) ? input : null);
    }

    public O execute(I input) {
        return currentHandler.process(input);
    }
}

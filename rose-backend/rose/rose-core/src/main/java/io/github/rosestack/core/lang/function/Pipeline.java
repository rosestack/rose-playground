package io.github.rosestack.core.lang.function;

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

    public O execute(I input) {
        return currentHandler.process(input);
    }
}

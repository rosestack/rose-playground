package io.github.rosestack.core.lang.function;

import static java.util.stream.Collectors.toList;

import io.github.rosestack.core.lang.function.checked.CheckedConsumer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 0.0.1
 */
public interface Streams {

    static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * <p>
     * filterStream.
     * </p>
     *
     * @param values    a S object
     * @param predicate a {@link Predicate} object
     * @param <T>       a T class
     * @param <S>       a S class
     * @return a {@link Stream} object
     */
    static <T, S extends Iterable<T>> Stream<T> filterStream(S values, Predicate<? super T> predicate) {
        return StreamSupport.stream(values.spliterator(), false).filter(predicate);
    }

    /**
     * <p>
     * filterList.
     * </p>
     *
     * @param values    a S object
     * @param predicate a {@link Predicate} object
     * @param <T>       a T class
     * @param <S>       a S class
     * @return a {@link List} object
     */
    static <T, S extends Iterable<T>> List<T> filterList(S values, Predicate<? super T> predicate) {
        return filterStream(values, predicate).collect(toList());
    }

    /**
     * <p>
     * filterSet.
     * </p>
     *
     * @param values    a S object
     * @param predicate a {@link Predicate} object
     * @param <T>       a T class
     * @param <S>       a S class
     * @return a {@link Set} object
     */
    static <T, S extends Iterable<T>> Set<T> filterSet(S values, Predicate<? super T> predicate) {
        // new Set with insertion order
        return filterStream(values, predicate).collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    /**
     * <p>
     * filter.
     * </p>
     *
     * @param values    a S object
     * @param predicate a {@link Predicate} object
     * @param <T>       a T class
     * @param <S>       a S class
     * @return a S object
     */
    static <T, S extends Iterable<T>> S filter(S values, Predicate<? super T> predicate) {
        final boolean isSet = Set.class.isAssignableFrom(values.getClass());
        return (S) (isSet ? filterSet(values, predicate) : filterList(values, predicate));
    }

    /**
     * <p>
     * filterAll.
     * </p>
     *
     * @param values     a S object
     * @param predicates a {@link Predicate} object
     * @param <T>        a T class
     * @param <S>        a S class
     * @return a S object
     */
    @SafeVarargs
    static <T, S extends Iterable<T>> S filterAll(S values, Predicate<T>... predicates) {
        return filter(values, Predicates.and(predicates));
    }

    /**
     * <p>
     * filterAny.
     * </p>
     *
     * @param values     a S object
     * @param predicates a {@link Predicate} object
     * @param <T>        a T class
     * @param <S>        a S class
     * @return a S object
     */
    @SafeVarargs
    static <T, S extends Iterable<T>> S filterAny(S values, Predicate<T>... predicates) {
        return filter(values, Predicates.or(predicates));
    }

    /**
     * <p>
     * filterFirst.
     * </p>
     *
     * @param values     a {@link Iterable} object
     * @param predicates a {@link Predicate} object
     * @param <T>        a T class
     * @return a T object
     */
    @SafeVarargs
    static <T> T filterFirst(Iterable<T> values, Predicate<T>... predicates) {
        return StreamSupport.stream(values.spliterator(), false)
                .filter(Predicates.and(predicates))
                .findFirst()
                .orElse(null);
    }

    /**
     * 对每个元素执行操作
     */
    static <T> void forEach(Iterable<T> values, Consumer<T> action) {
        if (values == null) {
            return;
        }
        Objects.requireNonNull(action, "action cannot be null");
        values.forEach(action);
    }

    /**
     * 对每个元素执行可能抛出异常的操作（重载）
     */
    static <T> void forEach(Iterable<T> values, CheckedConsumer<T> action) {
        if (values == null) {
            return;
        }
        Objects.requireNonNull(action, "action cannot be null");
        values.forEach(action.unchecked());
    }
}

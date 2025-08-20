package io.github.rosestack.core.util.function;

import java.util.function.Predicate;

public final class Predicates {

    @SuppressWarnings("rawtypes")
    private static final Predicate[] EMPTY_PREDICATE_ARRAY = new Predicate[0];

    private Predicates() {}

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T>[] emptyArray() {
        return (Predicate<T>[]) EMPTY_PREDICATE_ARRAY;
    }

    public static <T> Predicate<T> alwaysTrue() {
        return e -> Boolean.TRUE;
    }

    public static <T> Predicate<T> alwaysFalse() {
        return e -> Boolean.FALSE;
    }

    public static <T> Predicate<T> of(boolean condition) {
        return e -> condition;
    }

    /**
     * a composed predicate that represents a short-circuiting logical AND of
     * {@link Predicate predicates}
     *
     * @param predicates {@link Predicate predicates}
     * @param <T>        the type to test
     * @return non-null
     */
    @SafeVarargs
    public static <T> Predicate<T> and(Predicate<T>... predicates) {
        int length = predicates == null ? 0 : predicates.length;
        if (length == 0) {
            return alwaysTrue();
        } else if (length == 1) {
            return predicates[0];
        } else {
            Predicate<T> andPredicate = alwaysTrue();
            for (Predicate<T> p : predicates) {
                andPredicate = andPredicate.and(p);
            }
            return andPredicate;
        }
    }

    @SafeVarargs
    public static <T> Predicate<T> not(Predicate<T>... predicates) {
        if (predicates == null || predicates.length == 0) {
            // 对空数组的处理逻辑
            return e -> true;
        } else if (predicates.length == 1) {
            // 如果只有一个Predicate，直接对其取反
            return predicates[0].negate();
        } else {
            // 如果有多个Predicate，对它们全部取反
            Predicate<T> notPredicate = alwaysTrue();
            for (Predicate<T> p : predicates) {
                notPredicate = notPredicate.and(p.negate());
            }
            return notPredicate;
        }
    }

    /**
     * a composed predicate that represents a short-circuiting logical OR of
     * {@link Predicate predicates}
     *
     * @param predicates {@link Predicate predicates}
     * @param <T>        the detected type
     * @return non-null
     */
    @SafeVarargs
    public static <T> Predicate<T> or(Predicate<T>... predicates) {
        int length = predicates == null ? 0 : predicates.length;
        if (length == 0) {
            return alwaysTrue();
        } else if (length == 1) {
            return predicates[0];
        } else {
            Predicate<T> orPredicate = alwaysFalse();
            for (Predicate<T> p : predicates) {
                orPredicate = orPredicate.or(p);
            }
            return orPredicate;
        }
    }
}

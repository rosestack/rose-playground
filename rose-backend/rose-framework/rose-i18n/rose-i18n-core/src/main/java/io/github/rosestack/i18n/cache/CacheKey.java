package io.github.rosestack.i18n.cache;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@Getter
@ToString
public class CacheKey {
    private final String code;
    private final Object[] args;
    private final Locale locale;

    public CacheKey(String code, Locale locale, Object... args) {
        this.code = code;
        this.args = args != null ? Arrays.copyOf(args, args.length) : null;
        this.locale = locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey)) return false;
        CacheKey that = (CacheKey) o;
        return Objects.equals(code, that.code)
                && Arrays.deepEquals(args, that.args)
                && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(code, locale);
        result = 31 * result + Arrays.deepHashCode(args);
        return result;
    }
}
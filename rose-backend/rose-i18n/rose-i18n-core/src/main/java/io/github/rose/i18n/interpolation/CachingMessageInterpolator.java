package io.github.rose.i18n.interpolation;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachingMessageInterpolator implements MessageInterpolator {
    private final MessageInterpolator delegate;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public CachingMessageInterpolator(MessageInterpolator delegate) {
        this.delegate = delegate;
    }

    @Override
    public String interpolate(String message, Object args, Locale locale) {
        String key = message + "::" + args.hashCode();
        return cache.computeIfAbsent(key, k -> delegate.interpolate(message, args, locale));
    }
}
package io.github.rose.i18n;

import io.github.rose.i18n.util.MessageUtils;

import java.util.Arrays;
import java.util.StringJoiner;

public class I18nMessageException extends RuntimeException {

    private final String message;
    private final Object[] args;

    public I18nMessageException(String message, Object... args) {
        this.message = message;
        this.args = args;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getLocalizedMessage() {
        return MessageUtils.getLocalizedMessage(message, args);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", I18nMessageException.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("args=" + Arrays.toString(args))
                .add("localized message='" + getLocalizedMessage() + "'")
                .toString();
    }
}
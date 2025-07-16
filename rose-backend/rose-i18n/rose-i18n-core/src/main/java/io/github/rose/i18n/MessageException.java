package io.github.rose.i18n;

import io.github.rose.i18n.util.MessageUtils;

import java.util.Arrays;

public class MessageException extends RuntimeException {

    private final String rawMessage;
    private final Object[] args;

    public MessageException(String rawMessage, Object... args) {
        this.rawMessage = rawMessage;
        this.args = args;
    }

    @Override
    public String getMessage() {
        return rawMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return MessageUtils.getLocalizedMessage(rawMessage, args);
    }

    @Override
    public String toString() {
        return String.format("%s[message='%s', args=%s, localized='%s']",
                getClass().getSimpleName(), rawMessage, Arrays.toString(args), getLocalizedMessage());
    }
}
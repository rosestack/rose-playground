package io.github.rose.i18n;

public class MessageSourceException extends RuntimeException {
    public MessageSourceException(String message, Exception e) {
        super(message, e);
    }
}

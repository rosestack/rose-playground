package io.github.rose.common.model;

public interface HasEmail extends HasName {

    String EMAIL_REGEXP = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";

    String getEmail();
}

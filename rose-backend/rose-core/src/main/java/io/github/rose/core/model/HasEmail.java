package io.github.rose.core.model;

public interface HasEmail  {

    String EMAIL_REGEXP = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";

    String getEmail();
}

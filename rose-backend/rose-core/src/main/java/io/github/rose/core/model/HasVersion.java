package io.github.rose.core.model;

public interface HasVersion {

    Long getVersion();

    default void setVersion(Long version) {
    }
}

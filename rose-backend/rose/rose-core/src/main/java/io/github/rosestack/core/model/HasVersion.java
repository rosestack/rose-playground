package io.github.rosestack.core.model;

public interface HasVersion {

    Long getVersion();

    default void setVersion(Long version) {
    }
}

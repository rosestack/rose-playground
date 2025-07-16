package io.github.rose.common.model;

public interface HasVersion {

    Long getVersion();

    default void setVersion(Long version) {
    }
}

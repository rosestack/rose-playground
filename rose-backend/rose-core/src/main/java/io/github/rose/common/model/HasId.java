package io.github.rose.common.model;

import java.io.Serializable;

public interface HasId<ID extends Serializable> extends Serializable {

    ID getId();
}

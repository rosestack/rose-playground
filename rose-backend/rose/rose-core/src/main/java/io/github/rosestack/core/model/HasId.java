package io.github.rosestack.core.model;

import java.io.Serializable;

public interface HasId<ID extends Serializable> extends Serializable {

    ID getId();
}

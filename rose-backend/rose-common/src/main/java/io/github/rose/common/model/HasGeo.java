package io.github.rose.common.model;

import java.io.Serializable;

public interface HasGeo {

    /**
     * 纬度
     */
    Integer getLatitude();

    /**
     * 经度
     */
    Integer getLongitude();
}

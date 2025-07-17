package io.github.rose.core.model;

import lombok.Data;

@Data
public class GeoAddress extends Address {
    /**
     * 经度
     */
    private Integer longitude;
    /**
     * 纬度
     */
    private Integer latitude;
}

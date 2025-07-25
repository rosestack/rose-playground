package io.github.rosestack.core.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
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

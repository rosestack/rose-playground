package io.github.rosestack.core.model;

import lombok.Data;

/**
 * 地址信息基类
 *
 * @author rose
 */
@Data
public abstract class Address {
    /**
     * 所在国家
     */
    protected String country;

    /**
     * 所在省份
     */
    protected String province;

    /**
     * 所在城市
     */
    protected String city;

    /**
     * 用户所在区域
     */
    protected String region;

    /**
     * 所处街道地址
     */
    protected String streetAddress;

    /**
     * 所处地址
     */
    protected String address;

    /**
     * 邮政编码号
     */
    protected String postalCode;

    /**
     * 标准的完整地址
     */
    public String getFormatted() {
        StringBuilder sb = new StringBuilder();
        if (country != null) sb.append(country);
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (region != null) sb.append(region);
        if (streetAddress != null) sb.append(streetAddress);
        if (address != null) sb.append(address);
        if (postalCode != null) sb.append(postalCode);
        return sb.toString();
    }
}

package io.github.rose.core.model;

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
        return country + province + city + region + streetAddress + address + postalCode;
    }
}

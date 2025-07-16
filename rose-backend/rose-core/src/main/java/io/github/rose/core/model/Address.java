<<<<<<< HEAD:rose-backend/rose-core/src/main/java/io/github/rose/core/model/Address.java
package io.github.rose.core.model;
=======
package io.github.rose.common.model;
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现):rose-backend/rose-common/src/main/java/io/github/rose/common/model/Address.java

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

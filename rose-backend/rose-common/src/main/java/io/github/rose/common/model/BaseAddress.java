package io.github.rose.common.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseAddress<ID extends Serializable> extends BaseAudit<ID> {

    public BaseAddress(ID id) {
        super(id);
    }

    public BaseAddress(BaseAudit<ID> data) {
        super(data);
    }

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

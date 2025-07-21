package com.example.ddddemo.user.application.dto;

/**
 * 地址数据传输对象
 * <p>
 * 用于在应用层和接口层之间传输地址数据
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class AddressDTO {

    /** 国家 */
    private String country;
    
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 区县 */
    private String district;
    
    /** 详细地址 */
    private String detailAddress;
    
    /** 邮政编码 */
    private String postalCode;

    // 构造函数
    public AddressDTO() {}

    public AddressDTO(String country, String province, String city, String district,
                      String detailAddress, String postalCode) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
        this.postalCode = postalCode;
    }

    // Getter和Setter方法
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return "AddressDTO{" +
                "country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
} 
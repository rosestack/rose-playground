package com.example.ddddemo.user.domain.valueobject;

/**
 * 地址值对象
 * <p>
 * 表示用户的地址信息，是不可变的值对象
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>地址信息封装</li>
 *   <li>不可变性</li>
 *   <li>值相等性</li>
 * </ul>
 *
 * @author DDD Demo
 * @since 1.0.0
 */
public class Address {

    /** 国家 */
    private final String country;
    
    /** 省份 */
    private final String province;
    
    /** 城市 */
    private final String city;
    
    /** 区县 */
    private final String district;
    
    /** 详细地址 */
    private final String detailAddress;
    
    /** 邮政编码 */
    private final String postalCode;

    /**
     * 构造函数
     *
     * @param country 国家
     * @param province 省份
     * @param city 城市
     * @param district 区县
     * @param detailAddress 详细地址
     * @param postalCode 邮政编码
     */
    public Address(String country, String province, String city, String district, 
                   String detailAddress, String postalCode) {
        this.country = country;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailAddress = detailAddress;
        this.postalCode = postalCode;
    }

    /**
     * 获取国家
     */
    public String getCountry() {
        return country;
    }

    /**
     * 获取省份
     */
    public String getProvince() {
        return province;
    }

    /**
     * 获取城市
     */
    public String getCity() {
        return city;
    }

    /**
     * 获取区县
     */
    public String getDistrict() {
        return district;
    }

    /**
     * 获取详细地址
     */
    public String getDetailAddress() {
        return detailAddress;
    }

    /**
     * 获取邮政编码
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * 获取完整地址字符串
     *
     * @return 完整地址
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (country != null && !country.trim().isEmpty()) {
            sb.append(country);
        }
        if (province != null && !province.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(province);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(city);
        }
        if (district != null && !district.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(district);
        }
        if (detailAddress != null && !detailAddress.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(detailAddress);
        }
        
        return sb.toString();
    }

    /**
     * 检查地址是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return (country == null || country.trim().isEmpty()) &&
               (province == null || province.trim().isEmpty()) &&
               (city == null || city.trim().isEmpty()) &&
               (district == null || district.trim().isEmpty()) &&
               (detailAddress == null || detailAddress.trim().isEmpty());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Address address = (Address) obj;
        
        if (country != null ? !country.equals(address.country) : address.country != null) return false;
        if (province != null ? !province.equals(address.province) : address.province != null) return false;
        if (city != null ? !city.equals(address.city) : address.city != null) return false;
        if (district != null ? !district.equals(address.district) : address.district != null) return false;
        if (detailAddress != null ? !detailAddress.equals(address.detailAddress) : address.detailAddress != null) return false;
        return postalCode != null ? postalCode.equals(address.postalCode) : address.postalCode == null;
    }

    @Override
    public int hashCode() {
        int result = country != null ? country.hashCode() : 0;
        result = 31 * result + (province != null ? province.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (detailAddress != null ? detailAddress.hashCode() : 0);
        result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Address{" +
                "country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", detailAddress='" + detailAddress + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
} 
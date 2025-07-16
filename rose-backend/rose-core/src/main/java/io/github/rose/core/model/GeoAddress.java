<<<<<<< HEAD:rose-backend/rose-core/src/main/java/io/github/rose/core/model/GeoAddress.java
package io.github.rose.core.model;
=======
package io.github.rose.common.model;
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现):rose-backend/rose-common/src/main/java/io/github/rose/common/model/GeoAddress.java

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

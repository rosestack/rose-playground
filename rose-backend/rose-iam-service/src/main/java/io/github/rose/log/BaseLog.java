package io.github.rose.log;

<<<<<<< HEAD
import io.github.rose.core.model.GeoAddress;
import io.github.rose.core.model.ParsedUserAgent;
=======
import io.github.rose.common.model.GeoAddress;
import io.github.rose.common.model.ParsedUserAgent;
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现)
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseLog {
    private Long id;

    /**
     * 登录应用
     */
    private String appId;
    private String appName;
    private String appLogo;

    /**
     * 登录租户
     */
    private String tenantId;
    private String tenantName;
    private String tenantLogo;

    /**
     * 登录用户
     */
    private String userId;
    // 按照以下用户字段顺序进行展示：nickname > username > name > givenName > familyName -> email -> phone -> id
    private String userName;
    private String userAvatar;

    //登录用户池
    private String userPoolId;

    /**
     * 登录地址
     */
    private String clientIp;
    private GeoAddress geoAddress;

    private String requestUri;
    private String userAgent;
    private ParsedUserAgent parsedUserAgent;
    private String traceId;

    private Long costTime;

    /**
     * 登录时间
     */
    private LocalDateTime createdAt;
}

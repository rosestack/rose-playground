<<<<<<< HEAD:rose-backend/rose-core/src/main/java/io/github/rose/core/model/ParsedUserAgent.java
package io.github.rose.core.model;
=======
package io.github.rose.common.model;
>>>>>>> f6bb42d (refactor: 调整基础模型结构，移除租户相关字段，新增地理地址与身份源模型，优化审计基类实现):rose-backend/rose-common/src/main/java/io/github/rose/common/model/ParsedUserAgent.java

public class ParsedUserAgent {
    /**
     * 使用的设备类型
     */
    private String device;
    /**
     * 浏览器名称
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
}

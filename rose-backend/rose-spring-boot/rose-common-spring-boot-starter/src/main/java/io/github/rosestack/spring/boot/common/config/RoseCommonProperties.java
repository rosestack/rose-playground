package io.github.rosestack.spring.boot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rose.common")
public class RoseCommonProperties {
    /**
     * 字段加密配置
     */
    private Encryption encryption = new Encryption();

    /**
     * 字段加密配置
     */
    @Data
    public static class Encryption {
        private boolean enabled = true;

        /**
         * 加密密钥（生产环境应该从外部配置或密钥管理系统获取）
         */
        private String secretKey = "0123456789abcdeffedcba9876543210";

        /**
         * 加密失败时是否抛出异常
         */
        private boolean failOnError = true;

        /**
         * 密钥轮换配置
         */
        private KeyRotation keyRotation = new KeyRotation();

        /**
         * 哈希配置
         */
        private Hash hash = new Hash();

        /**
         * 哈希配置
         */
        @Data
        public static class Hash {
            /**
             * 是否启用哈希功能
             */
            private boolean enabled = true;

            /**
             * 全局盐值（生产环境应该从外部配置获取）
             */
            private String globalSalt = "rose-mybatis-global-salt-2024";

            /**
             * 默认哈希算法
             */
            private String algorithm = "HMAC_SHA256";

            /**
             * HMAC 密钥（生产环境应该从外部配置获取）
             */
            private String hmacKey = "rose-mybatis-hmac-key-2024";
        }

        /**
         * 密钥轮换配置
         */
        @Data
        public static class KeyRotation {
            /**
             * 是否启用密钥轮换
             */
            private boolean enabled = false;

            /**
             * 自动轮换间隔（天）
             */
            private int autoRotationDays = 90;

            /**
             * 密钥保留期（天）- 旧密钥保留多久用于解密
             */
            private int keyRetentionDays = 30;

            /**
             * 是否启用自动清理过期密钥
             */
            private boolean autoCleanup = true;

            /**
             * 默认密钥长度配置
             */
            private KeyLength keyLength = new KeyLength();

            @Data
            public static class KeyLength {
                /**
                 * AES密钥长度（位）
                 */
                private int aes = 256;

                /**
                 * RSA密钥长度（位）
                 */
                private int rsa = 2048;
            }
        }
    }
}

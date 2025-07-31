package io.github.rosestack.audit.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * Rose Audit 配置属性
 * <p>
 * 提供审计日志功能的配置选项，包括存储配置、加密配置、脱敏配置、性能配置等。
 * 支持生产环境的安全配置和外部化配置。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Data
@Validated
@ConfigurationProperties(prefix = "rose.audit")
public class AuditProperties {

    /**
     * 是否启用审计日志功能
     */
    private boolean enabled = true;

    private List<String> maskFields = Arrays.asList();

    /**
     * 存储配置
     */
    @Valid
    @NotNull
    private Storage storage = new Storage();

    /**
     * 数据保留配置
     */
    @Valid
    @NotNull
    private Retention retention = new Retention();

    /**
     * 事件过滤配置
     */
    @Valid
    @NotNull
    private Filter filter = new Filter();

    /**
     * 存储配置
     */
    @Data
    public static class Storage {
        /**
         * 存储类型：database, file, mq
         */
        @NotBlank(message = "存储类型不能为空")
        private String type = "database";

        /**
         * 批量处理大小
         */
        @Min(value = 1, message = "批量处理大小不能小于1")
        @Max(value = 10000, message = "批量处理大小不能大于10000")
        private int batchSize = 100;

        /**
         * 批量处理间隔（毫秒）
         */
        @Min(value = 100, message = "批量处理间隔不能小于100毫秒")
        @Max(value = 300000, message = "批量处理间隔不能大于300秒")
        private long batchInterval = 5000;

        /**
         * 数据库配置
         */
        @Valid
        private Database database = new Database();

        /**
         * 文件存储配置
         */
        @Valid
        private File file = new File();

        /**
         * 消息队列配置
         */
        @Valid
        private MessageQueue messageQueue = new MessageQueue();

        /**
         * 数据库存储配置
         */
        @Data
        public static class Database {
            /**
             * 是否启用分区表
             */
            private boolean partitionEnabled = true;

            /**
             * 分区类型：MONTH, QUARTER, YEAR
             */
            private String partitionType = "MONTH";

            /**
             * 是否启用表压缩
             */
            private boolean compressionEnabled = true;
        }

        /**
         * 文件存储配置
         */
        @Data
        public static class File {
            /**
             * 文件存储路径
             */
            private String path = "/var/log/audit";

            /**
             * 文件滚动策略：SIZE, TIME
             */
            private String rolloverStrategy = "TIME";

            /**
             * 最大文件大小（MB）
             */
            @Min(value = 1, message = "最大文件大小不能小于1MB")
            private int maxFileSize = 100;

            /**
             * 文件保留天数
             */
            @Min(value = 1, message = "文件保留天数不能小于1天")
            private int retentionDays = 30;
        }

        /**
         * 消息队列配置
         */
        @Data
        public static class MessageQueue {
            /**
             * 队列类型：KAFKA, RABBITMQ, ROCKETMQ
             */
            private String type = "KAFKA";

            /**
             * 主题名称
             */
            private String topic = "audit-log";

            /**
             * 是否启用事务
             */
            private boolean transactionEnabled = false;
        }
    }

    /**
     * 数据保留配置
     */
    @Data
    public static class Retention {
        /**
         * 数据保留天数
         */
        @Min(value = 1, message = "数据保留天数不能小于1天")
        @Max(value = 3650, message = "数据保留天数不能大于10年")
        private int days = 365;

        /**
         * 是否启用自动清理
         */
        private boolean autoCleanup = true;

        /**
         * 清理任务执行时间（cron表达式）
         */
        @NotBlank(message = "清理任务执行时间不能为空")
        private String cleanupCron = "0 0 2 * * ?";

        /**
         * 归档配置
         */
        @Valid
        private Archive archive = new Archive();

        /**
         * 归档配置
         */
        @Data
        public static class Archive {
            /**
             * 是否启用归档
             */
            private boolean enabled = false;

            /**
             * 归档阈值（天）
             */
            @Min(value = 30, message = "归档阈值不能小于30天")
            private int thresholdDays = 90;

            /**
             * 归档存储路径
             */
            private String storagePath = "/var/archive/audit";

            /**
             * 归档压缩格式：ZIP, GZIP, TAR
             */
            private String compressionFormat = "GZIP";
        }
    }

    /**
     * 事件过滤配置
     */
    @Data
    public static class Filter {
        /**
         * 忽略的事件类型
         */
        private List<String> ignoreEventTypes = new ArrayList<>();

        /**
         * 忽略的用户
         */
        private List<String> ignoreUsers = new ArrayList<String>() {{
            add("system");
            add("admin");
        }};

        /**
         * 忽略的IP地址
         */
        private List<String> ignoreIps = new ArrayList<String>() {{
            add("127.0.0.1");
            add("::1");
        }};

        /**
         * 忽略的URI模式（支持通配符）
         */
        private List<String> ignoreUriPatterns = new ArrayList<String>() {{
            add("/health/**");
            add("/actuator/**");
            add("/favicon.ico");
        }};

        /**
         * 最小风险等级（低于此等级的事件将被忽略）
         */
        private String minRiskLevel = "LOW";

        /**
         * 采样率配置
         */
        @Valid
        private Sampling sampling = new Sampling();

        /**
         * 采样率配置
         */
        @Data
        public static class Sampling {
            /**
             * 是否启用采样
             */
            private boolean enabled = false;

            /**
             * 采样率（0.0-1.0）
             */
            @Min(value = 0, message = "采样率不能小于0")
            @Max(value = 1, message = "采样率不能大于1")
            private double rate = 1.0;

            /**
             * 按事件类型的采样率
             */
            private Map<String, Double> eventTypeSamplingRates = new HashMap<>();
        }
    }
}
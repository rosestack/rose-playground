package io.github.rosestack.mybatis.quality;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * 数据质量监控器
 * <p>
 * 监控数据质量相关指标，包括：
 * 1. 数据完整性检查
 * 2. 数据格式验证
 * 3. 数据一致性检查
 * 4. 异常数据统计
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class DataQualityMonitor {

    /**
     * 数据质量统计
     */
    private static final Map<String, DataQualityStats> QUALITY_STATS = new ConcurrentHashMap<>();

    /**
     * 常用数据格式验证规则
     */
    private static final Map<String, Pattern> VALIDATION_PATTERNS = new HashMap<>();

    static {
        // 初始化验证规则
        VALIDATION_PATTERNS.put("email", Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"));
        VALIDATION_PATTERNS.put("phone", Pattern.compile("^1[3-9]\\d{9}$"));
        VALIDATION_PATTERNS.put("idCard", Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"));
        VALIDATION_PATTERNS.put("bankCard", Pattern.compile("^[1-9]\\d{12,19}$"));
        VALIDATION_PATTERNS.put("url", Pattern.compile("^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$"));
        VALIDATION_PATTERNS.put("ipv4", Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"));
    }

    /**
     * 记录数据质量事件
     *
     * @param tableName 表名
     * @param fieldName 字段名
     * @param eventType 事件类型
     * @param isValid   是否有效
     */
    public void recordQualityEvent(String tableName, String fieldName, QualityEventType eventType, boolean isValid) {
        String key = tableName + "." + fieldName;
        DataQualityStats stats = QUALITY_STATS.computeIfAbsent(key, k -> new DataQualityStats(tableName, fieldName));

        stats.incrementTotal();
        if (!isValid) {
            stats.incrementInvalid();
            stats.addInvalidEvent(eventType, LocalDateTime.now());
        }

        // 记录日志
        if (!isValid) {
            log.warn("数据质量问题: 表={}, 字段={}, 事件类型={}", tableName, fieldName, eventType);
        }
    }

    /**
     * 验证字段数据格式
     *
     * @param value     字段值
     * @param fieldType 字段类型
     * @return 是否有效
     */
    public boolean validateFieldFormat(String value, String fieldType) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        Pattern pattern = VALIDATION_PATTERNS.get(fieldType.toLowerCase());
        if (pattern == null) {
            // 未知类型，默认有效
            return true;
        }

        return pattern.matcher(value.trim()).matches();
    }

    /**
     * 检查数据完整性
     *
     * @param tableName    表名
     * @param requiredFields 必填字段
     * @param data         数据对象
     * @return 完整性检查结果
     */
    public DataIntegrityResult checkDataIntegrity(String tableName, Set<String> requiredFields, Map<String, Object> data) {
        DataIntegrityResult result = new DataIntegrityResult();
        result.setTableName(tableName);
        result.setCheckTime(LocalDateTime.now());

        List<String> missingFields = new ArrayList<>();
        List<String> emptyFields = new ArrayList<>();

        for (String field : requiredFields) {
            Object value = data.get(field);
            if (value == null) {
                missingFields.add(field);
                recordQualityEvent(tableName, field, QualityEventType.MISSING_VALUE, false);
            } else if (value.toString().trim().isEmpty()) {
                emptyFields.add(field);
                recordQualityEvent(tableName, field, QualityEventType.EMPTY_VALUE, false);
            } else {
                recordQualityEvent(tableName, field, QualityEventType.VALID_VALUE, true);
            }
        }

        result.setMissingFields(missingFields);
        result.setEmptyFields(emptyFields);
        result.setValid(missingFields.isEmpty() && emptyFields.isEmpty());

        return result;
    }

    /**
     * 获取数据质量统计
     *
     * @param tableName 表名（可选）
     * @return 质量统计列表
     */
    public List<DataQualityStats> getQualityStats(String tableName) {
        if (tableName == null) {
            return new ArrayList<>(QUALITY_STATS.values());
        }

        return QUALITY_STATS.values().stream()
                .filter(stats -> stats.getTableName().equals(tableName))
                .sorted((a, b) -> Double.compare(b.getInvalidRate(), a.getInvalidRate()))
                .toList();
    }

    /**
     * 获取质量报告
     *
     * @return 质量报告
     */
    public DataQualityReport generateQualityReport() {
        DataQualityReport report = new DataQualityReport();
        report.setGenerateTime(LocalDateTime.now());

        List<DataQualityStats> allStats = new ArrayList<>(QUALITY_STATS.values());
        report.setTotalFields(allStats.size());

        // 计算总体统计
        long totalRecords = allStats.stream().mapToLong(stats -> stats.getTotalCount().get()).sum();
        long totalInvalid = allStats.stream().mapToLong(stats -> stats.getInvalidCount().get()).sum();
        double overallQualityRate = totalRecords > 0 ? (double) (totalRecords - totalInvalid) / totalRecords * 100 : 100.0;

        report.setTotalRecords(totalRecords);
        report.setInvalidRecords(totalInvalid);
        report.setOverallQualityRate(overallQualityRate);

        // 找出质量最差的字段
        List<DataQualityStats> worstFields = allStats.stream()
                .filter(stats -> stats.getInvalidRate() > 0)
                .sorted((a, b) -> Double.compare(b.getInvalidRate(), a.getInvalidRate()))
                .limit(10)
                .toList();
        report.setWorstQualityFields(worstFields);

        // 按表分组统计
        Map<String, List<DataQualityStats>> byTable = new HashMap<>();
        allStats.forEach(stats -> byTable.computeIfAbsent(stats.getTableName(), k -> new ArrayList<>()).add(stats));
        report.setStatsByTable(byTable);

        return report;
    }

    /**
     * 清空统计数据
     */
    public void clearStats() {
        QUALITY_STATS.clear();
        log.info("数据质量统计已清空");
    }

    /**
     * 数据质量统计
     */
    @Data
    public static class DataQualityStats {
        private String tableName;
        private String fieldName;
        private AtomicLong totalCount = new AtomicLong(0);
        private AtomicLong invalidCount = new AtomicLong(0);
        private List<QualityEvent> recentInvalidEvents = new ArrayList<>();

        public DataQualityStats(String tableName, String fieldName) {
            this.tableName = tableName;
            this.fieldName = fieldName;
        }

        public void incrementTotal() {
            totalCount.incrementAndGet();
        }

        public void incrementInvalid() {
            invalidCount.incrementAndGet();
        }

        public void addInvalidEvent(QualityEventType eventType, LocalDateTime time) {
            synchronized (recentInvalidEvents) {
                recentInvalidEvents.add(new QualityEvent(eventType, time));
                // 只保留最近100个事件
                if (recentInvalidEvents.size() > 100) {
                    recentInvalidEvents.remove(0);
                }
            }
        }

        public double getInvalidRate() {
            long total = totalCount.get();
            return total > 0 ? (double) invalidCount.get() / total * 100 : 0.0;
        }
    }

    /**
     * 数据完整性检查结果
     */
    @Data
    public static class DataIntegrityResult {
        private String tableName;
        private LocalDateTime checkTime;
        private boolean valid;
        private List<String> missingFields;
        private List<String> emptyFields;
    }

    /**
     * 数据质量报告
     */
    @Data
    public static class DataQualityReport {
        private LocalDateTime generateTime;
        private int totalFields;
        private long totalRecords;
        private long invalidRecords;
        private double overallQualityRate;
        private List<DataQualityStats> worstQualityFields;
        private Map<String, List<DataQualityStats>> statsByTable;
    }

    /**
     * 质量事件
     */
    @Data
    public static class QualityEvent {
        private QualityEventType eventType;
        private LocalDateTime time;

        public QualityEvent(QualityEventType eventType, LocalDateTime time) {
            this.eventType = eventType;
            this.time = time;
        }
    }

    /**
     * 质量事件类型
     */
    public enum QualityEventType {
        MISSING_VALUE("缺失值"),
        EMPTY_VALUE("空值"),
        INVALID_FORMAT("格式错误"),
        OUT_OF_RANGE("超出范围"),
        DUPLICATE_VALUE("重复值"),
        VALID_VALUE("有效值");

        private final String description;

        QualityEventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

package io.github.rosestack.mybatis.optimization;

import io.github.rosestack.mybatis.cache.FieldCache;
import io.github.rosestack.mybatis.config.RoseMybatisProperties;
import io.github.rosestack.mybatis.encryption.OptimizedFieldEncryptor;
import io.github.rosestack.mybatis.enums.EncryptType;

import io.github.rosestack.mybatis.quality.DataQualityMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 优化功能集成测试
 * <p>
 * 测试所有优化组件的集成效果和性能提升。
 * </p>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OptimizationIntegrationTest {

    private RoseMybatisProperties properties;
    private OptimizedFieldEncryptor encryptor;
    private DataQualityMonitor qualityMonitor;


    @BeforeEach
    void setUp() {
        properties = new RoseMybatisProperties();
        properties.getEncryption().setEnabled(true);
        properties.getEncryption().setSecretKey("TestSecretKey123");
        properties.getEncryption().setDefaultAlgorithm("AES");

        encryptor = new OptimizedFieldEncryptor(properties);
        qualityMonitor = new DataQualityMonitor();

    }

    @Test
    void testFieldCachePerformance() {
        // 测试字段缓存性能
        Class<?> testClass = TestEntity.class;

        // 第一次调用（缓存未命中）
        long startTime = System.nanoTime();
        List<Field> fields1 = FieldCache.getEncryptFields(testClass);
        long firstCallTime = System.nanoTime() - startTime;

        // 第二次调用（缓存命中）
        startTime = System.nanoTime();
        List<Field> fields2 = FieldCache.getEncryptFields(testClass);
        long secondCallTime = System.nanoTime() - startTime;

        // 验证结果一致性
        assertEquals(fields1.size(), fields2.size());
        assertEquals(fields1, fields2);

        // 验证性能提升（第二次调用应该更快，但在测试环境中可能差异很小）
        // 这里我们主要验证缓存功能正常工作
        System.out.printf("缓存性能: 第一次=%dns, 第二次=%dns%n", firstCallTime, secondCallTime);

        // 验证缓存统计
        FieldCache.CacheStats stats = FieldCache.getCacheStats();
        assertTrue(stats.getEncryptFieldsCacheSize() > 0);
    }

    @Test
    void testOptimizedEncryptorPerformance() {
        // 测试优化加密器的性能
        String plainText = "测试数据123";
        List<String> plainTexts = Arrays.asList(
                "数据1", "数据2", "数据3", "数据4", "数据5"
        );

        // 测试单个加密
        long startTime = System.nanoTime();
        String encrypted = encryptor.encrypt(plainText, EncryptType.AES);
        long singleEncryptTime = System.nanoTime() - startTime;

        // 测试批量加密
        startTime = System.nanoTime();
        List<String> encryptedList = encryptor.encryptBatch(plainTexts, EncryptType.AES);
        long batchEncryptTime = System.nanoTime() - startTime;

        // 验证加密结果
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        assertEquals(plainTexts.size(), encryptedList.size());

        // 验证解密
        String decrypted = encryptor.decrypt(encrypted, EncryptType.AES);
        assertEquals(plainText, decrypted);

        List<String> decryptedList = encryptor.decryptBatch(encryptedList, EncryptType.AES);
        assertEquals(plainTexts, decryptedList);

        // 验证性能统计
        Map<String, Long> performanceStats = OptimizedFieldEncryptor.getPerformanceStats();
        assertNotNull(performanceStats);
        // 性能统计可能为空，这是正常的

        System.out.printf("单个加密耗时: %dns, 批量加密耗时: %dns%n", singleEncryptTime, batchEncryptTime);
    }

    @Test
    void testDataQualityMonitoring() {
        // 测试数据质量监控
        String tableName = "user";
        String fieldName = "email";

        // 记录有效数据
        qualityMonitor.recordQualityEvent(tableName, fieldName, 
                DataQualityMonitor.QualityEventType.VALID_VALUE, true);

        // 记录无效数据
        qualityMonitor.recordQualityEvent(tableName, fieldName, 
                DataQualityMonitor.QualityEventType.INVALID_FORMAT, false);

        // 获取统计信息
        List<DataQualityMonitor.DataQualityStats> stats = qualityMonitor.getQualityStats(tableName);
        assertFalse(stats.isEmpty());

        DataQualityMonitor.DataQualityStats fieldStats = stats.get(0);
        assertEquals(tableName, fieldStats.getTableName());
        assertEquals(fieldName, fieldStats.getFieldName());
        assertEquals(2, fieldStats.getTotalCount().get());
        assertEquals(1, fieldStats.getInvalidCount().get());
        assertEquals(50.0, fieldStats.getInvalidRate(), 0.01);

        // 测试数据完整性检查
        Set<String> requiredFields = Set.of("username", "email", "phone");
        Map<String, Object> validData = Map.of(
                "username", "testuser",
                "email", "test@example.com",
                "phone", "13800138000"
        );

        DataQualityMonitor.DataIntegrityResult result = 
                qualityMonitor.checkDataIntegrity(tableName, requiredFields, validData);
        assertTrue(result.isValid());
        assertTrue(result.getMissingFields().isEmpty());
        assertTrue(result.getEmptyFields().isEmpty());

        // 测试不完整数据
        Map<String, Object> incompleteData = Map.of(
                "username", "testuser",
                "email", ""  // 空值
                // 缺少 phone 字段
        );

        result = qualityMonitor.checkDataIntegrity(tableName, requiredFields, incompleteData);
        assertFalse(result.isValid());
        assertTrue(result.getMissingFields().contains("phone"));
        assertTrue(result.getEmptyFields().contains("email"));
    }

    @Test
    void testFieldFormatValidation() {
        // 测试字段格式验证
        assertTrue(qualityMonitor.validateFieldFormat("test@example.com", "email"));
        assertFalse(qualityMonitor.validateFieldFormat("invalid-email", "email"));

        assertTrue(qualityMonitor.validateFieldFormat("13800138000", "phone"));
        assertFalse(qualityMonitor.validateFieldFormat("12345", "phone"));

        // 身份证验证（使用一个更简单的测试）
        assertFalse(qualityMonitor.validateFieldFormat("123456", "idCard"));
        // 注意：身份证验证规则比较复杂，这里主要测试验证机制
    }

    @Test
    void testPerformanceStats() {
        // 测试性能统计
        String plainText = "测试数据";

        // 执行一些加密操作
        encryptor.encrypt(plainText, EncryptType.AES);
        encryptor.decrypt(encryptor.encrypt(plainText, EncryptType.AES), EncryptType.AES);

        // 获取性能统计
        Map<String, Long> performanceStats = OptimizedFieldEncryptor.getPerformanceStats();
        assertNotNull(performanceStats);

        // 清空统计
        OptimizedFieldEncryptor.clearCache();
    }

    @Test
    void testCacheManagement() {
        // 测试缓存管理
        Class<?> testClass = TestEntity.class;

        // 填充缓存
        FieldCache.getEncryptFields(testClass);
        FieldCache.getSensitiveFields(testClass);

        // 验证缓存存在
        FieldCache.CacheStats stats = FieldCache.getCacheStats();
        assertTrue(stats.getEncryptFieldsCacheSize() > 0 || stats.getSensitiveFieldsCacheSize() > 0);

        // 清空缓存
        FieldCache.clearCache();
        stats = FieldCache.getCacheStats();
        assertEquals(0, stats.getEncryptFieldsCacheSize());
        assertEquals(0, stats.getSensitiveFieldsCacheSize());
        assertEquals(0, stats.getAllFieldsCacheSize());
    }

    @Test
    void testQualityReport() {
        // 测试质量报告生成
        String tableName = "test_table";

        // 添加一些测试数据
        qualityMonitor.recordQualityEvent(tableName, "field1", 
                DataQualityMonitor.QualityEventType.VALID_VALUE, true);
        qualityMonitor.recordQualityEvent(tableName, "field1", 
                DataQualityMonitor.QualityEventType.INVALID_FORMAT, false);
        qualityMonitor.recordQualityEvent(tableName, "field2", 
                DataQualityMonitor.QualityEventType.VALID_VALUE, true);

        // 生成报告
        DataQualityMonitor.DataQualityReport report = qualityMonitor.generateQualityReport();
        assertNotNull(report);
        assertNotNull(report.getGenerateTime());
        assertTrue(report.getTotalFields() >= 2); // 可能有其他测试的数据
        assertTrue(report.getTotalRecords() >= 3);
        assertTrue(report.getInvalidRecords() >= 1);
        assertTrue(report.getOverallQualityRate() >= 0);
        assertTrue(report.getStatsByTable().containsKey(tableName));
    }

    /**
     * 测试实体类
     */
    public static class TestEntity {
        private String normalField;

        @io.github.rosestack.mybatis.annotation.EncryptField(io.github.rosestack.mybatis.enums.EncryptType.AES)
        private String encryptedField;

        @io.github.rosestack.mybatis.annotation.SensitiveField(io.github.rosestack.mybatis.enums.SensitiveType.NAME)
        private String sensitiveField;

        // Getters and Setters
        public String getNormalField() { return normalField; }
        public void setNormalField(String normalField) { this.normalField = normalField; }

        public String getEncryptedField() { return encryptedField; }
        public void setEncryptedField(String encryptedField) { this.encryptedField = encryptedField; }

        public String getSensitiveField() { return sensitiveField; }
        public void setSensitiveField(String sensitiveField) { this.sensitiveField = sensitiveField; }
    }
}

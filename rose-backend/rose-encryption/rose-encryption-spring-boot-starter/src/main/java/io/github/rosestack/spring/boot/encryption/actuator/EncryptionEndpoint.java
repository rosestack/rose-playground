package io.github.rosestack.spring.boot.encryption.actuator;

import io.github.rosestack.core.util.MaskUtils;
import io.github.rosestack.encryption.EncryptionUtils;
import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import io.github.rosestack.spring.boot.encryption.config.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加密服务监控端点
 *
 * <p>提供加密服务的监控和管理功能：
 * <ul>
 *   <li>GET /actuator/encryption - 获取完整的监控报告</li>
 *   <li>POST /actuator/encryption/clear-cache - 清空缓存和统计</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Component
@Endpoint(id = "encryption")
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.springframework.boot.actuator.endpoint.annotation.Endpoint")
public class EncryptionEndpoint {

	private final EncryptionProperties properties;

	/**
	 * 获取加密服务的完整监控报告
	 *
	 * @return 监控报告
	 */
	@ReadOperation
	public Map<String, Object> encryption() {
		Map<String, Object> result = new LinkedHashMap<>();

		// 基础配置信息
		result.put("configuration", getConfigurationInfo());

		// 监控报告
		if (properties.isEnabled()) {
			try {
				Map<String, Object> monitoringReport = EncryptionUtils.getMonitoringReport();
				result.put("monitoring", monitoringReport);
			} catch (Exception e) {
				result.put("monitoringError", e.getMessage());
			}
		} else {
			result.put("monitoring", "服务未启用");
		}

		return result;
	}

	/**
	 * 清空缓存和统计数据
	 *
	 * @return 操作结果
	 */
	@WriteOperation
	public Map<String, Object> clearCache() {
		Map<String, Object> result = new LinkedHashMap<>();

		if (!properties.isEnabled()) {
			result.put("success", false);
			result.put("message", "服务未启用");
			return result;
		}

		try {
			// 获取清空前的统计
			Map<String, Long> beforeStats = EncryptionMonitorManager.getInstance().getPerformanceStats();
			long totalOperationsBefore = beforeStats.values().stream()
				.mapToLong(Long::longValue)
				.sum();

			// 清空缓存
			EncryptionMonitorManager.getInstance().clearCache();

			result.put("success", true);
			result.put("message", "缓存和统计数据已清空");
			result.put("clearedOperations", totalOperationsBefore);
			result.put("timestamp", System.currentTimeMillis());

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "清空缓存失败: " + e.getMessage());
		}

		return result;
	}

	/**
	 * 获取配置信息
	 */
	private Map<String, Object> getConfigurationInfo() {
		Map<String, Object> config = new LinkedHashMap<>();

		config.put("enabled", properties.isEnabled());
		config.put("failOnError", properties.isFailOnError());

		if (properties.isEnabled()) {
			String secretKey = properties.getSecretKey();
			if (secretKey != null) {
				config.put("secretKeyConfigured", true);
				config.put("secretKeyLength", secretKey.length());
				// 出于安全考虑，不显示实际密钥
				config.put("secretKeyMasked", MaskUtils.maskSecretKey(secretKey));
			} else {
				config.put("secretKeyConfigured", false);
			}
		}

		return config;
	}
}

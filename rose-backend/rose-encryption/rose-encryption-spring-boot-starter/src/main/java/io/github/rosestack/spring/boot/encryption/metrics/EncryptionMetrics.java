package io.github.rosestack.spring.boot.encryption.metrics;

import io.github.rosestack.encryption.EncryptionUtils;
import io.github.rosestack.encryption.monitor.EncryptionMonitorManager;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 加密服务指标收集器
 *
 * <p>将加密服务的统计信息暴露为 Micrometer 指标，支持：
 * <ul>
 *   <li>操作计数器（成功/失败）</li>
 *   <li>算法使用统计</li>
 *   <li>错误统计</li>
 *   <li>数据大小分布</li>
 *   <li>成功率指标</li>
 * </ul>
 *
 * @author Rose Team
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnClass({MeterRegistry.class, Gauge.class})
public class EncryptionMetrics implements MeterRegistryCustomizer<MeterRegistry> {

	private MeterRegistry meterRegistry;

	@Override
	public void customize(MeterRegistry registry) {
		this.meterRegistry = registry;
	}

	@PostConstruct
	public void initMetrics() {
		if (meterRegistry == null) {
			log.warn("MeterRegistry 未配置，跳过加密指标注册");
			return;
		}

		registerPerformanceMetrics();
		registerAlgorithmUsageMetrics();
		registerErrorMetrics();
		registerDataSizeMetrics();
		registerSuccessRateMetrics();

		log.info("加密服务指标已注册到 MeterRegistry");
	}

	/**
	 * 注册性能指标
	 */
	private void registerPerformanceMetrics() {
		// 总操作数
		Gauge.builder("encryption.operations.total", this, metrics -> {
				try {
					Map<String, Long> stats = EncryptionMonitorManager.getInstance().getPerformanceStats();
					return stats.values().stream()
						.mapToLong(Long::longValue)
						.sum();
				} catch (Exception e) {
					log.debug("获取总操作数失败", e);
					return 0;
				}
			})
			.description("加密服务总操作数")
			.register(meterRegistry);

		// 成功操作数
		Gauge.builder("encryption.operations.success", this, metrics -> {
				try {
					Map<String, Long> stats = EncryptionMonitorManager.getInstance().getPerformanceStats();
					return stats.entrySet().stream()
						.filter(entry -> entry.getKey().endsWith("_success"))
						.mapToLong(Map.Entry::getValue)
						.sum();
				} catch (Exception e) {
					log.debug("获取成功操作数失败", e);
					return 0;
				}
			})
			.description("加密服务成功操作数")
			.register(meterRegistry);

		// 失败操作数
		Gauge.builder("encryption.operations.failure", this, metrics -> {
				try {
					Map<String, Long> stats = EncryptionMonitorManager.getInstance().getPerformanceStats();
					return stats.entrySet().stream()
						.filter(entry -> entry.getKey().endsWith("_failure"))
						.mapToLong(Map.Entry::getValue)
						.sum();
				} catch (Exception e) {
					log.debug("获取失败操作数失败", e);
					return 0;
				}
			})
			.description("加密服务失败操作数")
			.register(meterRegistry);
	}

	/**
	 * 注册算法使用指标
	 */
	private void registerAlgorithmUsageMetrics() {
		// 为每种算法注册使用计数指标
		String[] algorithms = {"AES", "DES", "DES3", "SM2", "SM4", "RSA"};

		for (String algorithm : algorithms) {
			Gauge.builder("encryption.algorithm.usage", algorithm, alg -> {
					try {
						Map<String, Long> stats = EncryptionMonitorManager.getInstance().getAlgorithmUsageStats();
						return stats.getOrDefault(alg, 0L).doubleValue();
					} catch (Exception e) {
						log.debug("获取算法使用统计失败: {}", alg, e);
						return 0;
					}
				})
				.description("算法使用次数")
				.tags(Tags.of("algorithm", algorithm))
				.register(meterRegistry);
		}
	}

	/**
	 * 注册错误指标
	 */
	private void registerErrorMetrics() {
		// 总错误数
		Gauge.builder("encryption.errors.total", this, metrics -> {
				try {
					Map<String, Long> errors = EncryptionMonitorManager.getInstance().getErrorStats();
					return errors.getOrDefault("encrypt_total_errors", 0L).doubleValue() +
						errors.getOrDefault("decrypt_total_errors", 0L).doubleValue();
				} catch (Exception e) {
					log.debug("获取总错误数失败", e);
					return 0;
				}
			})
			.description("加密服务总错误数")
			.register(meterRegistry);

		// 加密错误数
		Gauge.builder("encryption.errors.encrypt", this, metrics -> {
				try {
					Map<String, Long> errors = EncryptionMonitorManager.getInstance().getErrorStats();
					return errors.getOrDefault("encrypt_total_errors", 0L).doubleValue();
				} catch (Exception e) {
					log.debug("获取加密错误数失败", e);
					return 0;
				}
			})
			.description("加密错误数")
			.register(meterRegistry);

		// 解密错误数
		Gauge.builder("encryption.errors.decrypt", this, metrics -> {
				try {
					Map<String, Long> errors = EncryptionMonitorManager.getInstance().getErrorStats();
					return errors.getOrDefault("decrypt_total_errors", 0L).doubleValue();
				} catch (Exception e) {
					log.debug("获取解密错误数失败", e);
					return 0;
				}
			})
			.description("解密错误数")
			.register(meterRegistry);
	}

	/**
	 * 注册数据大小分布指标
	 */
	private void registerDataSizeMetrics() {
		String[] sizeCategories = {"small", "medium", "large", "xlarge"};
		String[] operations = {"encrypt", "decrypt"};

		for (String operation : operations) {
			for (String category : sizeCategories) {
				String key = operation + "_" + category;
				Gauge.builder("encryption.data.size.distribution", key, k -> {
						try {
							Map<String, Long> stats = EncryptionMonitorManager.getInstance().getDataSizeStats();
							return stats.getOrDefault(k, 0L).doubleValue();
						} catch (Exception e) {
							log.debug("获取数据大小分布失败: {}", k, e);
							return 0;
						}
					})
					.description("数据大小分布")
					.tags(Tags.of("operation", operation, "size", category))
					.register(meterRegistry);
			}
		}
	}

	/**
	 * 注册成功率指标
	 */
	private void registerSuccessRateMetrics() {
		// 总体成功率
		Gauge.builder("encryption.success.rate.overall", this, metrics -> {
				try {
					Map<String, Object> report = EncryptionMonitorManager.getInstance().getMonitoringReport();
					@SuppressWarnings("unchecked")
					Map<String, Double> successRates = (Map<String, Double>) report.get("successRates");

					if (successRates == null || successRates.isEmpty()) {
						return 100.0; // 没有操作时认为是100%
					}

					return successRates.values().stream()
						.mapToDouble(Double::doubleValue)
						.average()
						.orElse(100.0);
				} catch (Exception e) {
					log.debug("获取总体成功率失败", e);
					return 100.0;
				}
			})
			.description("总体成功率")
			.register(meterRegistry);

		// AES 成功率
		Gauge.builder("encryption.success.rate.aes", this, metrics -> getAlgorithmSuccessRate("AES"))
			.description("AES 算法成功率")
			.register(meterRegistry);

		// DES 成功率
		Gauge.builder("encryption.success.rate.des", this, metrics -> getAlgorithmSuccessRate("DES"))
			.description("DES 算法成功率")
			.register(meterRegistry);
	}

	/**
	 * 获取特定算法的成功率
	 */
	private double getAlgorithmSuccessRate(String algorithm) {
		try {
			Map<String, Object> report = EncryptionMonitorManager.getInstance().getMonitoringReport();
			@SuppressWarnings("unchecked")
			Map<String, Double> successRates = (Map<String, Double>) report.get("successRates");

			if (successRates == null) {
				return 100.0;
			}

			// 计算该算法的平均成功率
			double totalRate = 0.0;
			int count = 0;

			for (Map.Entry<String, Double> entry : successRates.entrySet()) {
				if (entry.getKey().contains(algorithm)) {
					totalRate += entry.getValue();
					count++;
				}
			}

			return count > 0 ? totalRate / count : 100.0;
		} catch (Exception e) {
			log.debug("获取{}算法成功率失败", algorithm, e);
			return 100.0;
		}
	}
}

package io.github.rosestack.notice.sender.sms;

import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.spi.Configurable;

/**
 * 短信服务商 SPI 扩展点，实现类需在 META-INF/services 配置 SPI 文件。 getProviderName() 用于唯一标识服务商（如 tencent/aliyun）。
 */

/**
 * 短信服务商 SPI 扩展点。
 *
 * <ul>
 *   <li>type = "tencent"、"aliyun" 等
 *   <li>channelConfig 需包含各服务商所需参数
 * </ul>
 */
public interface SmsProvider extends Configurable {
	/**
	 * 常用服务商类型 key
	 */
	String TENCENT = "tencent";

	String ALIYUN = "aliyun";

	/**
	 * 服务商唯一标识（如 "tencent"、"aliyun"）
	 */
	String getProviderType();

	/**
	 * 发送短信，成功返回 requestId，失败抛出 NoticeException/NoticeRetryableException
	 */
	String send(SendRequest request) throws Exception;

	default boolean isAnyBlank(String... args) {
		for (String arg : args) {
			if (arg == null || arg.trim().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	void destroy();
}

package io.github.rosestack.i18n;

import io.github.rosestack.i18n.util.I18nUtils;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

/**
 * 消息缓存加载器接口
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */
public interface MessageCacheLoader {

	/**
	 * 从缓存中获取消息
	 *
	 * @param code   消息代码
	 * @param locale 语言环境
	 * @return 缓存的消息，如果不存在则返回 null
	 */
	String getFromCache(String code, Locale locale);

	/**
	 * 将消息放入缓存
	 *
	 * @param code    消息代码
	 * @param locale  语言环境
	 * @param message 消息内容
	 */
	void putToCache(String code, Locale locale, String message);

	/**
	 * 批量从缓存中获取消息
	 *
	 * @param codes  消息代码列表
	 * @param locale 语言环境
	 * @return 缓存的消息映射，key为消息代码，value为消息内容
	 */
	Map<String, String> getFromCache(String[] codes, Locale locale);

	/**
	 * 批量将消息放入缓存
	 *
	 * @param messages 消息映射，key为消息代码，value为消息内容
	 * @param locale   语言环境
	 */
	void putToCache(Map<String, String> messages, Locale locale);

	/**
	 * 清除指定语言环境的缓存
	 *
	 * @param locale 语言环境
	 */
	void evictCache(Locale locale);

	default void evictCache(String resource) {
		if (!StringUtils.hasText(resource)) {
			return;
		}
		evictCache(I18nUtils.resolveLocale(resource));
	}

	/**
	 * 清除所有缓存
	 */
	void clearCache();

	/**
	 * 获取缓存统计信息摘要
	 *
	 * @return 统计信息字符串，如果不支持统计则返回空字符串
	 */
	default String getStatisticsSummary() {
		return "";
	}

	/**
	 * 获取缓存命中率
	 *
	 * @return 命中率（0.0 - 1.0），如果不支持统计则返回 0.0
	 */
	default double getHitRate() {
		return 0.0;
	}
}

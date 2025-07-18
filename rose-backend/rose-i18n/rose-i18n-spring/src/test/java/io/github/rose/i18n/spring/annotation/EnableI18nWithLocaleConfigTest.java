package io.github.rose.i18n.spring.annotation;

import io.github.rose.i18n.AbstractSpringTest;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.spring.TestServiceMessageSourceConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link EnableI18n} 测试 - 验证 defaultLocale 和 supportedLocales 参数
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EnableI18nWithLocaleConfigTest.class,
        TestServiceMessageSourceConfiguration.class
})
@EnableI18n(
        defaultLocale = "en_US",
        supportedLocales = {"en_US", "zh_CN", "ja_JP", "fr_FR"}
)
class EnableI18nWithLocaleConfigTest {

    @Autowired
    private I18nMessageSource i18nMessageSource;

    @Test
    public void testDefaultLocale() {
        // 验证默认 Locale 设置为 en_US
        Locale defaultLocale = i18nMessageSource.getDefaultLocale();
        assertEquals(Locale.US, defaultLocale);
    }

    @Test
    public void testSupportedLocales() {
        // 验证支持的 Locales 包含配置的所有 locale
        Set<Locale> supportedLocales = i18nMessageSource.getSupportedLocales();

        assertNotNull(supportedLocales);
        assertTrue(supportedLocales.contains(Locale.US)); // en_US
        assertTrue(supportedLocales.contains(Locale.CHINA)); // zh_CN
        assertTrue(supportedLocales.contains(Locale.JAPAN)); // ja_JP
        assertTrue(supportedLocales.contains(Locale.FRANCE)); // fr_FR

        // 验证还包含派生的 locales（如 en, zh, ja, fr）
        assertTrue(supportedLocales.contains(Locale.ENGLISH)); // en
        assertTrue(supportedLocales.contains(Locale.CHINESE)); // zh
        assertTrue(supportedLocales.contains(Locale.JAPANESE)); // ja
        assertTrue(supportedLocales.contains(Locale.FRENCH)); // fr
    }

    @Test
    public void testGetMessage() {
        // 测试基本消息获取功能仍然正常工作
        assertNotNull(i18nMessageSource.getMessage("hello", Locale.US, "World"));
    }

    @Test
    public void testLocaleConfiguration() {
        // 验证配置的 locale 数量（包括派生的 locale）
        Set<Locale> supportedLocales = i18nMessageSource.getSupportedLocales();

        // 至少应该包含配置的4个主要 locale 和它们的派生 locale
        assertTrue(supportedLocales.size() >= 4,
                "支持的 locale 数量应该至少为4，实际为: " + supportedLocales.size());

        // 打印所有支持的 locales 用于调试
        System.out.println("Supported Locales: " + supportedLocales);
    }
}

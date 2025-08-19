package io.github.rosestack.i18n.i18n.spring.annotation;

import io.github.rosestack.i18n.AbstractSpringTest;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.i18n.spring.beans.TestI18nMessageSourceConfiguration;
import io.github.rosestack.i18n.spring.annotation.EnableI18n;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static io.github.rosestack.i18n.util.I18nUtils.i18nMessageSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link EnableI18n} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EnableI18nTest.class, TestI18nMessageSourceConfiguration.class})
@EnableI18n
class EnableI18nTest extends AbstractSpringTest {

	@Autowired
	private I18nMessageSource i18nMessageSource;

	@Test
	public void testGetMessage() {
		// Testing Simplified Chinese
		// If the Message Code is "a"
		assertEquals("测试-a", i18nMessageSource.getMessage("a"));

		// The same is true for overloaded methods with Message Pattern arguments
		assertEquals("您好,World", i18nMessageSource.getMessage("hello", "World"));

		// Returns null if code does not exist
		assertNull(i18nMessageSource.getMessage("code-not-found"));

		// Test English, because the English Message resource does not exist
		assertEquals("Hello,World", i18nMessageSource.getMessage("hello", Locale.ENGLISH, "World"));

		// Returns null if code does not exist
		assertNull(i18nMessageSource.getMessage("code-not-found", Locale.US));
	}

	@Test
	public void testCommonServiceMessageSource() {
		assertSame(i18nMessageSource(), i18nMessageSource);
	}
}

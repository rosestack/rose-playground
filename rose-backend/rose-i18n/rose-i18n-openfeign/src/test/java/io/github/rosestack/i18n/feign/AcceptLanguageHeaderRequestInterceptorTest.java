package io.github.rosestack.i18n.feign;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

/**
 * {@link AcceptLanguageHeaderRequestInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AcceptLanguageHeaderRequestInterceptorTest {

	private AcceptLanguageHeaderRequestInterceptor requestInterceptor;

	private RequestTemplate requestTemplate;

	@BeforeEach
	public void before() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(ACCEPT_LANGUAGE, "en");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.requestInterceptor = new AcceptLanguageHeaderRequestInterceptor();
		this.requestTemplate = new RequestTemplate();
	}

	@AfterEach
	public void after() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	public void testApply() {
		assertTrue(requestTemplate.headers().isEmpty());
		requestInterceptor.apply(requestTemplate);
		assertEquals(Arrays.asList("en"), requestTemplate.headers().get(ACCEPT_LANGUAGE));
	}

	@Test
	public void testApplyNoWebMvc() {
		RequestContextHolder.resetRequestAttributes();
		assertTrue(requestTemplate.headers().isEmpty());
		requestInterceptor.apply(new RequestTemplate());
		assertTrue(requestTemplate.headers().isEmpty());
	}
}

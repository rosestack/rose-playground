package io.github.rosestack.i18n.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

/**
 * HTTP Header "Accept-Language" {@link RequestInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AcceptHeaderLocaleResolver
 * @since 1.0.0
 */
public class AcceptLanguageHeaderRequestInterceptor implements RequestInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(AcceptLanguageHeaderRequestInterceptor.class);

	@Override
	public void apply(RequestTemplate template) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (!(requestAttributes instanceof ServletRequestAttributes)) {
			logger.debug(
				"Feign calls in non-Spring WebMVC scenarios, ignoring setting request headers: '{}'",
				ACCEPT_LANGUAGE);
			return;
		}

		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

		HttpServletRequest request = servletRequestAttributes.getRequest();

		String acceptLanguage = request.getHeader(ACCEPT_LANGUAGE);

		if (StringUtils.hasText(acceptLanguage)) {
			template.header(ACCEPT_LANGUAGE, acceptLanguage);
			logger.debug(
				"Feign has set HTTP request header [name : '{}' , value : '{}']", ACCEPT_LANGUAGE, acceptLanguage);
		} else {
			logger.debug(
				"Feign could not set HTTP request header [name : '{}'] because the requester did not pass: '{}'",
				ACCEPT_LANGUAGE,
				acceptLanguage);
		}
	}
}

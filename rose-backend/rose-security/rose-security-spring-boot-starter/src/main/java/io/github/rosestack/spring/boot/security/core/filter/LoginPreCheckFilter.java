package io.github.rosestack.spring.boot.security.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.spring.boot.security.account.LoginLockoutService;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.model.AuthModels;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 在登录认证之前检查账户是否被锁定。
 */
public class LoginPreCheckFilter extends OncePerRequestFilter {

	private final RoseSecurityProperties properties;
	private final LoginLockoutService lockoutService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public LoginPreCheckFilter(
		RoseSecurityProperties properties, ObjectProvider<LoginLockoutService> loginLockoutServiceProvider) {
		this.properties = properties;
		this.lockoutService = loginLockoutServiceProvider.getIfAvailable();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (lockoutService != null
			&& lockoutService.isEnabled()
			&& "POST".equalsIgnoreCase(request.getMethod())
			&& pathEquals(request, properties.getLoginPath())) {
			ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request);
			// 读取 body（缓存后续过滤器可再次读取）
			AuthModels body = null;
			try {
				body = objectMapper.readValue(wrapped.getInputStream(), AuthModels.class);
			} catch (Exception ignored) {
			}
			String username = (body != null) ? body.getUsername() : null;
			if (StringUtils.hasText(username) && lockoutService.isLocked(username)) {
				response.setStatus(401);
				response.setCharacterEncoding(StandardCharsets.UTF_8.name());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.getWriter().write(JsonUtils.toString(ApiResponse.error(40101, "account locked")));
				return;
			}
			filterChain.doFilter(wrapped, response);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean pathEquals(HttpServletRequest req, String path) {
		String servletPath = req.getRequestURI();
		return servletPath != null && servletPath.equals(path);
	}
}

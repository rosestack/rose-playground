package io.github.rosestack.spring.boot.security.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.core.model.ApiResponse;
import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import io.github.rosestack.spring.boot.security.core.model.AuthModels;
import io.github.rosestack.spring.boot.security.core.model.AuthModels.AuthResponse;
import io.github.rosestack.spring.boot.security.core.token.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenService tokenService;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager,
                                     RoseSecurityProperties props,
                                     TokenService tokenService) {
        super(regexPostMatcher(props.getLoginPath()));
        setAuthenticationManager(authenticationManager);
        this.tokenService = tokenService;
    }

    private static RequestMatcher regexPostMatcher(String path) {
        // 简化：将明确的登录路径转为等效正则匹配，仅匹配 POST
        String regex = "^" + java.util.regex.Pattern.quote(path) + "$";
        return new RegexRequestMatcher(regex, "POST");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        AuthModels body = objectMapper.readValue(request.getInputStream(), AuthModels.class);
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword());
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        String token = tokenService.issue(authResult.getName());
        AuthResponse result = new AuthResponse(token, tokenService.getExpiresInSeconds());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(result)));
    }
}



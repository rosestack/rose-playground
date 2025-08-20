package io.github.rosestack.spring.boot.security.core.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.spring.boot.security.config.SecurityProperties;
import io.github.rosestack.spring.boot.security.core.model.AuthModels;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            SecurityProperties props,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler) {
        super(regexPostMatcher(props.getLoginPath()));
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    private static RequestMatcher regexPostMatcher(String path) {
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
}

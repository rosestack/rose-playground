package io.github.rosestack.spring.boot.security.core.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import org.springframework.security.authentication.AuthenticationDetailsSource;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class RoseAuthenticationDetailsSource
        implements AuthenticationDetailsSource<HttpServletRequest, RoseWebAuthenticationDetails> {

    @Override
    public RoseWebAuthenticationDetails buildDetails(HttpServletRequest request) {
        return RoseWebAuthenticationDetails.fromRequest(request);
    }
}

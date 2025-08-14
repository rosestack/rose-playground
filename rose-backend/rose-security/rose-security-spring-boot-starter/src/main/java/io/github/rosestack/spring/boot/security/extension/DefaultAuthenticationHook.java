package io.github.rosestack.spring.boot.security.extension;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认的空实现 AuthenticationHook，允许用户通过自定义 Bean 覆盖
 */
public class DefaultAuthenticationHook implements AuthenticationHook {
}

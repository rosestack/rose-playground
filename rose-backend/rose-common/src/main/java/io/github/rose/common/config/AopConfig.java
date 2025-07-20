package io.github.rose.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Aspect-Oriented Programming (AOP) configuration for enabling AspectJ auto-proxying.
 * <p>
 * This configuration class enables Spring's AspectJ auto-proxy functionality with
 * proxy exposure, allowing aspects to be applied to Spring-managed beans and
 * enabling access to the current proxy object through AopContext.
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li><strong>AspectJ Auto-Proxy:</strong> Automatically creates proxies for beans with aspects</li>
 *   <li><strong>Proxy Exposure:</strong> Exposes the current proxy object for self-invocation scenarios</li>
 *   <li><strong>Auto-Configuration:</strong> Automatically applied when present on classpath</li>
 *   <li><strong>Cross-Cutting Concerns:</strong> Enables implementation of logging, security, transactions, etc.</li>
 * </ul>
 *
 * <h3>Proxy Exposure Benefits:</h3>
 * The {@code exposeProxy = true} configuration enables access to the current proxy object
 * through {@code AopContext.currentProxy()}. This is particularly useful for:
 * <ul>
 *   <li><strong>Self-Invocation:</strong> Calling other methods on the same bean with aspect support</li>
 *   <li><strong>Nested Transactions:</strong> Ensuring transactional behavior in internal method calls</li>
 *   <li><strong>Security Checks:</strong> Maintaining security aspects for internal method invocations</li>
 *   <li><strong>Logging/Monitoring:</strong> Ensuring aspects are applied to all method calls</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * @Service
 * public class MyService {
 *
 *     @Transactional
 *     public void publicMethod() {
 *         // This will NOT trigger @Transactional aspect
 *         // this.internalMethod();
 *
 *         // This WILL trigger @Transactional aspect
 *         ((MyService) AopContext.currentProxy()).internalMethod();
 *     }
 *
 *     @Transactional(propagation = Propagation.REQUIRES_NEW)
 *     public void internalMethod() {
 *         // Method implementation
 *     }
 * }
 * }</pre>
 *
 * <h3>Performance Considerations:</h3>
 * <ul>
 *   <li><strong>Proxy Creation:</strong> Adds slight overhead for proxy creation</li>
 *   <li><strong>Method Invocation:</strong> Minimal overhead for proxied method calls</li>
 *   <li><strong>Memory Usage:</strong> Additional proxy objects consume memory</li>
 *   <li><strong>ThreadLocal:</strong> AopContext uses ThreadLocal for proxy exposure</li>
 * </ul>
 *
 * <h3>Common Use Cases:</h3>
 * <ul>
 *   <li>Transactional method invocations within the same bean</li>
 *   <li>Security aspect enforcement for internal method calls</li>
 *   <li>Logging and monitoring of all method invocations</li>
 *   <li>Caching aspects that need to work with self-invocation</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @see EnableAspectJAutoProxy
 * @see org.springframework.aop.framework.AopContext
 * @see org.springframework.aop.aspectj.annotation.AspectJProxyFactory
 * @since 1.0.0
 */
@AutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true) // Enable proxy exposure for AopContext access
public class AopConfig {
}

package io.github.rose.infrastructure.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * AOP 配置类
 * <p>
 * 启用 AspectJ 自动代理功能，支持切面编程和代理对象暴露。
 * <p>
 * <h3>核心特性：</h3>
 * <ul>
 *   <li>启用 AspectJ 自动代理</li>
 *   <li>暴露代理对象，支持内部方法调用切面</li>
 * </ul>
 * <p>
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Service
 * public class UserService {
 *
 *     @Transactional
 *     public void updateUser(User user) {
 *         // 通过代理调用，确保事务切面生效
 *         ((UserService) AopContext.currentProxy()).saveUserLog(user);
 *     }
 *
 *     @Transactional(propagation = Propagation.REQUIRES_NEW)
 *     public void saveUserLog(User user) {
 *         // 保存用户日志
 *     }
 * }
 * }</pre>
 *
 * @author chensoul
 * @since 1.0.0
 * @see EnableAspectJAutoProxy
 * @see org.springframework.aop.framework.AopContext
 */
@AutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {
}

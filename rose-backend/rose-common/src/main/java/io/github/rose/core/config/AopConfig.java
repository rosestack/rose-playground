package io.github.rose.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true) // 表示通过aop框架暴露该代理对象,AopContext能够访问
public class AopConfig {
}

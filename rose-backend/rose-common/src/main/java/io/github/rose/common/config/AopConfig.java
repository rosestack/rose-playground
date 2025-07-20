package io.github.rose.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@AutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true)
public class AopConfig {
}

package io.github.rose.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncRetryConfig {
    // 可扩展线程池、重试等配置
}

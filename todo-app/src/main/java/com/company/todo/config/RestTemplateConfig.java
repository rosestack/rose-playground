package com.company.todo.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @Bean
    public RestOperations restTemplate(RestTemplateBuilder builder) {
        // 使用 Spring Boot 的 RestTemplateBuilder，可自动注入 Micrometer/Tracing 拦截器，保留超时设置
        RestTemplate rest = builder.build();
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3_000);
        f.setReadTimeout(7_000);
        rest.setRequestFactory(f);
        return rest;
    }
}

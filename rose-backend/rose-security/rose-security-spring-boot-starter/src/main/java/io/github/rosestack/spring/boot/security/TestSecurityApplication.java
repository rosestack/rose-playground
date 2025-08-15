package io.github.rosestack.spring.boot.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Rose Security 测试应用
 *
 * <p>用于测试 Rose Security 功能的独立应用</p>
 */
@SpringBootApplication
public class TestSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSecurityApplication.class, args);
    }
}
package com.example.ddddemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DDD Demo应用启动类
 * <p>
 * 基于DDD分层架构的用户管理系统Demo
 *
 * @author DDD Demo Team
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.example.ddddemo.user.infrastructure.persistence.mapper")
public class DddDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DddDemoApplication.class, args);
    }
}
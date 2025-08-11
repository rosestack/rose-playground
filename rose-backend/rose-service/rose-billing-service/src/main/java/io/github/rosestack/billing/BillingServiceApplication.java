package io.github.rosestack.billing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 计费服务应用启动类
 * <p>
 * 提供计费、订阅、发票、支付等功能的微服务应用。
 *
 * @author chensoul
 * @since 1.0.0
 */
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("io.github.rosestack.billing.repository")
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}

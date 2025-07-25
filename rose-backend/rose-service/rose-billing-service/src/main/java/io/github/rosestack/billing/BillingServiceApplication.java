package io.github.rosestack.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 计费服务应用启动类
 * <p>
 * 提供计费、订阅、发票、支付等功能的微服务应用。
 *
 * @author chensoul
 * @since 1.0.0
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }
}

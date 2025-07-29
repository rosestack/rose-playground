package com.company.usermodulith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User CRUD 应用主启动类
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.company.usermodulith", "io.github.rosestack.mybatis"})
public class UserModulithApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserModulithApplication.class, args);
    }
}
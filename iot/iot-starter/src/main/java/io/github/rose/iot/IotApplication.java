package io.github.rose.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IoT物联网平台启动类
 * <p>
 * 基于Spring Boot的物联网平台主启动类，集成所有模块功能。
 * 支持设备接入、数据管理、规则引擎等核心功能。
 * </p>
 *
 * @author 技术团队
 * @since 1.0.0
 */
@SpringBootApplication
public class IotApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotApplication.class, args);
    }
}
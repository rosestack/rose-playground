package com.company.usermodulith;

import com.company.usermodulith.user.internal.UserConverter;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * User CRUD 应用主启动类
 * </p>
 *
 * @author Chen Soul
 * @since 1.0.0
 */
@SpringBootApplication
public class UserModulithApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserModulithApplication.class, args);
    }

    /**
     * 手动注册 MapStruct 生成的转换器 Bean
     * 这是一个备用方案，确保 MapStruct 生成的实现类能被 Spring 正确注册
     */
    @Bean
    public UserConverter userConverter() {
        return Mappers.getMapper(UserConverter.class);
    }
}
package io.github.rose.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@MapperScan("io.github.rose.**.mapper")
@EnableTransactionManagement
public class DomainConfig {
}
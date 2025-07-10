package io.github.rose.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.github.rose"})
@MapperScan("io.github.rose.**.mapper")
public class RoseServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoseServerApplication.class, args);
    }
}

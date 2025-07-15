package io.github.rose.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication(scanBasePackages = {"io.github.rose"})
@MapperScan("io.github.rose.**.mapper")
public class RoseServerApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RoseServerApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
    }
}

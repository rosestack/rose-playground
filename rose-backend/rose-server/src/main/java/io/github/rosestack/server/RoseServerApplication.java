package io.github.rosestack.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication(scanBasePackages = {"io.github.rosestack"})
public class RoseServerApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RoseServerApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
    }
}

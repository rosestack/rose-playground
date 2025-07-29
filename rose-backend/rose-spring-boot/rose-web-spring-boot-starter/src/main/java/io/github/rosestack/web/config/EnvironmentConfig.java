package io.github.rosestack.web.config;

import io.github.rosestack.core.spring.SpringBeanUtils;
import io.github.rosestack.core.spring.YmlPropertySourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class EnvironmentConfig {
    private static final Logger log = LoggerFactory.getLogger(EnvironmentConfig.class);

    @Order
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(WebServerInitializedEvent event) {
        String appName = SpringBeanUtils.getApplicationName();
        int localPort = event.getWebServer().getPort();
        String[] profiles = SpringBeanUtils.getActiveProfiles();
        log.info("Application {} finish to start with port {} and profiles {} ", appName, localPort, profiles);
    }
}
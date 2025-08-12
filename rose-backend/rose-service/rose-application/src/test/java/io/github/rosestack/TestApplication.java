package io.github.rosestack;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RabbitAutoConfiguration.class})
public class TestApplication {}

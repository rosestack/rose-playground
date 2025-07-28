package io.github.rosestack.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson 配置
 * <p>
 * 提供 Jackson JSON 序列化配置
 * </p>
 *
 * @author rosestack
 * @since 1.0.0
 */
@ConditionalOnClass(ObjectMapper.class)
public class JacksonConfig {

    private final WebProperties webProperties;

    public JacksonConfig(WebProperties webProperties) {
        this.webProperties = webProperties;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        WebProperties.Jackson jackson = webProperties.getJackson();

        // 配置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(jackson.getDateFormat()));
        // 配置时区
        objectMapper.setTimeZone(TimeZone.getTimeZone(jackson.getTimeZone()));

        // 配置日期时间模块
        objectMapper.registerModule(new JavaTimeModule());

        // 配置序列化选项
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, jackson.isWriteDatesAsTimestamps());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, jackson.isFailOnEmptyBeans());

        // 配置反序列化选项
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, jackson.isFailOnUnknownProperties());

        return objectMapper;
    }
} 
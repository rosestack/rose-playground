package io.github.rosestack.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

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

    private final RoseWebProperties roseWebProperties;

    public JacksonConfig(RoseWebProperties roseWebProperties) {
        this.roseWebProperties = roseWebProperties;
    }

} 
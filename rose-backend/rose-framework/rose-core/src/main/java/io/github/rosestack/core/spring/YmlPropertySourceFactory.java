package io.github.rosestack.core.spring;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Properties;

/**
 * yml 配置源工厂
 */
public class YmlPropertySourceFactory extends DefaultPropertySourceFactory {

    @Override
    @SuppressWarnings("null")
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = resource.getResource().getFilename();
        if (StringUtils.isNotBlank(sourceName) && StringUtils.endsWithAny(sourceName, ".yml", ".yaml")) {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();

            Properties properties = factory.getObject();
            if (properties == null) {
                properties = new Properties();
            }

            String actualSourceName = sourceName != null ? sourceName : "yaml-property-source";
            return new PropertiesPropertySource(actualSourceName, properties);
        }
        return super.createPropertySource(name, resource);
    }

}

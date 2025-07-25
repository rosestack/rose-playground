package io.github.rosestack.i18n.spring;

import io.github.rosestack.i18n.ReloadedResourceMessageSource;
import io.github.rosestack.i18n.spi.AbstractPropertiesResourceMessageSource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.springframework.util.StringUtils.hasText;

public class PropertySourceResourceI18nMessageSource extends
        AbstractPropertiesResourceMessageSource implements ReloadedResourceMessageSource, EnvironmentAware {
    private Environment environment;

    public PropertySourceResourceI18nMessageSource(String source) {
        super(source);
    }

    @Override
    protected String getResource(String resourceName) {
        return getSource() + "." + resourceName;
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(String resource) throws IOException {
        String propertiesContent = getPropertiesContent(resource);
        return hasText(propertiesContent) ? Arrays.asList(new StringReader(propertiesContent)) : Collections.emptyList();
    }

    protected String getPropertiesContent(String resource) {
        String propertyName = getPropertyName(resource);
        String propertiesContent = environment.getProperty(propertyName);
        return propertiesContent;
    }

    public String getPropertyName(Locale locale) {
        String resource = getResource(locale);
        String propertyName = getPropertyName(resource);
        return propertyName;
    }

    protected String getPropertyName(String resource) {
        String propertyName = resource;
        return propertyName;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Nullable
    @Override
    protected Locale getInternalLocale() {
        return LocaleContextHolder.getLocale();
    }

}

package io.github.rosestack.i18n.spring.boot.actuate;

import io.github.rosestack.i18n.AbstractResourceMessageSource;
import io.github.rosestack.i18n.I18nMessageSource;
import io.github.rosestack.i18n.spring.DelegatingI18nMessageSource;
import io.github.rosestack.i18n.spring.I18nConstants;
import io.github.rosestack.i18n.spring.PropertySourceResourceI18nMessageSource;
import io.github.rosestack.i18n.util.I18nUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.springframework.util.StringUtils.hasText;

/**
 * I18n Spring Boot Actuator Endpoint
 *
 * <pre>
 * {
 * "test.i18n_messages_zh.properties": {
 *
 * },
 * "META-INF/i18n/test/i18n_messages_zh_CN.properties": {
 * "a": "测试-a",
 * "hello": "您好,{}"
 * },
 * "META-INF/i18n/test/i18n_messages_en.properties": {
 * "a": "test-a",
 * "hello": "Hello,{}"
 * },
 * "META-INF/i18n/common/i18n_messages_zh_CN.properties": {
 * "a": "a"
 * }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@Endpoint(id = "i18n")
public class I18nEndpoint {
    public static final String PROPERTY_SOURCE_NAME = "i18nEndpointPropertySource";

    private List<I18nMessageSource> i18nMessageSources;

    @Autowired
    private ConfigurableEnvironment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        I18nMessageSource i18nMessageSource =
                context.getBean(I18nConstants.I18N_MESSAGE_SOURCE_BEAN_NAME, I18nMessageSource.class);
        initMessageSources(i18nMessageSource);
    }

    private void initMessageSources(I18nMessageSource serviceMessageSource) {
        List<I18nMessageSource> messageSources = emptyList();
        if (serviceMessageSource instanceof DelegatingI18nMessageSource) {
            DelegatingI18nMessageSource delegatingServiceMessageSource =
                    (DelegatingI18nMessageSource) serviceMessageSource;
            messageSources = delegatingServiceMessageSource.getDelegate().getMessageSources();
        }

        LinkedList<I18nMessageSource> allMessageSources = new LinkedList<>();

        int size = messageSources.size();
        for (int i = 0; i < size; i++) {
            List<I18nMessageSource> subServiceMessageSources = I18nUtils.findAllMessageSources(messageSources.get(i));
            allMessageSources.addAll(subServiceMessageSources);
        }

        this.i18nMessageSources = allMessageSources;
    }

    @ReadOperation
    public Map<String, Map<String, String>> invoke() {
        List<I18nMessageSource> messageSources = this.i18nMessageSources;
        int size = messageSources.size();
        Map<String, Map<String, String>> allLocalizedResourceMessages = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            // FIXME
            I18nMessageSource serviceMessageSource = messageSources.get(i);
            if (serviceMessageSource instanceof AbstractResourceMessageSource) {
                AbstractResourceMessageSource resourceMessageSource =
                        (AbstractResourceMessageSource) serviceMessageSource;
                Map<String, Map<String, String>> localizedResourceMessages =
                        resourceMessageSource.getLocalizedResourceMessages();
                localizedResourceMessages.forEach((k, v) -> allLocalizedResourceMessages.merge(
                        k, v, (oldValue, value) -> value.isEmpty() ? oldValue : value));
            }
        }
        return allLocalizedResourceMessages;
    }

    public Object getMessage(@Selector String code) {
        return getMessage(code, null);
    }

    @ReadOperation
    public List<Map<String, String>> getMessage(@Selector String code, @Selector Locale locale) {
        Set<Locale> supportedLocales = getSupportedLocales(locale);
        List<I18nMessageSource> messageSources = this.i18nMessageSources;
        int size = messageSources.size();
        List<Map<String, String>> messageMaps = new ArrayList<>(size * supportedLocales.size());

        for (int i = 0; i < size; i++) {
            I18nMessageSource serviceMessageSource = messageSources.get(i);
            for (Locale supportedLocale : supportedLocales) {
                Map<String, String> messageMap = new LinkedHashMap<>(5);
                String message = serviceMessageSource.getMessage(code, supportedLocale);

                messageMap.put("code", code);
                messageMap.put("source", serviceMessageSource.getSource());

                String resource = getResource(serviceMessageSource, supportedLocale);
                if (hasText(resource)) {
                    messageMap.put("resource", resource);
                }

                if (hasText(message)) {
                    messageMap.put("message", message);
                    messageMap.put("locale", supportedLocale.toString());
                }
                messageMaps.add(messageMap);
            }
        }
        return messageMaps;
    }

    @WriteOperation
    public Map<String, Object> addMessage(String source, Locale locale, String code, String message)
            throws IOException {
        PropertySourceResourceI18nMessageSource messageSource = getPropertySourcesServiceMessageSource(source);
        Properties properties = loadProperties(messageSource, locale);
        // Add a new code with message
        properties.setProperty(code, message);

        String propertyName = messageSource.getPropertyName(locale);
        StringWriter stringWriter = new StringWriter();
        // Properties -> StringWriter
        properties.store(stringWriter, "");
        // StringWriter -> String
        String propertyValue = stringWriter.toString();

        MapPropertySource propertySource = getPropertySource();
        Map<String, Object> newProperties = propertySource.getSource();
        newProperties.put(propertyName, propertyValue);

        // PropertySource-backed message source loads on demand; no explicit init required
        return newProperties;
    }

    private Properties loadProperties(PropertySourceResourceI18nMessageSource messageSource, Locale locale)
            throws IOException {
        String propertyName = messageSource.getPropertyName(locale);
        String content = environment.getProperty(propertyName);
        Properties properties = new Properties();
        if (hasText(content)) {
            try (Reader r = new StringReader(content)) {
                properties.load(r);
            }
        }
        return properties;
    }

    private MapPropertySource getPropertySource() {
        MutablePropertySources propertySources = environment.getPropertySources();
        String name = PROPERTY_SOURCE_NAME;
        MapPropertySource propertySource = (MapPropertySource) propertySources.get(name);
        if (propertySource == null) {
            Map<String, Object> properties = new HashMap<>();
            propertySource = new MapPropertySource(name, properties);
            propertySources.addFirst(propertySource);
        }
        return propertySource;
    }

    private PropertySourceResourceI18nMessageSource getPropertySourcesServiceMessageSource(String source) {
        return i18nMessageSources.stream()
                .filter(messageSource -> Objects.equals(source, messageSource.getSource()))
                .filter(this::isPropertySourcesMessageSource)
                .map(PropertySourceResourceI18nMessageSource.class::cast)
                .findFirst()
                .get();
    }

    private boolean isPropertySourcesMessageSource(I18nMessageSource serviceMessageSource) {
        return serviceMessageSource instanceof PropertySourceResourceI18nMessageSource;
    }

    private String getResource(I18nMessageSource messageSource, Locale locale) {
        String resource = null;
        if (messageSource instanceof AbstractResourceMessageSource) {
            AbstractResourceMessageSource resourceServiceMessageSource = (AbstractResourceMessageSource) messageSource;
            resource = resourceServiceMessageSource.getResource(locale);
        }
        return resource;
    }

    private Set<Locale> getSupportedLocales(Locale locale) {
        if (locale == null) {
            Set<Locale> locales = new LinkedHashSet<>();
            i18nMessageSources.forEach(messageSource -> {
                locales.addAll(messageSource.getSupportedLocales());
            });
            return locales;
        } else {
            return singleton(locale);
        }
    }
}

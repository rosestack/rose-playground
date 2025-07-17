package io.github.rose.i18n.spring.context;

import io.github.rose.core.util.BeanUtils;
import io.github.rose.core.util.ClassLoaderUtils;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.util.I18nUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.springframework.util.ObjectUtils.containsElement;

/**
 * Internationalization {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SmartApplicationListener
 * @since 1.0.0
 */
public class I18nApplicationListener implements SmartApplicationListener {
    String MESSAGE_SOURCE_BEAN_NAME = "i18nMessageSource";

    private static final Logger logger = LoggerFactory.getLogger(I18nApplicationListener.class);

    private static final String ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME = "org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver";

    private static final Class<?> ACCEPT_HEADER_LOCALE_RESOLVER_CLASS = ClassLoaderUtils.resolveClass(ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);

    private static final Class<?>[] SUPPORTED_EVENT_TYPES = {
            ContextRefreshedEvent.class,
            ContextClosedEvent.class
    };

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return containsElement(SUPPORTED_EVENT_TYPES, eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent((ContextClosedEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        initializeServiceMessageSource(context);

        initializeAcceptHeaderLocaleResolver(context);
    }

    private void initializeServiceMessageSource(ApplicationContext context) {
        I18nMessageSource serviceMessageSource = context.getBean(MESSAGE_SOURCE_BEAN_NAME, I18nMessageSource.class);
        I18nUtils.setMessageSource(serviceMessageSource);
    }


    @SuppressWarnings("unchecked")
    private void initializeAcceptHeaderLocaleResolver(ApplicationContext context) {
        if (ACCEPT_HEADER_LOCALE_RESOLVER_CLASS == null) {
            logger.debug("The class '{}' was not found!", ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);
            return;
        }

        Class<AcceptHeaderLocaleResolver> beanClass = (Class<AcceptHeaderLocaleResolver>) ACCEPT_HEADER_LOCALE_RESOLVER_CLASS;

        List<AcceptHeaderLocaleResolver> acceptHeaderLocaleResolvers = BeanUtils.getSortedBeans(context, beanClass);

        if (acceptHeaderLocaleResolvers.isEmpty()) {
            logger.debug("The '{}' Spring Bean was not found!", ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);
            return;
        }

        I18nMessageSource serviceMessageSource = BeanUtils.getOptionalBean(context, I18nMessageSource.class);

        for (AcceptHeaderLocaleResolver acceptHeaderLocaleResolver : acceptHeaderLocaleResolvers) {
            Locale defaultLocale = Locale.getDefault();
            Set<Locale> supportedLocales = serviceMessageSource.getSupportedLocales();
            acceptHeaderLocaleResolver.setDefaultLocale(defaultLocale);
            acceptHeaderLocaleResolver.setSupportedLocales(new ArrayList<>(supportedLocales));
            logger.debug("AcceptHeaderLocaleResolver Bean associated with default Locale : '{}' , list of supported Locales : {}", defaultLocale, supportedLocales);
        }
    }

    private void onContextClosedEvent(ContextClosedEvent event) {
        I18nUtils.destroyMessageSource();
    }
}
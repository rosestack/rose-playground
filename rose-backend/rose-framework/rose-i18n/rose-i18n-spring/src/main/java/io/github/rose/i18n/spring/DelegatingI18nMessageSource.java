package io.github.rose.i18n.spring;

import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.i18n.I18nMessageSource;
import io.github.rose.i18n.ReloadedResourceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.charset.Charset;
import java.util.*;

import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

public class DelegatingI18nMessageSource implements ReloadedResourceMessageSource, InitializingBean,
        DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingI18nMessageSource.class);

    private final ObjectProvider<I18nMessageSource> messageSources;

    private CompositeMessageSource delegate;

    private ListableBeanFactory beanFactory;

    public DelegatingI18nMessageSource(ObjectProvider<I18nMessageSource> messageSourceObjectProvider) {
        this.messageSources = messageSourceObjectProvider;
    }

    @Override
    public void init() {
        CompositeMessageSource delegate = this.delegate;
        if (delegate == null) {
            delegate = new CompositeMessageSource();
            delegate.setMessageSources(getI18nMessageSourceBeans());
            this.delegate = delegate;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return this.delegate.getMessage(code, locale, args);
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Locale locale) {
        return delegate.getMessages(locale);
    }

    @NonNull
    @Override
    public Locale getLocale() {
        return this.delegate.getLocale();
    }

    @NonNull
    @Override
    public Locale getDefaultLocale() {
        return this.delegate.getDefaultLocale();
    }

    @NonNull
    @Override
    public Set<Locale> getSupportedLocales() {
        return this.delegate.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return this.delegate.getSource();
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        this.delegate.reload(changedResources);
    }

    @Override
    public boolean canReload(Iterable<String> changedResources) {
        return this.delegate.canReload(changedResources);
    }

    @Override
    public void initializeResource(String resource) {
        this.delegate.initializeResource(resource);
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        this.delegate.initializeResources(resources);
    }

    @Override
    public Set<String> getInitializeResources() {
        return this.delegate.getInitializeResources();
    }

    @Override
    public Charset getEncoding() {
        return this.delegate.getEncoding();
    }

    @Override
    public void destroy() {
        this.delegate.destroy();
    }

    @Override
    public String toString() {
        return "DelegatingI18nMessageSource{" + "delegate=" + this.delegate + '}';
    }

    public CompositeMessageSource getDelegate() {
        return this.delegate;
    }

    private List<I18nMessageSource> getI18nMessageSourceBeans() {
        List<I18nMessageSource> i18nMessageSources = new LinkedList<>();
        messageSources.forEach(i18nMessageSources::add);
        sort(i18nMessageSources);
        logger.debug("Initializes the I18nMessageSource Bean list : {}", i18nMessageSources);
        return i18nMessageSources;
    }

}
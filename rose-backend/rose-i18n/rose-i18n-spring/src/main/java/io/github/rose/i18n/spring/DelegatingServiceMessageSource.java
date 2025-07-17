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

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

public class DelegatingServiceMessageSource implements ReloadedResourceMessageSource, InitializingBean,
        DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingServiceMessageSource.class);

    private final ObjectProvider<I18nMessageSource> messageSources;

    private CompositeMessageSource delegate;

    private ListableBeanFactory beanFactory;

    public DelegatingServiceMessageSource(ObjectProvider<I18nMessageSource> messageSourceObjectProvider) {
        this.messageSources = messageSourceObjectProvider;
    }

    @Override
    public void init() {
        CompositeMessageSource delegate = this.delegate;
        if (delegate == null) {
            delegate = new CompositeMessageSource();
            delegate.setMessageSources(getServiceMessageSourceBeans());
            this.delegate = delegate;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return delegate.getMessage(code, args, defaultMessage, locale);
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
        return "ServiceMessageSources{" + "delegate=" + delegate + '}';
    }

    public CompositeMessageSource getDelegate() {
        return delegate;
    }

    private List<I18nMessageSource> getServiceMessageSourceBeans() {
        List<I18nMessageSource> serviceMessageSources = new LinkedList<>();
        messageSources.forEach(serviceMessageSources::add);
        sort(serviceMessageSources);
        logger.debug("Initializes the ServiceMessageSource Bean list : {}", serviceMessageSources);
        return serviceMessageSources;
    }

}
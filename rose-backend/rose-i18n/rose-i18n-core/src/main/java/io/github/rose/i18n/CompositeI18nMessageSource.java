package io.github.rose.i18n;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

import static io.github.rose.core.collection.ListUtils.forEach;

public class CompositeI18nMessageSource implements ReloadableResourceI18nMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(CompositeI18nMessageSource.class);

    private List<? extends I18nMessageSource> serviceMessageSources;

    public CompositeI18nMessageSource() {
        this.serviceMessageSources = Collections.emptyList();
    }

    public CompositeI18nMessageSource(List<? extends I18nMessageSource> serviceMessageSources) {
        setServiceMessageSources(serviceMessageSources);
    }

    @Override
    public void init() {
        forEach(this.serviceMessageSources, I18nMessageSource::init);
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        for (I18nMessageSource serviceMessageSource : serviceMessageSources) {
            message = serviceMessageSource.getMessage(code, locale, args);
            if (message != null) {
                break;
            }
        }
        return message;
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        Map<String, String> messages = new HashMap<>();
        for (I18nMessageSource source : serviceMessageSources) {
            Map<String, String> sourceMessages = source.getMessages(codes, locale);
            if (sourceMessages != null) {
                messages.putAll(sourceMessages);
            }
        }
        return messages;
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Locale locale) {
        for (I18nMessageSource source : serviceMessageSources) {
            Map<String, String> sourceMessages = source.getMessages(locale);
            if (sourceMessages != null) {
                return sourceMessages;
            }
        }
        return Map.of();
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        I18nMessageSource serviceMessageSource = getFirstServiceMessageSource();
        return serviceMessageSource == null ? getDefaultLocale() : serviceMessageSource.getLocale();
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale() {
        I18nMessageSource serviceMessageSource = getFirstServiceMessageSource();
        return serviceMessageSource == null ? ReloadableResourceI18nMessageSource.super.getDefaultLocale() : serviceMessageSource.getLocale();
    }

    @Nonnull
    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> supportedLocales = new TreeSet<>();
        iterate(serviceMessageSource -> {
            for (Locale locale : serviceMessageSource.getSupportedLocales()) {
                if (!supportedLocales.contains(locale)) {
                    supportedLocales.add(locale);
                }
            }
        });

        return supportedLocales.isEmpty() ? getDefaultSupportedLocales() :
                Collections.unmodifiableSet(supportedLocales);
    }

    public Set<Locale> getDefaultSupportedLocales() {
        return ReloadableResourceI18nMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ReloadableResourceI18nMessageSource.super.getSource();
    }

    public void setServiceMessageSources(List<? extends I18nMessageSource> serviceMessageSources) {
        List<? extends I18nMessageSource> oldServiceMessageSources = this.serviceMessageSources;
        List<I18nMessageSource> newServiceMessageSources = new ArrayList<>(serviceMessageSources);
        OrderComparator.sort(newServiceMessageSources);
        if (oldServiceMessageSources != null) {
            oldServiceMessageSources.clear();
        }
        this.serviceMessageSources = newServiceMessageSources;
        logger.debug("Source '{}' sets ServiceMessageSource list, sorted : {}", serviceMessageSources, newServiceMessageSources);
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        iterate(ReloadableResourceI18nMessageSource.class, reloadableResourceServiceMessageSource -> {
            if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                reloadableResourceServiceMessageSource.reload(changedResources);
            }
        });
    }

    @Override
    public boolean canReload(Iterable<String> changedResources) {
        return true;
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(Collections.singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        iterate(ReloadableResourceI18nMessageSource.class, resourceServiceMessageSource -> {
            resourceServiceMessageSource.initializeResources(resources);
        });
    }

    @Override
    public Set<String> getInitializeResources() {
        Set<String> resources = new LinkedHashSet<>();
        iterate(ReloadableResourceI18nMessageSource.class, resourceServiceMessageSource -> {
            resources.addAll(resourceServiceMessageSource.getInitializeResources());
        });
        return Collections.unmodifiableSet(resources);
    }

    @Override
    public Charset getEncoding() {
        return ReloadableResourceI18nMessageSource.super.getEncoding();
    }

    /**
     * Get the read-only list of the composited {@link I18nMessageSource}
     *
     * @return non-null
     */
    @Nonnull
    public List<I18nMessageSource> getServiceMessageSources() {
        return Collections.unmodifiableList(serviceMessageSources);
    }

    @Override
    public void destroy() {
        List<? extends I18nMessageSource> serviceMessageSources = this.serviceMessageSources;
        forEach(serviceMessageSources, I18nMessageSource::destroy);
        serviceMessageSources.clear();
    }

    @Override
    public String toString() {
        return "CompositeI18nMessageSource{" +
                "serviceMessageSources=" + serviceMessageSources +
                '}';
    }

    private I18nMessageSource getFirstServiceMessageSource() {
        return this.serviceMessageSources.isEmpty() ? null : this.serviceMessageSources.get(0);
    }

    private <T> void iterate(Class<T> serviceMessageSourceType, Consumer<T> consumer) {
        this.serviceMessageSources.stream()
                .filter(serviceMessageSourceType::isInstance)
                .map(serviceMessageSourceType::cast)
                .forEach(consumer);
    }

    private <T> void iterate(Consumer<I18nMessageSource> consumer) {
        this.serviceMessageSources.forEach(consumer);
    }
}
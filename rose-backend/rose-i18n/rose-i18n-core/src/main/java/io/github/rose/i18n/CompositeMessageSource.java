package io.github.rose.i18n;

import io.github.rose.core.collection.ListUtils;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

/**
 * 组合型 I18nMessageSource，可聚合多个消息源，按顺序查找。
 */
public class CompositeMessageSource implements I18nMessageSource, ReloadedResourceMessageSource {
    private static final Logger logger = LoggerFactory.getLogger(CompositeMessageSource.class);

    private List<? extends I18nMessageSource> messageSources;

    public CompositeMessageSource() {
        this.messageSources = Collections.emptyList();
    }

    public CompositeMessageSource(List<? extends I18nMessageSource> messageSources) {
        setMessageSources(messageSources);
    }

    @Override
    public void init() {
        ListUtils.forEach(this.messageSources, I18nMessageSource::init);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String message = null;
        for (I18nMessageSource serviceMessageSource : messageSources) {
            message = serviceMessageSource.getMessage(code, args, defaultMessage, locale);
            if (message != null) {
                break;
            }
        }
        return message;
    }

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
        return ReloadedResourceMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ReloadedResourceMessageSource.super.getSource();
    }

    public void setMessageSources(List<? extends I18nMessageSource> messageSources) {
        List<? extends I18nMessageSource> oldmessageSources = this.messageSources;
        List<I18nMessageSource> newmessageSources = new ArrayList<>(messageSources);
        OrderComparator.sort(newmessageSources);
        if (oldmessageSources != null) {
            oldmessageSources.clear();
        }
        this.messageSources = newmessageSources;
        logger.debug("Source '{}' sets ServiceMessageSource list, sorted : {}", messageSources, newmessageSources);
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        iterate(ReloadedResourceMessageSource.class, reloadableResourceServiceMessageSource -> {
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
        iterate(ResourceMessageSource.class, resourceServiceMessageSource -> {
            resourceServiceMessageSource.initializeResources(resources);
        });
    }

    @Override
    public Set<String> getInitializeResources() {
        Set<String> resources = new LinkedHashSet<>();
        iterate(ResourceMessageSource.class, resourceServiceMessageSource -> {
            resources.addAll(resourceServiceMessageSource.getInitializeResources());
        });
        return Collections.unmodifiableSet(resources);
    }

    @Override
    public Charset getEncoding() {
        return ReloadedResourceMessageSource.super.getEncoding();
    }

    /**
     * Get the read-only list of the composited {@link I18nMessageSource}
     *
     * @return non-null
     */
    @Nonnull
    public List<I18nMessageSource> getMessageSources() {
        return Collections.unmodifiableList(messageSources);
    }

    @Override
    public void destroy() {
        List<? extends I18nMessageSource> messageSources = this.messageSources;
        ListUtils.forEach(messageSources, I18nMessageSource::destroy);
        messageSources.clear();
    }

    @Override
    public String toString() {
        return "CompositeMessageSource{" +
                "messageSources=" + messageSources +
                '}';
    }

    private I18nMessageSource getFirstServiceMessageSource() {
        return this.messageSources.isEmpty() ? null : this.messageSources.get(0);
    }

    private <T> void iterate(Class<T> serviceMessageSourceType, Consumer<T> consumer) {
        this.messageSources.stream()
                .filter(serviceMessageSourceType::isInstance)
                .map(serviceMessageSourceType::cast)
                .forEach(consumer);
    }

    private <T> void iterate(Consumer<I18nMessageSource> consumer) {
        this.messageSources.forEach(consumer);
    }
}

package io.github.rose.i18n;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Composite I18nMessageSource that can aggregate multiple message sources and search in order.
 */
public class CompositeMessageSource implements I18nMessageSource, ReloadedResourceMessageSource {
    private static final Logger logger = LoggerFactory.getLogger(CompositeMessageSource.class);

    private List<? extends I18nMessageSource> i18nMessageSources;

    public CompositeMessageSource() {
        this.i18nMessageSources = Collections.emptyList();
    }

    public CompositeMessageSource(List<? extends I18nMessageSource> i18nMessageSources) {
        if (i18nMessageSources == null) {
            this.i18nMessageSources = Collections.emptyList();
        } else {
            this.i18nMessageSources = i18nMessageSources;
        }
    }

    @Override
    public void init() {
        this.i18nMessageSources.forEach(I18nMessageSource::init);
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        if (code == null || i18nMessageSources == null || i18nMessageSources.isEmpty()) {
            return null;
        }

        String message = null;
        for (I18nMessageSource i18nMessageSource : i18nMessageSources) {
            if (i18nMessageSource != null) {
                message = i18nMessageSource.getMessage(code, locale, args);
                if (message != null) {
                    break;
                }
            }
        }
        return message;
    }

    @Nullable
    @Override
    public Map<String, String> getMessages(Locale locale) {
        for (I18nMessageSource i18nMessageSource : i18nMessageSources) {
            Map<String, String> messages = i18nMessageSource.getMessages(locale);
            if (ObjectUtils.isNotEmpty(messages)) {
                return messages;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        I18nMessageSource i18nMessageSource = getFirstMessageSource();
        return i18nMessageSource == null ? getDefaultLocale() : i18nMessageSource.getLocale();
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale() {
        I18nMessageSource i18nMessageSource = getFirstMessageSource();
        return i18nMessageSource == null ? ReloadedResourceMessageSource.super.getDefaultLocale() : i18nMessageSource.getDefaultLocale();
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        Set<Locale> supportedLocales = new LinkedHashSet<>();
        this.i18nMessageSources.forEach(serviceMessageSource -> {
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
        List<? extends I18nMessageSource> oldMessageSources = this.i18nMessageSources;

        if (messageSources == null) {
            this.i18nMessageSources = Collections.emptyList();
        } else {
            List<I18nMessageSource> newMessageSources = messageSources.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
            OrderComparator.sort(newMessageSources);
            this.i18nMessageSources = newMessageSources;
        }

        if (oldMessageSources != null && oldMessageSources instanceof ArrayList) {
            ((ArrayList<?>) oldMessageSources).clear();
        }

        logger.debug("Source '{}' sets ServiceMessageSource list, sorted : {}", messageSources, this.i18nMessageSources);
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
        return Collections.unmodifiableList(i18nMessageSources);
    }

    @Override
    public void destroy() {
        List<? extends I18nMessageSource> messageSources = this.i18nMessageSources;
        messageSources.forEach(I18nMessageSource::destroy);
        messageSources.clear();
    }

    @Override
    public String toString() {
        return "CompositeMessageSource{" +
                "messageSources=" + i18nMessageSources +
                '}';
    }

    private I18nMessageSource getFirstMessageSource() {
        return this.i18nMessageSources.isEmpty() ? null : this.i18nMessageSources.get(0);
    }

    private <T> void iterate(Class<T> messageSourceType, Consumer<T> consumer) {
        this.i18nMessageSources.stream()
                .filter(messageSourceType::isInstance)
                .map(messageSourceType::cast)
                .forEach(consumer);
    }
}

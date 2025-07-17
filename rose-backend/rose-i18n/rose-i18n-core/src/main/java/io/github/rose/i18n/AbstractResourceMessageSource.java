package io.github.rose.i18n;

import io.github.rose.core.util.Assert;
import io.github.rose.core.util.FormatUtils;
import io.github.rose.i18n.interpolation.DefaultMessageInterpolator;
import io.github.rose.i18n.interpolation.MessageInterpolator;
import io.github.rose.i18n.util.I18nResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singleton;

@Slf4j
public abstract class AbstractResourceMessageSource extends AbstractMessageSource implements ResourceMessageSource, ReloadedResourceMessageSource {
    public static final String DEFAULT_RESOURCE_NAME = "i18n_messages_";
    public static final String RESOURCE_LOCATION_PATTERN = "META-INF/i18n/%s/";

    private volatile Map<String, Map<String, String>> localizedResourceMessages = new ConcurrentHashMap<>();

    protected String location;
    protected String basename;
    protected String source;
    protected MessageInterpolator interpolator;

    public AbstractResourceMessageSource(String source) {
        this.source = source;
        this.location = RESOURCE_LOCATION_PATTERN.formatted(source);
        this.basename = DEFAULT_RESOURCE_NAME;
        this.interpolator = new DefaultMessageInterpolator();
    }

    @Override
    public void init() {
        Assert.assertNotNull(this.source, "The 'source' attribute must be assigned before initialization!");
        Assert.assertNotNull(getResourceSuffixes(), "getResourceSuffixes() Methods cannot return an empty array");
        initialize();
    }

    @Override
    public void destroy() {
        localizedResourceMessages.clear();
    }

    @Override
    public String getMessageInternal(String code, Object[] args, Locale locale) {
        for (Locale candidate : I18nResourceUtils.getFallbackLocales(locale)) {
            Map<String, String> messages = localizedResourceMessages.get(candidate);
            if (messages != null && messages.containsKey(code)) {
                String template = messages.get(code);
                return interpolator.interpolate(template, args, candidate);
            }
        }
        return null;
    }

    protected final void initialize() {
        Set<Locale> supportedLocales = getSupportedLocales();
        assertSupportedLocales(supportedLocales);
        Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(supportedLocales.size());
        for (Locale resolveLocale : supportedLocales) {
            for (String resourceSuffix : getResourceSuffixes()) {
                String resource = getResource(resolveLocale, resourceSuffix);
                initializeResource(resource, localizedResourceMessages);
            }
        }
        // Exchange the field
        this.localizedResourceMessages = localizedResourceMessages;
        log.debug("Source '{}' Initialization is completed , localizedResourceMessages : {}", source, localizedResourceMessages);
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        synchronized (this) {
            // Copy the current messages and initialized resources
            Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(this.localizedResourceMessages);
            initializeResources(resources, localizedResourceMessages);
            // Exchange the field
            this.localizedResourceMessages = localizedResourceMessages;
        }
    }

    private void assertSupportedLocales(Set<Locale> supportedLocales) {
        if (CollectionUtils.isEmpty(supportedLocales)) {
            throw new IllegalStateException(String.format("{}.getSupportedLocales() Methods cannot return an empty list of locales!", this.getClass()));
        }
    }

    private void initializeResources(Iterable<String> resources, Map<String, Map<String, String>> localizedResourceMessages) {
        for (String resource : resources) {
            initializeResource(resource, localizedResourceMessages);
        }
    }

    private void initializeResource(String resource, Map<String, Map<String, String>> localizedResourceMessages) {
        Map<String, String> messages = loadMessages(resource);
        log.debug("Source '{}' loads the resource['{}'] messages : {}", source, resource, messages);

        if (messages == null) {
            return;
        }

        // Override the localized message if present
        localizedResourceMessages.putIfAbsent(resource, messages);
    }

    @Override
    public Set<String> getInitializeResources() {
        return localizedResourceMessages.keySet();
    }

    protected String getResource(Locale locale, String resourceSuffix) {
        return location + DEFAULT_RESOURCE_NAME + "_" + locale + resourceSuffix;
    }

    protected abstract String[] getResourceSuffixes();

    public void setSource(String source) {
        this.source = source;
    }

    public void setInterpolator(MessageInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    protected abstract Map<String, String> loadMessages(String resource);
} 
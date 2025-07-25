package io.github.rose.i18n;

import io.github.rose.i18n.render.DefaultMessageRenderer;
import io.github.rose.i18n.render.MessageRenderer;
import io.github.rose.i18n.util.I18nUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

@Slf4j
public abstract class AbstractResourceMessageSource extends AbstractMessageSource implements ResourceMessageSource, ReloadedResourceMessageSource {
    public static final String DEFAULT_RESOURCE_LOCATION = "i18n";
    public static final String DEFAULT_RESOURCE_NAME = "i18n_messages";

    private volatile Map<String, Map<String, String>> localizedResourceMessages = new ConcurrentHashMap<>();

    protected String location;
    protected String basename;
    protected MessageRenderer messageRenderer;

    public AbstractResourceMessageSource(String source) {
        super(source);
        this.location = DEFAULT_RESOURCE_LOCATION;
        this.basename = DEFAULT_RESOURCE_NAME;
        this.messageRenderer = new DefaultMessageRenderer();
    }

    @Override
    public void init() {
        initialize();
    }

    @Override
    public void destroy() {
        localizedResourceMessages.clear();
    }

    @Override
    public String getMessageInternal(@Nullable String code, @Nullable Locale locale, @Nullable Object... args) {
        for (Locale candidate : I18nUtils.getFallbackLocales(locale)) {
            Map<String, String> messages = localizedResourceMessages.get(getResource(candidate));
            if (messages != null && messages.containsKey(code)) {
                String template = messages.get(code);
                return messageRenderer.render(template, candidate, args);
            }
        }
        return null;
    }

    @Override
    protected Map<String, String> getMessagesInternal(Locale locale) {
        return localizedResourceMessages.get(getResource(locale));
    }

    protected final void initialize() {
        Set<Locale> supportedLocales = getSupportedLocales();
        if (CollectionUtils.isEmpty(supportedLocales)) {
            throw new IllegalStateException(String.format("{}.getSupportedLocales() Methods cannot return an empty list of locales!", this.getClass()));
        }

        Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(supportedLocales.size());
        for (Locale resolveLocale : supportedLocales) {
            String resource = getResource(resolveLocale);
            initializeResource(resource, localizedResourceMessages);

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
            for (String resource : resources) {
                initializeResource(resource, localizedResourceMessages);
            }
            // Exchange the field
            this.localizedResourceMessages = localizedResourceMessages;
        }
    }

    @Override
    public Set<String> getInitializeResources() {
        return localizedResourceMessages.keySet();
    }

    protected String buildResourceName(Locale locale) {
        requireNonNull(getResourceSuffix(), "'getResourceSuffix()' method reture value must not be null");

        return basename + "_" + locale + getResourceSuffix();
    }

    protected String getResource(String resourceName) {
        return location + "/" + source + "/" + resourceName;
    }

    protected String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName).replaceAll("//", "/");
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public MessageRenderer getMessageRenderer() {
        return messageRenderer;
    }

    private void initializeResource(String resource, Map<String, Map<String, String>> localizedResourceMessages) {
        Map<String, String> messages = loadMessages(resource);
        log.debug("Source '{}' loads the resource['{}'] messages : {}", source, resource, messages);

        if (messages == null) {
            return;
        }

        // Override the localized message if present
        localizedResourceMessages.put(resource, messages);
    }

    protected abstract String getResourceSuffix();

    protected abstract Map<String, String> loadMessages(String resource);
} 
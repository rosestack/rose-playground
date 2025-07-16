package io.github.rose.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

import io.github.rose.i18n.util.I18nUtils;
import io.github.rose.i18n.util.MessageFormatCache;

/**
 * Abstract Resource {@link I18nMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceI18nMessageSource extends AbstractI18nMessageSource implements ResourceI18nMessageSource {
    protected static final MessageFormatCache MESSAGE_FORMAT_CACHE = new MessageFormatCache();

    /**
     * The default prefix of message resource name
     */
    public static final String DEFAULT_RESOURCE_NAME_PREFIX = "i18n_messages_";

    /**
     * The default suffix of message resource name
     */
    public static final String DEFAULT_RESOURCE_NAME_SUFFIX = ".properties";

    // path -> messages
    private volatile Map<String, Map<String, String>> localizedResourceMessages = new ConcurrentHashMap<>();

    public AbstractResourceI18nMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        requireNonNull(this.source, "The 'source' attribute must be assigned before initialization!");

        Set<Locale> supportedLocales = getSupportedLocales();
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            throw new IllegalStateException(this.getClass() + ".getSupportedLocales() Methods cannot return an empty list of locales!");
        }

        for (Locale resolveLocale : supportedLocales) {
            String resource = getResource(resolveLocale);
            initializeResource(resource);
        }
    }

    @Override
    public void initializeResource(String resource) {
        requireNonNull(resource, "The 'resource' attribute must be assigned before initialization!");
        Map<String, String> messages = loadMessages(resource);
        this.localizedResourceMessages.put(resource, messages);
    }

    @Override
    public Set<String> getInitializeResources() {
        return localizedResourceMessages.keySet();
    }

    @Override
    public void destroy() {
        if (this.localizedResourceMessages != null) {
            this.localizedResourceMessages.clear();
        }
    }

    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        Map<String, String> messageMap = localizedResourceMessages.getOrDefault(getResource(locale), emptyMap());

        Map<String, String> messages = new HashMap<>();
        for (String code : codes) {
            String sourceMessages = messageMap.get(code);
            if (sourceMessages != null) {
                messages.put(code, sourceMessages);
            }
        }
        return messages;
    }

    @Override
    public Map<String, String> getMessages(Locale locale) {
        return localizedResourceMessages.getOrDefault(getResource(locale), emptyMap());
    }

    @Override
    protected String doGetMessage(String code, Locale locale, Object[] args) {
        String message = null;
        Map<String, String> messages = getMessages(locale);
        if (messages != null) {
            String messagePattern = messages.get(code);
            if (messagePattern != null) {
                message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, locale, args);
            }
        }
        return message;
    }

    protected String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName);
    }

    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    protected abstract Map<String, String> loadMessages(String resource);


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                .append("{source='").append(source).append('\'')
                .append(", defaultLocale=").append(getDefaultLocale())
                .append(", supportedLocales=").append(getSupportedLocales())
                .append(", localizedResourceMessages=").append(localizedResourceMessages)
                .append('}');
        return sb.toString();
    }
}
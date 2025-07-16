package io.github.rose.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    private volatile Map<String, Map<String, String>> localizedResourceMessages = emptyMap();

    public AbstractResourceI18nMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        requireNonNull(this.source, "The 'source' attribute must be assigned before initialization!");
        initialize();
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

    @Override
    public void destroy() {
        clearAllMessages();
    }

    @Override
    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        if (localizedResourceMessages == null) {
            return emptyMap();
        }
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
    public Map<String, String> getAllMessages(Locale locale) {
        if (localizedResourceMessages == null) {
            return emptyMap();
        }
        return localizedResourceMessages.getOrDefault(getResource(locale), emptyMap());
    }

    protected String resolveMessageCode(String code) {
        if (code.startsWith(codePrefix)) { // The complete Message code
            return code;
        }
        return codePrefix + code;
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args) {
        String message = null;
        Map<String, String> messages = getMessages(resolvedLocale);
        if (messages != null) {
            String messagePattern = messages.get(resolvedCode);
            if (messagePattern != null) {
                message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, locale, args);
            }
        }
        return message;
    }

    /**
     * Initialization
     */
    protected final void initialize() {
        Set<Locale> supportedLocales = getSupportedLocales();
        assertSupportedLocales(supportedLocales);
        Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(supportedLocales.size());
        for (Locale resolveLocale : I18nUtils.resolveLocales(supportedLocales)) {
            String resource = getResource(resolveLocale);
            initializeResource(resource, localizedResourceMessages);
        }
        this.localizedResourceMessages = localizedResourceMessages;
    }

    private void assertSupportedLocales(Set<Locale> supportedLocales) {
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            throw new IllegalStateException(this.getClass() + ".getSupportedLocales() Methods cannot return an empty list of locales!");
        }
    }

    protected final void clearAllMessages() {
        if (this.localizedResourceMessages != null) {
            this.localizedResourceMessages.clear();
        }
        this.localizedResourceMessages = emptyMap();
    }

    public String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName);
    }

    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    protected abstract Map<String, String> loadMessages(String resource);

    public final Map<String, String> getMessages(Locale locale) {
        if (localizedResourceMessages == null) {
            return emptyMap();
        }
        String resource = getResource(locale);
        return localizedResourceMessages.get(resource);
    }

    private void initializeResources(Iterable<String> resources, Map<String, Map<String, String>> localizedResourceMessages) {
        for (String resource : resources) {
            initializeResource(resource, localizedResourceMessages);
        }
    }

    private void initializeResource(String resource, Map<String, Map<String, String>> localizedResourceMessages) {
        Map<String, String> messages = loadMessages(resource);
        if (messages == null) {
            return;
        }

        // Override the localized message if present
        localizedResourceMessages.put(resource, messages);
    }

    public Map<String, Map<String, String>> getLocalizedResourceMessages() {
        return unmodifiableMap(this.localizedResourceMessages);
    }

    @Override
    public Set<String> getInitializeResources() {
        return localizedResourceMessages.keySet();
    }

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
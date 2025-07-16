package io.github.rose.i18n;

import io.github.rose.core.util.Assert;
import io.github.rose.i18n.util.MessageFormatCache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

/**
 * Abstract Resource {@link MessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceMessageSource extends AbstractMessageSource implements ResourceMessageSource {
    protected static final MessageFormatCache MESSAGE_FORMAT_CACHE = new MessageFormatCache();
    public static final String RESOURCE_PATH_PATTERN = "META-INF/i18n/%s/";
    public static final String DEFAULT_RESOURCE_NAME_PREFIX = "i18n_messages_";

    // path -> messages
    private volatile Map<String, Map<String, String>> localizedResourceMessages = new ConcurrentHashMap<>();

    public AbstractResourceMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        Assert.assertNotNull(this.source, "The 'source' attribute must be assigned before initialization!");

        Set<Locale> supportedLocales = getSupportedLocales();
        Assert.assertNotEmpty(supportedLocales, "supportedLocales must not be null");

        for (Locale locale : supportedLocales) {
            String resourceName = getResourceName(locale);
            initializeResource(resourceName);
        }
    }

    @Override
    public void initializeResource(String resourceName) {
        requireNonNull(resourceName, "The 'resource' attribute must be assigned before initialization!");
        Map<String, String> messages = loadMessages(resourceName);
        this.localizedResourceMessages.put(getKey(resourceName), messages);
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
        Map<String, String> messageMap = localizedResourceMessages.getOrDefault(getKey(getResourceName(locale)), emptyMap());

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
        return localizedResourceMessages.getOrDefault(getKey(getResourceName(locale)), emptyMap());
    }

    @Override
    protected String doGetMessage(String code, Locale locale, Object[] args) {
        String message = null;
        // 1. 完整 locale
        Map<String, String> messages = getMessages(locale);
        if (messages != null) {
            String messagePattern = messages.get(code);
            if (messagePattern != null) {
                message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, locale, args);
                if (message != null) return message;
            }
        }
        // 2. 仅 language（如 zh_CN -> zh）
        if (locale != null && !locale.getCountry().isEmpty()) {
            Locale languageOnly = new Locale(locale.getLanguage());
            messages = getMessages(languageOnly);
            if (messages != null) {
                String messagePattern = messages.get(code);
                if (messagePattern != null) {
                    message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, languageOnly, args);
                    if (message != null) return message;
                }
            }
        }
        // 3. 默认 locale
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null && !defaultLocale.equals(locale)) {
            messages = getMessages(defaultLocale);
            if (messages != null) {
                String messagePattern = messages.get(code);
                if (messagePattern != null) {
                    message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, defaultLocale, args);
                    if (message != null) return message;
                }
            }
        }
        // 4. fallback: null
        return null;
    }

    private String getKey(String resourceName) {
        return source + "/" + resourceName;
    }

    protected abstract String getResourceName(Locale locale);

    protected abstract Map<String, String> loadMessages(String resourceName);

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
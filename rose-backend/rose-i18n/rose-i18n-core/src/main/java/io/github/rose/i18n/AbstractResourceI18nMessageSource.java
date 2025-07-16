package io.github.rose.i18n;

import io.github.rose.core.util.FormatUtils;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

/**
 * Abstract Resource {@link I18nMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceI18nMessageSource extends AbstractI18nMessageSource implements ResourceI18nMessageSource {

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

    @Nullable
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

    @Nullable
    @Override
    public Map<String, String> getAllMessages(Locale locale) {
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
                message = resolveMessage(messagePattern, args);
                logMessage(code, resolvedCode, locale, resolvedLocale, args, messagePattern, message);
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
        for (Locale resolveLocale : supportedLocales) {
            String resource = getResource(resolveLocale);
            initializeResource(resource, localizedResourceMessages);
        }
        // Exchange the field
        this.localizedResourceMessages = localizedResourceMessages;
        log.debug("Source '{}' Initialization is completed , localizedResourceMessages : {}", source, localizedResourceMessages);
    }

    private void assertSupportedLocales(Set<Locale> supportedLocales) {
        if (CollectionUtils.isEmpty(supportedLocales)) {
            throw new IllegalStateException(FormatUtils.format("{}.getSupportedLocales() Methods cannot return an empty list of locales!", this.getClass()));
        }
    }

    protected final void clearAllMessages() {
        this.localizedResourceMessages.clear();
        this.localizedResourceMessages = null;
    }

    private void validateMessages(Map<String, String> messages, String resourceName) {
        messages.forEach((code, message) -> validateMessageCode(code, resourceName));
    }

    protected void validateMessageCode(String code, String resourceName) {
        validateMessageCodePrefix(code, resourceName);
    }

    private void validateMessageCodePrefix(String code, String resourceName) {
        if (!code.startsWith(codePrefix)) {
            throw new IllegalStateException(FormatUtils.format("Source '{}' Message Resource[name : '{}'] code '{}' must start with '{}'",
                    source, resourceName, code, codePrefix));
        }
    }

    public String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName);
    }

    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    @Nullable
    protected abstract Map<String, String> loadMessages(String resource);

    @Nullable
    public final Map<String, String> getMessages(Locale locale) {
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
        log.debug("Source '{}' loads the resource['{}'] messages : {}", source, resource, messages);

        if (messages == null) {
            return;
        }

        validateMessages(messages, resource);
        // Override the localized message if present
        localizedResourceMessages.put(resource, messages);
    }

    protected void logMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args,
                              String messagePattern, String message) {
        if (log.isDebugEnabled()) {
            log.debug("Source '{}' gets Message[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}' , pattern : '{}'] : '{}'",
                    source, code, resolvedCode, locale, resolvedLocale, ArrayUtils.toString(args), messagePattern, message);
        }
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
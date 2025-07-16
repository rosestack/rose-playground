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

import io.github.rose.i18n.LocaleResolver;

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

        // 先加载所有 locale 资源
        for (Locale locale : supportedLocales) {
            String resourceName = getResourceName(locale);
            initializeResource(resourceName);
        }
        // 最后加载无后缀兜底资源，且不覆盖已存在的 key
        String defaultResourceName = getResourceName(null);
        initializeResource(defaultResourceName);
    }

    @Override
    public void initializeResource(String resourceName) {
        requireNonNull(resourceName, "The 'resource' attribute must be assigned before initialization!");
        String key = getKey(resourceName);
        if (!this.localizedResourceMessages.containsKey(key)) {
            Map<String, String> messages = loadMessages(resourceName);
            this.localizedResourceMessages.put(key, messages);
        }
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

    public Map<String, String> getMessages(Set<String> codes, Locale locale) {
        Map<String, String> messageMap = localizedResourceMessages.getOrDefault(getKey(getResourceName(locale)), emptyMap());
        if (codes == null || codes.isEmpty()) return messageMap;
        Map<String, String> result = new HashMap<>();
        for (String code : codes) {
            if (messageMap.containsKey(code)) {
                result.put(code, messageMap.get(code));
            }
        }
        return result;
    }

    public Map<String, String> getMessages(Locale locale) {
        if (locale == null) {
            // 只查 generic 资源，不再 fallback 到默认 locale
            return localizedResourceMessages.getOrDefault(getKey(getResourceName(null)), emptyMap());
        }
        return localizedResourceMessages.getOrDefault(getKey(getResourceName(locale)), emptyMap());
    }

    public boolean isSupportedLocale(Locale locale) {
        if (locale == null) return false;
        return localizedResourceMessages.containsKey(getKey(getResourceName(locale)));
    }

    @Override
    protected String doGetMessage(String code, Locale locale, Object[] args) {
        if (locale == null) {
            locale = getDefaultLocale();
        }
        String message = null;
        // 1. 完整 locale
        Map<String, String> messages = getMessages(locale);
        String messagePattern = messages != null ? messages.get(code) : null;
        if (messagePattern != null) {
            message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, locale, args);
            if (message != null) {
                return message;
            }
        }
        // 2. 仅 language（如 zh_CN -> zh）
        if (locale != null && !locale.getCountry().isEmpty()) {
            Locale languageOnly = new Locale(locale.getLanguage());
            messages = getMessages(languageOnly);
            messagePattern = messages != null ? messages.get(code) : null;
            if (messagePattern != null) {
                message = MESSAGE_FORMAT_CACHE.formatMessage(messagePattern, languageOnly, args);
                if (message != null) {
                    return message;
                }
            }
        }
        // 3. generic（无后缀）
        Map<String, String> genericMessages = getMessages(null);
        String genericPattern = genericMessages != null ? genericMessages.get(code) : null;
        if (genericPattern != null) {
            message = MESSAGE_FORMAT_CACHE.formatMessage(genericPattern, getDefaultLocale(), args);
            if (message != null) {
                return message;
            }
        }
        // 4. fallback 到默认 locale
        Locale def = getDefaultLocale();
        Map<String, String> defMessages = getMessages(def);
        String defPattern = defMessages != null ? defMessages.get(code) : null;
        if (defPattern != null) {
            message = MESSAGE_FORMAT_CACHE.formatMessage(defPattern, def, args);
            if (message != null) {
                return message;
            }
        }
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
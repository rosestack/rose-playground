package io.github.rosestack.i18n;

import static java.util.Objects.requireNonNull;

import io.github.rosestack.i18n.util.I18nUtils;
import java.util.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public abstract class AbstractMessageSource implements HierarchicalMessageSource, I18nMessageSource {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMessageSource.class);

    protected String source;

    private I18nMessageSource parentMessageSource;
    private Locale defaultLocale;
    private List<Locale> supportedLocales;
    private MessageCacheLoader messageCacheLoader;

    protected AbstractMessageSource(String source) {
        requireNonNull(source, "'source' argument must not be null");
        this.source = source;
    }

    protected static List<Locale> resolveLocales(List<Locale> supportedLocales) {
        List<Locale> resolvedLocales = new ArrayList<>();
        for (Locale supportedLocale : supportedLocales) {
            addLocale(resolvedLocales, supportedLocale);
            for (Locale derivedLocale : I18nUtils.getFallbackLocales(supportedLocale)) {
                addLocale(resolvedLocales, derivedLocale);
            }
        }
        return Collections.unmodifiableList(resolvedLocales);
    }

    protected static void addLocale(List<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    @Nullable @Override
    public I18nMessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    @Override
    public void setParentMessageSource(@Nullable I18nMessageSource parent) {
        this.parentMessageSource = parent;
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        if (code == null) {
            return null;
        }

        // 首先尝试从缓存获取
        if (messageCacheLoader != null) {
            String cachedMessage = messageCacheLoader.getFromCache(code, locale);
            if (cachedMessage != null) {
                return cachedMessage;
            }
        }

        String msg = this.getMessageInternal(code, locale, args);
        if (msg == null) {
            msg = getMessageFromParent(code, locale, args);
        }

        if (msg != null) {
            // 将消息放入缓存
            if (messageCacheLoader != null) {
                messageCacheLoader.putToCache(code, locale, msg);
            }
        }

        return msg;
    }

    @Nullable @Override
    public Map<String, String> getMessages(Locale locale) {
        // 首先尝试从缓存获取所有消息
        if (messageCacheLoader != null) {}

        Map<String, String> messages = this.getMessagesInternal(locale);
        if (ObjectUtils.isEmpty(messages)) {
            messages = this.getMessagesFromParent(locale);
        }

        // 将获取到的消息批量放入缓存
        if (messages != null && !messages.isEmpty() && messageCacheLoader != null) {
            messageCacheLoader.putToCache(messages, locale);
        }

        return messages;
    }

    protected abstract Map<String, String> getMessagesInternal(Locale locale);

    @Nullable protected abstract String getMessageInternal(
            @Nullable String code, @Nullable Locale locale, @Nullable Object... args);

    @Nullable protected Map<String, String> getMessagesFromParent(Locale locale) {
        I18nMessageSource parent = this.getParentMessageSource();
        if (parent != null) {
            if (parent instanceof AbstractMessageSource) {
                AbstractMessageSource abstractMessageSource = (AbstractMessageSource) parent;
                return abstractMessageSource.getMessagesInternal(locale);
            } else {
                return parent.getMessages(locale);
            }
        } else {
            return null;
        }
    }

    @Nullable protected String getMessageFromParent(String code, Locale locale, @Nullable Object... args) {
        if (code == null) {
            return null;
        }

        I18nMessageSource parent = this.getParentMessageSource();
        if (parent != null) {
            if (parent instanceof AbstractMessageSource) {
                AbstractMessageSource abstractMessageSource = (AbstractMessageSource) parent;
                return abstractMessageSource.getMessageInternal(code, locale, args);
            } else {
                return parent.getMessage(code, locale, args);
            }
        } else {
            return null;
        }
    }

    @NonNull @Override
    public final Locale getLocale() {
        Locale locale = getInternalLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    /**
     * Get the internal {@link Locale}
     *
     * @return the internal {@link Locale}
     */
    @Nullable protected Locale getInternalLocale() {
        return null;
    }

    @NonNull @Override
    public final Locale getDefaultLocale() {
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return HierarchicalMessageSource.super.getDefaultLocale();
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        logger.debug("Source '{}' sets the default Locale : '{}'", source, defaultLocale);
    }

    @Override
    public List<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return HierarchicalMessageSource.super.getSupportedLocales();
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = resolveLocales(supportedLocales);
        logger.debug("Source '{}' sets the supported Locales : {}", source, supportedLocales);
    }

    @Override
    public String getSource() {
        return source;
    }

    public void setMessageCacheLoader(MessageCacheLoader messageCacheLoader) {
        this.messageCacheLoader = messageCacheLoader;
    }
}

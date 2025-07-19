package io.github.rose.i18n;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public abstract class AbstractMessageSource implements HierarchicalMessageSource, I18nMessageSource {
    private static final Logger log = LoggerFactory.getLogger(AbstractMessageSource.class);
    protected String source;

    private I18nMessageSource parentMessageSource;
    private Locale defaultLocale;
    private Set<Locale> supportedLocales;

    protected AbstractMessageSource(String source) {
        requireNonNull(source, "'source' argument must not be null");
        this.source = source;
    }

    @Override
    public void setParentMessageSource(@Nullable I18nMessageSource parent) {
        this.parentMessageSource = parent;
    }

    @Nullable
    @Override
    public I18nMessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        if (code == null) {
            return null;
        }

        String msg = this.getMessageInternal(code, locale, args);
        if (msg != null) {
            return msg;
        }
        msg = getMessageFromParent(code, locale, args);
        if (msg != null) {
            return msg;
        }
        return null;
    }

    @Nullable
    @Override
    public Map<String, String>  getMessages(Locale locale) {
        Map<String, String>  messages = this.getMessagesInternal(locale);
        if (ObjectUtils.isEmpty(messages)) {
            return this.getMessagesFromParent(locale);
        }
        return messages;
    }

    protected abstract Map<String, String>  getMessagesInternal(Locale locale);

    @Nullable
    protected abstract String  getMessageInternal(@Nullable String code, @Nullable Locale locale, @Nullable Object... args);

    @Nullable
    protected Map<String, String>  getMessagesFromParent(Locale locale) {
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

    @Nullable
    protected String getMessageFromParent(String code, Locale locale, @Nullable Object... args) {
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

    @NonNull
    @Override
    public final Locale getLocale() {
        Locale locale = getInternalLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    /**
     * Get the internal {@link Locale}
     *
     * @return the internal {@link Locale}
     */
    @Nullable
    protected Locale getInternalLocale() {
        return null;
    }

    @NonNull
    @Override
    public final Locale getDefaultLocale() {
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return HierarchicalMessageSource.super.getDefaultLocale();
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return HierarchicalMessageSource.super.getSupportedLocales();
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        log.debug("Source '{}' sets the default Locale : '{}'", source, defaultLocale);
    }

    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = resolveLocales(supportedLocales);
        log.debug("Source '{}' sets the supported Locales : {}", source, supportedLocales);
    }

    protected static Set<Locale> resolveLocales(Set<Locale> supportedLocales) {
        Set<Locale> resolvedLocales = new LinkedHashSet<>();
        for (Locale supportedLocale : supportedLocales) {
            addLocale(resolvedLocales, supportedLocale);
            for (Locale derivedLocale : resolveDerivedLocales(supportedLocale)) {
                addLocale(resolvedLocales, derivedLocale);
            }
        }
        return Collections.unmodifiableSet(resolvedLocales);
    }

    protected static void addLocale(Set<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    protected static List<Locale> resolveDerivedLocales(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = StringUtils.isNotBlank(region);
        boolean hasVariant = StringUtils.isNotBlank(variant);

        if (!hasRegion && !hasVariant) {
            return emptyList();
        }

        List<Locale> derivedLocales = new LinkedList<>();

        if (hasVariant) {
            derivedLocales.add(new Locale(language, region));
        }

        if (hasRegion) {
            derivedLocales.add(new Locale(language));
        }

        return derivedLocales;
    }

    @Override
    public String getSource() {
        return source;
    }
}

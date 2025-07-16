package io.github.rose.i18n;

import io.github.rose.core.util.FormatUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractI18nMessageSource implements I18nMessageSource {
    protected static final Logger log = LoggerFactory.getLogger(AbstractI18nMessageSource.class);

    protected static final String SOURCE_SEPARATOR = ".";

    protected final String source;

    protected final String codePrefix;

    private Set<Locale> supportedLocales;

    private Locale defaultLocale;

    public AbstractI18nMessageSource(String source) {
        requireNonNull(source, "'source' argument must not be null");
        this.source = source;
        this.codePrefix = source + SOURCE_SEPARATOR;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public final String getMessage(String code, Object... args) {
        return I18nMessageSource.super.getMessage(code, args);
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            String resolvedCode = resolveMessageCode(code);
            if (resolvedCode != null) {
                Locale resolvedLocale = resolveLocale(locale);
                message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
            }
        }
        return message;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public final Locale getDefaultLocale() {
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return I18nMessageSource.super.getDefaultLocale();
    }

    @Nonnull
    @Override
    public final Set<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return I18nMessageSource.super.getSupportedLocales();
    }

    @Override
    public final String getSource() {
        return source;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        log.debug("Source '{}' sets the default Locale : '{}'", source, defaultLocale);
    }

    public void setSupportedLocales(Set<Locale> supportedLocales) {
        this.supportedLocales = resolveLocales(supportedLocales);
        log.debug("Source '{}' sets the supported Locales : {}", source, supportedLocales);
    }

    protected String resolveMessageCode(String code) {
        return code;
    }

    protected Locale resolveLocale(Locale locale) {
        return locale;
    }

    protected abstract String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args);

    protected boolean supports(Locale locale) {
        return getSupportedLocales().contains(locale);
    }

    protected static Set<Locale> resolveLocales(Set<Locale> supportedLocales) {
        Set<Locale> resolvedLocales = new TreeSet<>();
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

        boolean hasRegion = isNotBlank(region);
        boolean hasVariant = isNotBlank(variant);

        if (!hasRegion && !hasVariant) {
            return Collections.emptyList();
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

    protected String resolveMessage(String message, Object... args) {
        // Using FormatUtils#format, future subclasses may re-implement formatting
        return FormatUtils.format(message, args);
    }
}

package io.github.rose.i18n.config;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Configuration class for Message Sources
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class MessageSourceConfiguration {

    /**
     * Source name
     */
    private String source;

    /**
     * Encoding for resource files
     */
    private Charset encoding = StandardCharsets.UTF_8;

    /**
     * Base paths for resource resolution
     */
    private List<String> basePaths;

    /**
     * Supported locales
     */
    private List<Locale> supportedLocales;

    /**
     * Default locale
     */
    private Locale defaultLocale = Locale.getDefault();

    /**
     * Cache TTL for loaded messages (for caching implementations)
     */
    private Duration cacheTtl = Duration.ofMinutes(30);

    /**
     * Whether to enable fallback to default locale
     */
    private boolean enableFallback = true;

    /**
     * Whether to enable resource reloading
     */
    private boolean enableReloading = false;

    /**
     * Whether to fail fast on missing resources
     */
    private boolean failFast = false;

    // Getters and Setters

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    public List<String> getBasePaths() {
        return basePaths;
    }

    public void setBasePaths(List<String> basePaths) {
        this.basePaths = basePaths;
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }

    public boolean isEnableFallback() {
        return enableFallback;
    }

    public void setEnableFallback(boolean enableFallback) {
        this.enableFallback = enableFallback;
    }

    public boolean isEnableReloading() {
        return enableReloading;
    }

    public void setEnableReloading(boolean enableReloading) {
        this.enableReloading = enableReloading;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override
    public String toString() {
        return "MessageSourceConfiguration{" +
                "source='" + source + '\'' +
                ", encoding=" + encoding +
                ", basePaths=" + basePaths +
                ", supportedLocales=" + supportedLocales +
                ", defaultLocale=" + defaultLocale +
                ", cacheTtl=" + cacheTtl +
                ", enableFallback=" + enableFallback +
                ", enableReloading=" + enableReloading +
                ", failFast=" + failFast +
                '}';
    }
}
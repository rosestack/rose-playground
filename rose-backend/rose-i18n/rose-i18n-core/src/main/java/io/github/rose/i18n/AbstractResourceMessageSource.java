package io.github.rose.i18n;

import io.github.rose.core.util.Assert;
import io.github.rose.i18n.interpolation.DefaultMessageInterpolator;
import io.github.rose.i18n.interpolation.MessageInterpolator;
import io.github.rose.i18n.util.I18nResourceUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractResourceMessageSource extends AbstractMessageSource implements I18nMessageSource {
    public static final String DEFAULT_LOCATION = "META-INF/i18n";
    public static final String DEFAULT_RESOURCE_NAME = "i18n_messages";

    private volatile Map<Locale, Map<String, String>> localizedResourceMessages = new ConcurrentHashMap<>();

    protected String location;
    protected String basename;
    protected String encoding;
    protected String source;
    protected MessageInterpolator interpolator;

    public AbstractResourceMessageSource(String source) {
        this.source = source;
        this.encoding = "UTF-8";

        setLocaltion(DEFAULT_LOCATION);
        setBasename(DEFAULT_RESOURCE_NAME);
        this.interpolator = new DefaultMessageInterpolator();
    }

    @Override
    public void init() {
        Assert.assertNotNull(this.source, "The 'source' attribute must be assigned before initialization!");
        initializeResource();
    }

    @Override
    public void destroy() {
        localizedResourceMessages.clear();
    }

    @Override
    public String getMessageInternal(String code, Object[] args, Locale locale) {
        for (Locale candidate : I18nResourceUtils.getFallbackLocales(locale)) {
            Map<String, String> messages = localizedResourceMessages.get(candidate);
            if (messages != null && messages.containsKey(code)) {
                String template = messages.get(code);
                return interpolator.interpolate(template, args, candidate);
            }
        }
        return null;
    }

    private void initializeResource() {
        String resourceDir = getResourceDir();
        I18nResourceUtils.loadResourceMessages(resourceDir, basename, getSupportedExtensions(),
                (filename, inputStream) -> handleResourceFile(filename, inputStream));
    }

    public String getResourceDir() {
        return String.format("%s/%s/%s/", location, basename, source);
    }

    private void handleResourceFile(String fileName, InputStream in) {
        Locale locale = I18nResourceUtils.parseLocale(fileName);
        if (locale != null && !localizedResourceMessages.containsKey(locale)) {
            try (Reader reader = new InputStreamReader(in, encoding)) {
                Map<String, String> messages = doLoadMessages(reader);
                if (messages != null && !messages.isEmpty()) {
                    localizedResourceMessages.put(locale, messages);
                }
            } catch (Exception e) {
                // 可选：日志记录
            }
        }
    }

    public Set<Locale> getSupportedLocales() {
        return localizedResourceMessages.keySet();
    }

    public void setLocaltion(String localtion) {
        this.location = localtion;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setInterpolator(MessageInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    protected abstract List<String> getSupportedExtensions();

    protected abstract Map<String, String> doLoadMessages(Reader reader);
} 
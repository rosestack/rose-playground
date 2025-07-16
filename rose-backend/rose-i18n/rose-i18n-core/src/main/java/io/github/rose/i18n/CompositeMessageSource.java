package io.github.rose.i18n;

import io.github.rose.core.collection.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

import jakarta.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * The Composite {@link MessageSource} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractMessageSource
 * @see MessageSource
 * @see ResourceMessageSource
 * @see ReloadableResourceMessageSource
 * @since 1.0.0
 */
public class CompositeMessageSource implements ReloadableResourceMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(CompositeMessageSource.class);

    private List<? extends MessageSource> MessageSources;

    public CompositeMessageSource() {
        this.MessageSources = emptyList();
    }

    public CompositeMessageSource(List<? extends MessageSource> MessageSources) {
        setMessageSources(MessageSources);
    }

    @Override
    public void init() {
        ListUtils.forEach(this.MessageSources, MessageSource::init);
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        for (MessageSource MessageSource : MessageSources) {
            message = MessageSource.getMessage(code, locale, args);
            if (message != null) {
                break;
            }
        }
        return message;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        MessageSource MessageSource = getFirstMessageSource();
        return MessageSource == null ? getDefaultLocale() : MessageSource.getLocale();
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale() {
        MessageSource MessageSource = getFirstMessageSource();
        return MessageSource == null ? ReloadableResourceMessageSource.super.getDefaultLocale() : MessageSource.getLocale();
    }

    @Nonnull
    @Override
    public List<Locale> getSupportedLocales() {
        List<Locale> supportedLocales = new LinkedList<>();
        iterate(MessageSource -> {
            for (Locale locale : MessageSource.getSupportedLocales()) {
                if (!supportedLocales.contains(locale)) {
                    supportedLocales.add(locale);
                }
            }
        });

        return supportedLocales.isEmpty() ? getDefaultSupportedLocales() :
                unmodifiableList(supportedLocales);
    }

    public List<Locale> getDefaultSupportedLocales() {
        return ReloadableResourceMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ReloadableResourceMessageSource.super.getSource();
    }

    public void setMessageSources(List<? extends MessageSource> MessageSources) {
        List<? extends MessageSource> oldMessageSources = this.MessageSources;
        List<MessageSource> newMessageSources = new ArrayList<>(MessageSources);
        OrderComparator.sort(newMessageSources);
        if (oldMessageSources != null) {
            oldMessageSources.clear();
        }
        this.MessageSources = newMessageSources;
        logger.debug("Source '{}' sets MessageSource list, sorted : {}", MessageSources, newMessageSources);
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        iterate(ReloadableResourceMessageSource.class, reloadableResourceMessageSource -> {
            if (reloadableResourceMessageSource.canReload(changedResources)) {
                reloadableResourceMessageSource.reload(changedResources);
            }
        });
    }

    @Override
    public boolean canReload(Iterable<String> changedResources) {
        return true;
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        iterate(ResourceMessageSource.class, resourceMessageSource -> {
            resourceMessageSource.initializeResources(resources);
        });
    }

    @Override
    public Set<String> getInitializeResources() {
        Set<String> resources = new LinkedHashSet<>();
        iterate(ResourceMessageSource.class, resourceMessageSource -> {
            resources.addAll(resourceMessageSource.getInitializeResources());
        });
        return unmodifiableSet(resources);
    }

    @Override
    public Charset getEncoding() {
        return ReloadableResourceMessageSource.super.getEncoding();
    }

    /**
     * Get the read-only list of the composited {@link MessageSource}
     *
     * @return non-null
     */
    @Nonnull
    public List<MessageSource> getMessageSources() {
        return unmodifiableList(MessageSources);
    }

    @Override
    public void destroy() {
        List<? extends MessageSource> MessageSources = this.MessageSources;
        ListUtils.forEach(MessageSources, MessageSource::destroy);
        MessageSources.clear();
    }

    @Override
    public String toString() {
        return "CompositeMessageSource{" +
                "MessageSources=" + MessageSources +
                '}';
    }

    private MessageSource getFirstMessageSource() {
        return this.MessageSources.isEmpty() ? null : this.MessageSources.get(0);
    }

    private <T> void iterate(Class<T> MessageSourceType, Consumer<T> consumer) {
        this.MessageSources.stream()
                .filter(MessageSourceType::isInstance)
                .map(MessageSourceType::cast)
                .forEach(consumer);
    }

    private <T> void iterate(Consumer<MessageSource> consumer) {
        this.MessageSources.forEach(consumer);
    }
}

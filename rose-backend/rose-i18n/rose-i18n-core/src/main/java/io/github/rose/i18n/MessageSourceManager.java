package io.github.rose.i18n;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ServiceLoader;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.CompositeMessageSource;
import io.github.rose.core.lang.Prioritized;

/**
 * MessageSource manager.
 * <p>
 * Supports automatic discovery, composition, dynamic registration, and priority sorting of message sources.
 * Provides unified management, composition, and lifecycle management for message sources.
 */
public final class MessageSourceManager {
    /** List of message sources discovered via SPI */
    private static final List<MessageSource> SOURCES = new CopyOnWriteArrayList<>();
    /** Singleton instance of the composite message source */
    private static volatile CompositeMessageSource compositeInstance;

    private MessageSourceManager() {}

    static {
        reloadAndDiscover();
    }

    /**
     * Initialize all registered message sources.
     */
    public static void initializeSources() {
        for (MessageSource source : SOURCES) {
            source.init();
        }
    }

    /**
     * Destroy all registered message sources.
     */
    public static void destroySources() {
        for (MessageSource source : SOURCES) {
            source.destroy();
        }
    }

    /**
     * Automatically discover and register all MessageSource implementations via SPI.
     */
    public static void reloadAndDiscover() {
        List<MessageSource> discovered = new ArrayList<>();
        ServiceLoader.load(MessageSource.class).forEach(discovered::add);
        discovered.sort(Comparator.comparingInt(Prioritized::getPriority));
        SOURCES.clear();
        SOURCES.addAll(discovered);
        compositeInstance = new CompositeMessageSource(SOURCES);
        initializeSources(); // Auto-initialize
    }

    /**
     * Dynamically register a message source.
     */
    public static void registerSource(MessageSource source) {
        SOURCES.add(source);
        SOURCES.sort(Comparator.comparingInt(Prioritized::getPriority));
        compositeInstance = new CompositeMessageSource(SOURCES);
    }

    /**
     * Dynamically remove a message source.
     */
    public static void unregisterSource(MessageSource source) {
        SOURCES.remove(source);
        compositeInstance = new CompositeMessageSource(SOURCES);
    }

    /**
     * Get all registered message sources (ordered).
     */
    public static List<MessageSource> getRegisteredSources() {
        return Collections.unmodifiableList(SOURCES);
    }

    /**
     * Get the automatically composed message source (priority sorted).
     */
    public static MessageSource getInstance() {
        return compositeInstance;
    }
}

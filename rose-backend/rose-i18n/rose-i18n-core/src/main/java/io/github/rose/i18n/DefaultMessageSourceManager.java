package io.github.rose.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Default implementation of MessageSourceManager
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class DefaultMessageSourceManager implements MessageSourceManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageSourceManager.class);

    private final ConcurrentMap<String, MessageSource> messageSources = new ConcurrentHashMap<>();
    private volatile CompositeMessageSource compositeSource;
    private volatile boolean initialized = false;

    public DefaultMessageSourceManager() {
        // Default constructor
    }

    public DefaultMessageSourceManager(List<MessageSource> initialSources) {
        if (initialSources != null) {
            for (MessageSource source : initialSources) {
                register(source);
            }
        }
    }

    @Override
    public void register(MessageSource messageSource) {
        if (messageSource == null) {
            throw new IllegalArgumentException("MessageSource cannot be null");
        }

        String sourceName = messageSource.getSource();
        MessageSource existing = messageSources.put(sourceName, messageSource);
        
        if (existing != null) {
            logger.warn("Replacing existing message source for '{}'", sourceName);
            try {
                existing.destroy();
            } catch (Exception e) {
                logger.warn("Error destroying replaced message source '{}': {}", sourceName, e.getMessage());
            }
        }

        // Rebuild composite source
        rebuildCompositeSource();
        
        logger.info("Registered message source: {}", sourceName);
    }

    @Override
    public void unregister(String source) {
        if (source == null) {
            return;
        }

        MessageSource removed = messageSources.remove(source);
        if (removed != null) {
            try {
                removed.destroy();
            } catch (Exception e) {
                logger.warn("Error destroying message source '{}': {}", source, e.getMessage());
            }
            
            // Rebuild composite source
            rebuildCompositeSource();
            
            logger.info("Unregistered message source: {}", source);
        }
    }

    @Override
    public Optional<MessageSource> getMessageSource(String source) {
        return Optional.ofNullable(messageSources.get(source));
    }

    @Nonnull
    @Override
    public List<MessageSource> getAllMessageSources() {
        return new ArrayList<>(messageSources.values());
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        ensureInitialized();
        if (compositeSource != null) {
            return compositeSource.getMessage(code, locale, args);
        }
        return null;
    }

    @Override
    public void initAll() {
        logger.info("Initializing all message sources...");
        
        List<String> failedSources = new ArrayList<>();
        for (Map.Entry<String, MessageSource> entry : messageSources.entrySet()) {
            String sourceName = entry.getKey();
            MessageSource source = entry.getValue();
            
            try {
                source.init();
                logger.debug("Initialized message source: {}", sourceName);
            } catch (Exception e) {
                logger.error("Failed to initialize message source '{}': {}", sourceName, e.getMessage(), e);
                failedSources.add(sourceName);
            }
        }

        if (!failedSources.isEmpty()) {
            logger.warn("Failed to initialize {} message sources: {}", failedSources.size(), failedSources);
        }

        // Initialize composite source
        if (compositeSource != null) {
            try {
                compositeSource.init();
            } catch (Exception e) {
                logger.error("Failed to initialize composite source: {}", e.getMessage(), e);
            }
        }

        initialized = true;
        logger.info("Message source manager initialization completed. Active sources: {}", messageSources.size());
    }

    @Override
    public void destroyAll() {
        logger.info("Destroying all message sources...");

        // Destroy composite source first
        if (compositeSource != null) {
            try {
                compositeSource.destroy();
            } catch (Exception e) {
                logger.warn("Error destroying composite source: {}", e.getMessage());
            }
            compositeSource = null;
        }

        // Destroy individual sources
        for (Map.Entry<String, MessageSource> entry : messageSources.entrySet()) {
            String sourceName = entry.getKey();
            MessageSource source = entry.getValue();
            
            try {
                source.destroy();
                logger.debug("Destroyed message source: {}", sourceName);
            } catch (Exception e) {
                logger.warn("Error destroying message source '{}': {}", sourceName, e.getMessage());
            }
        }

        messageSources.clear();
        initialized = false;
        
        logger.info("Message source manager destruction completed");
    }

    /**
     * Get the composite message source
     */
    public CompositeMessageSource getCompositeSource() {
        ensureInitialized();
        return compositeSource;
    }

    /**
     * Check if the manager is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Get the number of registered message sources
     */
    public int getSourceCount() {
        return messageSources.size();
    }

    /**
     * Get statistics about registered sources
     */
    public ManagerStatistics getStatistics() {
        Map<String, String> sourceTypes = new HashMap<>();
        Map<String, Integer> sourcePriorities = new HashMap<>();
        
        for (Map.Entry<String, MessageSource> entry : messageSources.entrySet()) {
            String sourceName = entry.getKey();
            MessageSource source = entry.getValue();
            
            sourceTypes.put(sourceName, source.getClass().getSimpleName());
            sourcePriorities.put(sourceName, source.getPriority());
        }
        
        return new ManagerStatistics(messageSources.size(), sourceTypes, sourcePriorities, initialized);
    }

    /**
     * Rebuild the composite source with current registered sources
     */
    private void rebuildCompositeSource() {
        if (compositeSource != null) {
            try {
                compositeSource.destroy();
            } catch (Exception e) {
                logger.warn("Error destroying old composite source: {}", e.getMessage());
            }
        }

        if (!messageSources.isEmpty()) {
            List<MessageSource> sources = new ArrayList<>(messageSources.values());
            compositeSource = new CompositeMessageSource(sources);
            
            if (initialized) {
                try {
                    compositeSource.init();
                } catch (Exception e) {
                    logger.error("Failed to initialize new composite source: {}", e.getMessage(), e);
                }
            }
        } else {
            compositeSource = null;
        }
    }

    /**
     * Ensure the manager is initialized
     */
    private void ensureInitialized() {
        if (!initialized) {
            initAll();
        }
    }

    /**
     * Manager statistics
     */
    public static class ManagerStatistics {
        private final int totalSources;
        private final Map<String, String> sourceTypes;
        private final Map<String, Integer> sourcePriorities;
        private final boolean initialized;

        public ManagerStatistics(int totalSources, Map<String, String> sourceTypes, 
                               Map<String, Integer> sourcePriorities, boolean initialized) {
            this.totalSources = totalSources;
            this.sourceTypes = Collections.unmodifiableMap(new HashMap<>(sourceTypes));
            this.sourcePriorities = Collections.unmodifiableMap(new HashMap<>(sourcePriorities));
            this.initialized = initialized;
        }

        public int getTotalSources() { return totalSources; }
        public Map<String, String> getSourceTypes() { return sourceTypes; }
        public Map<String, Integer> getSourcePriorities() { return sourcePriorities; }
        public boolean isInitialized() { return initialized; }

        @Override
        public String toString() {
            return String.format("ManagerStatistics{totalSources=%d, initialized=%s, types=%s}", 
                    totalSources, initialized, sourceTypes);
        }
    }

    @Override
    public String toString() {
        return String.format("DefaultMessageSourceManager{sources=%d, initialized=%s}",
                messageSources.size(), initialized);
    }
}
package io.github.rose.i18n.impl;

import io.github.rose.i18n.AbstractResourceMessageSource;
import io.github.rose.i18n.ReloadableResourceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database-based implementation of ResourceMessageSource
 * 
 * <p>This implementation loads internationalization messages from a database,
 * providing enterprise-level features including:
 * <ul>
 *   <li>Dynamic message management through database</li>
 *   <li>Real-time message updates without application restart</li>
 *   <li>Support for complex message hierarchies</li>
 *   <li>Automatic resource reload capabilities</li>
 * </ul>
 * 
 * <p><strong>Database Schema Requirements:</strong></p>
 * <pre>{@code
 * CREATE TABLE i18n_messages (
 *   id BIGINT PRIMARY KEY AUTO_INCREMENT,
 *   message_key VARCHAR(255) NOT NULL,
 *   locale VARCHAR(10) NOT NULL,
 *   message_value TEXT NOT NULL,
 *   source_name VARCHAR(100) NOT NULL,
 *   created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 *   UNIQUE KEY uk_key_locale_source (message_key, locale, source_name),
 *   INDEX idx_source_locale (source_name, locale)
 * );
 * }</pre>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Basic usage with DataSource
 * DatabaseMessageSource source = new DatabaseMessageSource("app", dataSource);
 * source.init();
 * 
 * // With custom table and column names
 * DatabaseMessageSource source = DatabaseMessageSource.builder("app")
 *     .dataSource(dataSource)
 *     .tableName("custom_messages")
 *     .keyColumn("msg_key")
 *     .valueColumn("msg_value")
 *     .build();
 * 
 * // With caching enabled
 * DatabaseMessageSource source = DatabaseMessageSource.builder("app")
 *     .dataSource(dataSource)
 *     .enableCache(true)
 *     .cacheRefreshInterval(Duration.ofMinutes(5))
 *     .build();
 * }</pre>
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class DatabaseMessageSource extends AbstractResourceMessageSource
        implements ReloadableResourceMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMessageSource.class);

    // Default configuration
    public static final String DEFAULT_TABLE_NAME = "i18n_messages";
    public static final String DEFAULT_KEY_COLUMN = "message_key";
    public static final String DEFAULT_VALUE_COLUMN = "message_value";
    public static final String DEFAULT_LOCALE_COLUMN = "locale";
    public static final String DEFAULT_SOURCE_COLUMN = "source_name";
    public static final String DEFAULT_UPDATED_TIME_COLUMN = "updated_time";

    private final DataSource dataSource;
    private final String tableName;
    private final String keyColumn;
    private final String valueColumn;
    private final String localeColumn;
    private final String sourceColumn;
    private final String updatedTimeColumn;
    
    private final Set<String> initializedResources = ConcurrentHashMap.newKeySet();
    private final Map<String, Timestamp> resourceLastModified = new ConcurrentHashMap<>();
    
    // Cache configuration
    private final boolean cacheEnabled;
    private final long cacheRefreshIntervalMs;
    private final Map<String, Map<String, String>> messageCache = new ConcurrentHashMap<>();
    private volatile long lastCacheRefresh = 0;

    /**
     * Constructor with default configuration
     */
    public DatabaseMessageSource(String source, DataSource dataSource) {
        this(source, dataSource, DEFAULT_TABLE_NAME, DEFAULT_KEY_COLUMN, DEFAULT_VALUE_COLUMN,
             DEFAULT_LOCALE_COLUMN, DEFAULT_SOURCE_COLUMN, DEFAULT_UPDATED_TIME_COLUMN,
             false, 300_000L); // 5 minutes default cache refresh
    }

    /**
     * Full constructor for custom configuration
     */
    public DatabaseMessageSource(String source, DataSource dataSource,
                                 String tableName, String keyColumn, String valueColumn,
                                 String localeColumn, String sourceColumn, String updatedTimeColumn,
                                 boolean cacheEnabled, long cacheRefreshIntervalMs) {
        super(source);
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
        this.tableName = tableName;
        this.keyColumn = keyColumn;
        this.valueColumn = valueColumn;
        this.localeColumn = localeColumn;
        this.sourceColumn = sourceColumn;
        this.updatedTimeColumn = updatedTimeColumn;
        this.cacheEnabled = cacheEnabled;
        this.cacheRefreshIntervalMs = cacheRefreshIntervalMs;
    }

    @Override
    protected String getResource(String resourceName) {
        // Resource name for database is the source identifier
        return resourceName;
    }

    @Override
    protected Map<String, String> loadMessages(String resource) {
        String locale = extractLocaleFromResource(resource);
        if (locale == null) {
            logger.warn("Cannot extract locale from resource: {}", resource);
            return Collections.emptyMap();
        }

        Map<String, String> messages = loadMessagesFromDatabase(locale);
        
        if (!messages.isEmpty()) {
            initializedResources.add(resource);
            resourceLastModified.put(resource, getCurrentTimestamp());
            
            if (cacheEnabled) {
                messageCache.put(resource, new HashMap<>(messages));
                lastCacheRefresh = System.currentTimeMillis();
            }
            
            logger.debug("Loaded {} messages from database for locale: {}", messages.size(), locale);
        }
        
        return messages;
    }

    private Map<String, String> loadMessagesFromDatabase(String locale) {
        String sql = String.format(
            "SELECT %s, %s FROM %s WHERE %s = ? AND %s = ? ORDER BY %s",
            keyColumn, valueColumn, tableName, localeColumn, sourceColumn, keyColumn
        );

        Map<String, String> messages = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, locale);
            stmt.setString(2, getSource());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString(keyColumn);
                    String value = rs.getString(valueColumn);
                    messages.put(key, value);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to load messages from database for locale: " + locale, e);
            throw new RuntimeException("Database message loading failed", e);
        }
        
        return messages;
    }

    private Set<String> discoverAvailableLocales() {
        String sql = String.format(
            "SELECT DISTINCT %s FROM %s WHERE %s = ?",
            localeColumn, tableName, sourceColumn
        );

        Set<String> locales = new HashSet<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, getSource());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    locales.add(rs.getString(localeColumn));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to discover available locales from database", e);
            throw new RuntimeException("Database locale discovery failed", e);
        }
        
        return locales;
    }

    @Override
    public boolean canReload(String changedResource) {
        return initializedResources.contains(changedResource);
    }

    @Override
    public void reload(String changedResource) {
        if (!canReload(changedResource)) {
            logger.debug("Resource {} is not initialized, skipping reload", changedResource);
            return;
        }

        logger.info("Reloading database resource: {}", changedResource);
        
        // Check if resource has been modified in database
        if (hasResourceChanged(changedResource)) {
            // Reload the resource
            Map<String, String> newMessages = loadMessages(changedResource);
            logger.info("Reloaded {} messages for resource: {}", newMessages.size(), changedResource);
        } else {
            logger.debug("Resource {} has not changed, skipping reload", changedResource);
        }
    }

    private boolean hasResourceChanged(String resource) {
        if (!cacheEnabled) {
            return true; // Always reload if cache is disabled
        }

        Timestamp lastModified = resourceLastModified.get(resource);
        if (lastModified == null) {
            return true;
        }

        String locale = extractLocaleFromResource(resource);
        if (locale == null) {
            return false;
        }

        return checkDatabaseModification(locale, lastModified);
    }

    private boolean checkDatabaseModification(String locale, Timestamp since) {
        String sql = String.format(
            "SELECT COUNT(*) FROM %s WHERE %s = ? AND %s = ? AND %s > ?",
            tableName, localeColumn, sourceColumn, updatedTimeColumn
        );

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, locale);
            stmt.setString(2, getSource());
            stmt.setTimestamp(3, since);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to check database modification for locale: " + locale, e);
            // Return true to trigger reload on error
            return true;
        }
        
        return false;
    }

    private String extractLocaleFromResource(String resource) {
        // Extract locale from resource name like "i18n_messages_en.properties" -> "en"
        if (resource.startsWith(DEFAULT_RESOURCE_NAME_PREFIX) && resource.endsWith(DEFAULT_RESOURCE_NAME_SUFFIX)) {
            return resource.substring(DEFAULT_RESOURCE_NAME_PREFIX.length(), 
                                    resource.length() - DEFAULT_RESOURCE_NAME_SUFFIX.length());
        }
        
        return null;
    }

    private Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Builder pattern for creating DatabaseMessageSource instances
     */
    public static Builder builder(String source) {
        return new Builder(source);
    }

    public static class Builder {
        private final String source;
        private DataSource dataSource;
        private String tableName = DEFAULT_TABLE_NAME;
        private String keyColumn = DEFAULT_KEY_COLUMN;
        private String valueColumn = DEFAULT_VALUE_COLUMN;
        private String localeColumn = DEFAULT_LOCALE_COLUMN;
        private String sourceColumn = DEFAULT_SOURCE_COLUMN;
        private String updatedTimeColumn = DEFAULT_UPDATED_TIME_COLUMN;
        private boolean cacheEnabled = false;
        private long cacheRefreshIntervalMs = 300_000L; // 5 minutes

        private Builder(String source) {
            this.source = Objects.requireNonNull(source, "Source cannot be null");
        }

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder keyColumn(String keyColumn) {
            this.keyColumn = keyColumn;
            return this;
        }

        public Builder valueColumn(String valueColumn) {
            this.valueColumn = valueColumn;
            return this;
        }

        public Builder localeColumn(String localeColumn) {
            this.localeColumn = localeColumn;
            return this;
        }

        public Builder sourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
            return this;
        }

        public Builder updatedTimeColumn(String updatedTimeColumn) {
            this.updatedTimeColumn = updatedTimeColumn;
            return this;
        }

        public Builder enableCache(boolean cacheEnabled) {
            this.cacheEnabled = cacheEnabled;
            return this;
        }

        public Builder cacheRefreshInterval(java.time.Duration duration) {
            this.cacheRefreshIntervalMs = duration.toMillis();
            return this;
        }

        public DatabaseMessageSource build() {
            Objects.requireNonNull(dataSource, "DataSource must be provided");
            
            return new DatabaseMessageSource(source, dataSource, tableName,
                    keyColumn, valueColumn, localeColumn, sourceColumn, updatedTimeColumn,
                    cacheEnabled, cacheRefreshIntervalMs);
        }
    }
}
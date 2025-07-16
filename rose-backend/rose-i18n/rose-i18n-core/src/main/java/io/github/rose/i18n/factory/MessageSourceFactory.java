package io.github.rose.i18n.factory;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.config.MessageSourceConfiguration;
import io.github.rose.i18n.impl.ClassPathPropertiesMessageSource;
import io.github.rose.i18n.impl.ClassPathYamlResourceMessageSource;

import java.util.Objects;

/**
 * Factory for creating MessageSource instances
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class MessageSourceFactory {

    /**
     * Message source type enumeration
     */
    public enum MessageSourceType {
        PROPERTIES("properties"),
        YAML("yaml"),
        AUTO("auto");

        private final String type;

        MessageSourceType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static MessageSourceType fromString(String type) {
            for (MessageSourceType sourceType : values()) {
                if (sourceType.type.equalsIgnoreCase(type)) {
                    return sourceType;
                }
            }
            throw new IllegalArgumentException("Unknown message source type: " + type);
        }
    }

    /**
     * Create a MessageSource with default configuration
     * 
     * @param source the source name
     * @param type the message source type
     * @return MessageSource instance
     */
    public static MessageSource create(String source, MessageSourceType type) {
        return create(source, type, null);
    }

    /**
     * Create a MessageSource with custom configuration
     * 
     * @param source the source name
     * @param type the message source type
     * @param config the configuration (optional)
     * @return MessageSource instance
     */
    public static MessageSource create(String source, MessageSourceType type, MessageSourceConfiguration config) {
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(type, "MessageSourceType cannot be null");

        MessageSource messageSource;

        switch (type) {
            case PROPERTIES:
                messageSource = createPropertiesMessageSource(source, config);
                break;
            case YAML:
                messageSource = createYamlMessageSource(source, config);
                break;
            case AUTO:
                messageSource = createAutoDetectMessageSource(source, config);
                break;
            default:
                throw new IllegalArgumentException("Unsupported message source type: " + type);
        }

        // Apply configuration if provided
        if (config != null) {
            applyConfiguration(messageSource, config);
        }

        return messageSource;
    }

    /**
     * Create a Properties-based message source
     */
    private static MessageSource createPropertiesMessageSource(String source, MessageSourceConfiguration config) {
        ClassPathPropertiesMessageSource messageSource;
        
        if (config != null && config.getBasePaths() != null) {
            // Custom base paths not supported in current implementation
            // This is a placeholder for future enhancement
            messageSource = new ClassPathPropertiesMessageSource(source);
        } else {
            messageSource = new ClassPathPropertiesMessageSource(source);
        }
        
        return messageSource;
    }

    /**
     * Create a YAML-based message source
     */
    private static MessageSource createYamlMessageSource(String source, MessageSourceConfiguration config) {
        ClassPathYamlResourceMessageSource messageSource;
        
        if (config != null && config.getBasePaths() != null) {
            String[] basePaths = config.getBasePaths().toArray(new String[0]);
            messageSource = new ClassPathYamlResourceMessageSource(source,
                    Thread.currentThread().getContextClassLoader(), basePaths);
        } else {
            messageSource = new ClassPathYamlResourceMessageSource(source);
        }
        
        return messageSource;
    }

    /**
     * Create message source with auto-detection of format
     */
    private static MessageSource createAutoDetectMessageSource(String source, MessageSourceConfiguration config) {
        // Try YAML first, then fallback to Properties
        // This is a simple implementation - could be enhanced with actual resource detection
        try {
            MessageSource yamlSource = createYamlMessageSource(source, config);
            yamlSource.init();
            return yamlSource;
        } catch (Exception e) {
            // Fallback to Properties
            return createPropertiesMessageSource(source, config);
        }
    }

    /**
     * Apply configuration to message source
     */
    private static void applyConfiguration(MessageSource messageSource, MessageSourceConfiguration config) {
        // Apply supported locales and default locale if the message source supports it
        // This is a simplified implementation - real implementation would use reflection
        // or interface-based configuration
        
        // For now, we'll just log the configuration
        // In a real implementation, you would cast to appropriate types and apply settings
    }

    /**
     * Create a builder for fluent configuration
     */
    public static MessageSourceBuilder builder(String source) {
        return new MessageSourceBuilder(source);
    }

    /**
     * Builder pattern for creating message sources
     */
    public static class MessageSourceBuilder {
        private final String source;
        private MessageSourceType type = MessageSourceType.AUTO;
        private MessageSourceConfiguration config = new MessageSourceConfiguration();

        private MessageSourceBuilder(String source) {
            this.source = source;
            this.config.setSource(source);
        }

        public MessageSourceBuilder type(MessageSourceType type) {
            this.type = type;
            return this;
        }

        public MessageSourceBuilder type(String type) {
            this.type = MessageSourceType.fromString(type);
            return this;
        }

        public MessageSourceBuilder configuration(MessageSourceConfiguration config) {
            this.config = config;
            return this;
        }

        public MessageSource build() {
            return MessageSourceFactory.create(source, type, config);
        }
    }
}
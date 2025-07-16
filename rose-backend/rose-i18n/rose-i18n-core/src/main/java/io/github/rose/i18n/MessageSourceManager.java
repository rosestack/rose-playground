package io.github.rose.i18n;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service Message Source Manager for centralized message source management
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public interface MessageSourceManager {

    /**
     * Register a message source
     * 
     * @param messageSource the message source to register
     */
    void register(MessageSource messageSource);

    /**
     * Unregister a message source
     * 
     * @param source the source name to unregister
     */
    void unregister(String source);

    /**
     * Get message source by source name
     * 
     * @param source the source name
     * @return Optional of MessageSource
     */
    Optional<MessageSource> getMessageSource(String source);

    /**
     * Get all registered message sources
     * 
     * @return list of all message sources
     */
    @Nonnull
    List<MessageSource> getAllMessageSources();

    /**
     * Get message from all registered sources
     * 
     * @param code message code
     * @param locale locale
     * @param args message arguments
     * @return the resolved message, null if not found
     */
    String getMessage(String code, Locale locale, Object... args);

    /**
     * Initialize all registered message sources
     */
    void initAll();

    /**
     * Destroy all registered message sources
     */
    void destroyAll();
}
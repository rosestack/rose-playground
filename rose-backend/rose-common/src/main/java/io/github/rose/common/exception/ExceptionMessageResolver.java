package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.spring.SpringBeans;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Exception message resolver utility class for internationalization support.
 *
 * This class provides static methods to resolve exception messages using Spring's MessageSource
 * for internationalization (i18n) support. It handles both generic message resolution and
 * specialized BusinessException message resolution.
 *
 * Key features:
 * - Lazy loading and caching of MessageSource bean
 * - Thread-safe singleton pattern for MessageSource access
 * - Fallback mechanism when MessageSource is unavailable
 * - Support for message parameters and default messages
 * - Specialized handling for BusinessException instances
 *
 * Usage patterns:
 * - resolveMessage(messageCode) - Simple message resolution
 * - resolveMessage(messageCode, args) - Message with parameters
 * - resolveMessage(businessException) - BusinessException specific resolution
 *
 * Thread Safety:
 * This class is thread-safe. The MessageSource field uses volatile keyword and
 * double-checked locking pattern for safe lazy initialization.
 *
 * Dependencies:
 * - Spring Framework (MessageSource, LocaleContextHolder)
 * - SpringBeans utility for bean access
 * - BusinessException for specialized exception handling
 *
 * @author Rose Framework
 * @since 1.0.0
 */
public class ExceptionMessageResolver {

    /**
     * Cached MessageSource instance for message resolution.
     * Uses volatile keyword to ensure thread-safe lazy initialization.
     * Will be null if Spring context is not available or MessageSource bean is not found.
     */
    private static volatile MessageSource messageSource;

    /**
     * Resolves a message using the provided message code with current locale.
     *
     * This is a convenience method that uses the current thread's locale from
     * LocaleContextHolder and no message arguments.
     *
     * @param messageCode The message code to resolve from message source
     * @return The resolved message string, or null if message code is empty or resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     */
    public static String resolveMessage(String messageCode) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), null);
    }

    /**
     * Resolves a message using the provided message code and default message with current locale.
     *
     * This method provides a fallback mechanism by accepting a default message that will be
     * returned if the message code cannot be resolved.
     *
     * @param messageCode The message code to resolve from message source
     * @param defaultMessage The default message to return if resolution fails
     * @return The resolved message string, or defaultMessage if resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     */
    public static String resolveMessage(String messageCode, String defaultMessage) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), null);
    }

    /**
     * Resolves a message using the provided message code and arguments with current locale.
     *
     * This method allows parameterized messages where placeholders in the message template
     * are replaced with the provided arguments. Uses the current thread's locale.
     *
     * Example:
     * - Message template: "User {0} not found with ID {1}"
     * - Args: ["john", 123]
     * - Result: "User john not found with ID 123"
     *
     * @param messageCode The message code to resolve from message source
     * @param args Array of arguments to substitute into the message template
     * @return The resolved and parameterized message string, or null if resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     */
    public static String resolveMessage(String messageCode, Object[] args) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Resolves a message using the provided message code, default message, and arguments with current locale.
     *
     * This method combines parameterized message resolution with fallback support.
     * If the message code cannot be resolved, the default message is returned.
     *
     * @param messageCode The message code to resolve from message source
     * @param defaultMessage The default message to return if resolution fails
     * @param args Array of arguments to substitute into the message template
     * @return The resolved and parameterized message string, or defaultMessage if resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     */
    public static String resolveMessage(String messageCode, String defaultMessage, Object[] args) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Core message resolution method with full parameter control.
     *
     * This is the main method that all other overloaded methods delegate to.
     * It provides complete control over message resolution including locale,
     * default message, and message arguments.
     *
     * Resolution process:
     * 1. Check if messageCode is not empty
     * 2. Attempt to get MessageSource (lazy loaded and cached)
     * 3. Try to resolve message using MessageSource.getMessage()
     * 4. If resolution fails or MessageSource unavailable, return defaultMessage
     *
     * Error handling:
     * - Silently handles MessageSource unavailability (Spring context not ready)
     * - Catches and ignores message resolution exceptions
     * - Always returns a result (message or defaultMessage or null)
     *
     * @param messageCode The message code to resolve from message source
     * @param defaultMessage The default message to return if resolution fails (can be null)
     * @param locale The locale to use for message resolution
     * @param args Array of arguments to substitute into the message template (can be null)
     * @return The resolved message string, defaultMessage if resolution fails, or null if both fail
     *
     * @see MessageSource#getMessage(String, Object[], Locale)
     */
    public static String resolveMessage(String messageCode, String defaultMessage, Locale locale, Object[] args) {
        String message = null;

        // Only attempt resolution if messageCode is provided
        if (!StringUtils.isEmpty(messageCode)) {
            MessageSource msgSource = getMessageSource();
            if (msgSource != null) {
                try {
                    message = msgSource.getMessage(messageCode, args, locale);
                } catch (Exception e) {
                    // Message resolution failed, continue with fallback handling
                    // This can happen if:
                    // - Message code not found in message source
                    // - Invalid message format
                    // - Argument substitution errors
                }
            }
        }

        // Fallback to default message if resolution failed
        if (message == null) {
            message = defaultMessage;
        }

        return message;
    }

    /**
     * Specialized message resolution for BusinessException instances using current locale.
     *
     * This method provides convenient handling of BusinessException objects by
     * extracting their message properties and delegating to the appropriate resolution method.
     * Uses the current thread's locale from LocaleContextHolder.
     *
     * @param exception The BusinessException instance to resolve message for
     * @return The resolved message string based on exception properties
     *
     * @throws NullPointerException if exception is null
     * @see #resolveMessage(BusinessException, Locale)
     */
    public static String resolveMessage(BusinessException exception) {
        return resolveMessage(exception, LocaleContextHolder.getLocale());
    }

    /**
     * Specialized message resolution for BusinessException instances with specific locale.
     *
     * This method handles BusinessException objects intelligently based on their
     * internationalization requirements:
     *
     * Non-internationalized exceptions:
     * - Returns defaultMessage if available
     * - Falls back to exception.getMessage() if defaultMessage is null
     *
     * Internationalized exceptions:
     * - Uses messageCode for resolution via MessageSource
     * - Uses defaultMessage as fallback
     * - Applies messageArgs for parameter substitution
     * - Uses specified locale for resolution
     *
     * Decision logic:
     * The method checks exception.isNeedsInternationalization() to determine
     * whether to perform i18n resolution or return the message directly.
     *
     * @param exception The BusinessException instance to resolve message for
     * @param locale The locale to use for message resolution
     * @return The resolved message string based on exception properties and locale
     *
     * @throws NullPointerException if exception is null
     * @see BusinessException#isNeedsInternationalization()
     * @see BusinessException#getMessageCode()
     * @see BusinessException#getDefaultMessage()
     * @see BusinessException#getMessageArgs()
     */
    public static String resolveMessage(BusinessException exception, Locale locale) {
        // Handle non-internationalized exceptions directly
        if (!exception.isNeedsInternationalization()) {
            String defaultMessage = exception.getDefaultMessage();
            return defaultMessage != null ? defaultMessage : exception.getMessage();
        }

        // Handle internationalized exceptions through message resolution
        return resolveMessage(
                exception.getMessageCode(),
                exception.getDefaultMessage(),
                locale,
                exception.getMessageArgs()
        );
    }

    /**
     * Retrieves the MessageSource bean with lazy loading and caching support.
     *
     * This method implements the double-checked locking pattern for thread-safe
     * lazy initialization of the MessageSource. The MessageSource is cached
     * after first successful retrieval to avoid repeated Spring context lookups.
     *
     * Initialization process:
     * 1. Check if messageSource is already cached (first check - no synchronization)
     * 2. If null, enter synchronized block
     * 3. Double-check if messageSource is still null (second check - synchronized)
     * 4. Attempt to retrieve MessageSource bean from Spring context
     * 5. Cache the result for future use
     *
     * Error handling:
     * - Returns null if Spring context is not initialized
     * - Returns null if MessageSource bean is not found
     * - Silently handles all exceptions during bean retrieval
     *
     * Thread safety:
     * - Uses volatile field for messageSource
     * - Implements double-checked locking pattern
     * - Synchronizes on class object for initialization
     *
     * @return The cached MessageSource instance, or null if unavailable
     *
     * @see SpringBeans#getBean(Class)
     */
    private static MessageSource getMessageSource() {
        // First check (no locking)
        if (messageSource == null) {
            // Enter critical section for initialization
            synchronized (ExceptionMessageResolver.class) {
                // Second check (with locking) - double-checked locking pattern
                if (messageSource == null) {
                    try {
                        messageSource = SpringBeans.getBean(MessageSource.class);
                    } catch (Exception e) {
                        // Spring context not initialized or MessageSource bean not found
                        // Return null to indicate MessageSource is unavailable
                        return null;
                    }
                }
            }
        }
        return messageSource;
    }

    /**
     * Clears the cached MessageSource instance.
     *
     * This method is primarily intended for testing purposes to reset the
     * MessageSource cache and force re-initialization on next access.
     *
     * Use cases:
     * - Unit testing with different MessageSource configurations
     * - Integration testing with Spring context reloading
     * - Testing fallback behavior when MessageSource is unavailable
     *
     * Thread safety:
     * This method is not synchronized, so it should only be called from
     * test environments where concurrent access is controlled.
     *
     * @see #setMessageSource(MessageSource)
     */
    public static void clearCache() {
        messageSource = null;
    }

    /**
     * Sets the MessageSource instance directly (primarily for testing).
     *
     * This method allows direct injection of a MessageSource instance,
     * bypassing the normal Spring bean lookup mechanism. It's primarily
     * intended for testing scenarios where you want to provide a mock
     * or custom MessageSource implementation.
     *
     * Use cases:
     * - Unit testing with mock MessageSource
     * - Testing with custom MessageSource implementations
     * - Testing error scenarios with null MessageSource
     *
     * Thread safety:
     * This method is not synchronized, so it should only be called from
     * test environments where concurrent access is controlled.
     *
     * @param messageSource The MessageSource instance to use (can be null)
     *
     * @see #clearCache()
     */
    public static void setMessageSource(MessageSource messageSource) {
        ExceptionMessageResolver.messageSource = messageSource;
    }
}

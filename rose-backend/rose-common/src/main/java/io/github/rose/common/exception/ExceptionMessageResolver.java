package io.github.rose.common.exception;

import io.github.rose.core.exception.BusinessException;
import io.github.rose.core.spring.SpringBeans;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Exception message resolver utility class providing internationalization support for exception messages.
 *
 * This utility class serves as a centralized message resolution system that integrates with Spring's
 * MessageSource infrastructure to provide internationalized exception messages. It implements a robust
 * message resolution strategy with multiple fallback mechanisms to ensure reliable message delivery
 * even when the internationalization infrastructure is unavailable.
 *
 * <h3>Core Responsibilities:</h3>
 * <ul>
 *   <li>Resolve internationalized messages using Spring MessageSource</li>
 *   <li>Provide fallback mechanisms when MessageSource is unavailable</li>
 *   <li>Handle parameterized messages with argument substitution</li>
 *   <li>Offer specialized support for BusinessException instances</li>
 *   <li>Maintain thread-safe access to MessageSource with lazy loading</li>
 * </ul>
 *
 * <h3>Architecture Design:</h3>
 * The class follows a static utility pattern with thread-safe lazy initialization of the MessageSource.
 * It uses the double-checked locking pattern to ensure safe concurrent access while minimizing
 * synchronization overhead. The MessageSource is cached after first successful retrieval to avoid
 * repeated Spring context lookups.
 *
 * <h3>Message Resolution Strategy:</h3>
 * <ol>
 *   <li>Attempt to resolve message using Spring MessageSource with provided locale</li>
 *   <li>If MessageSource is unavailable or resolution fails, return default message</li>
 *   <li>If no default message is provided, return null</li>
 * </ol>
 *
 * <h3>Thread Safety:</h3>
 * This class is fully thread-safe. The MessageSource field uses volatile semantics and
 * double-checked locking pattern for safe lazy initialization. All public methods are
 * stateless and can be safely called from multiple threads concurrently.
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Simple message resolution
 * String message = ExceptionMessageResolver.resolveMessage("user.not.found");
 *
 * // Message with parameters
 * String message = ExceptionMessageResolver.resolveMessage("user.not.found",
 *     new Object[]{"john", 123});
 *
 * // Message with default fallback
 * String message = ExceptionMessageResolver.resolveMessage("user.not.found",
 *     "User not found");
 *
 * // BusinessException resolution
 * String message = ExceptionMessageResolver.resolveMessage(businessException);
 * }</pre>
 *
 * <h3>Dependencies:</h3>
 * <ul>
 *   <li>Spring Framework (MessageSource, LocaleContextHolder, StringUtils)</li>
 *   <li>SpringBeans utility for Spring context access</li>
 *   <li>BusinessException for specialized exception handling</li>
 * </ul>
 *
 * @author Rose Framework Team
 * @since 1.0.0
 * @see MessageSource
 * @see BusinessException
 * @see LocaleContextHolder
 */
public class ExceptionMessageResolver {

    /**
     * Cached MessageSource instance for internationalized message resolution.
     *
     * This field uses volatile semantics to ensure thread-safe lazy initialization
     * using the double-checked locking pattern. The MessageSource is retrieved from
     * the Spring application context on first access and cached for subsequent use.
     *
     * The field will remain null if:
     * - Spring application context is not initialized
     * - MessageSource bean is not available in the context
     * - An exception occurs during bean retrieval
     *
     * @see #getMessageSource()
     */
    private static volatile MessageSource messageSource;

    /**
     * Resolves an internationalized message using the specified message code with current thread locale.
     *
     * This convenience method provides the simplest way to resolve a message code to its
     * internationalized representation. It uses the current thread's locale as determined
     * by Spring's LocaleContextHolder and does not support message parameters.
     *
     * <p>The method delegates to the core resolution method with null values for default
     * message and arguments, making it suitable for scenarios where:
     * <ul>
     *   <li>The message code is guaranteed to exist in the message source</li>
     *   <li>No parameterization is required</li>
     *   <li>Null return value is acceptable when resolution fails</li>
     * </ul>
     *
     * @param messageCode The message code to resolve from the configured MessageSource.
     *                   Must not be null or empty for successful resolution.
     * @return The resolved internationalized message string, or null if the message code
     *         is empty, MessageSource is unavailable, or resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     * @see LocaleContextHolder#getLocale()
     */
    public static String resolveMessage(String messageCode) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), null);
    }

    /**
     * Resolves an internationalized message with fallback support using current thread locale.
     *
     * This method extends the basic message resolution by providing a fallback mechanism
     * through the default message parameter. If the message code cannot be resolved from
     * the MessageSource, the default message is returned instead of null.
     *
     * <p>This approach is particularly useful in production environments where displaying
     * a meaningful message to users is more important than strict internationalization
     * compliance. The default message serves as a safety net when:
     * <ul>
     *   <li>MessageSource is temporarily unavailable</li>
     *   <li>Message code is missing from the message bundle</li>
     *   <li>Locale-specific message is not available</li>
     * </ul>
     *
     * @param messageCode The message code to resolve from the configured MessageSource.
     *                   Can be null or empty, in which case defaultMessage is returned.
     * @param defaultMessage The fallback message to return if resolution fails.
     *                      Can be null if no fallback is desired.
     * @return The resolved internationalized message string, or defaultMessage if
     *         resolution fails, or null if both resolution and fallback fail
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     * @see LocaleContextHolder#getLocale()
     */
    public static String resolveMessage(String messageCode, String defaultMessage) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), null);
    }

    /**
     * Resolves a parameterized internationalized message using current thread locale.
     *
     * This method enables the resolution of messages that contain placeholders for dynamic
     * content substitution. The message template retrieved from the MessageSource can contain
     * numbered placeholders (e.g., {0}, {1}, {2}) that are replaced with the corresponding
     * arguments from the provided array.
     *
     * <p>Parameterized messages are essential for creating dynamic, context-aware error messages
     * and notifications. The MessageSource implementation (typically ResourceBundleMessageSource)
     * handles the placeholder substitution using MessageFormat internally.
     *
     * <p><strong>Example Usage:</strong>
     * <pre>{@code
     * // Message template in properties file: "User {0} not found with ID {1}"
     * Object[] args = {"john", 123};
     * String result = resolveMessage("user.not.found", args);
     * // Result: "User john not found with ID 123"
     * }</pre>
     *
     * <p><strong>Argument Handling:</strong>
     * <ul>
     *   <li>Arguments are processed in order (0-indexed)</li>
     *   <li>Null arguments are converted to "null" string</li>
     *   <li>Missing arguments result in placeholder retention</li>
     *   <li>Extra arguments are ignored</li>
     * </ul>
     *
     * @param messageCode The message code to resolve from the configured MessageSource.
     *                   Must not be null or empty for successful resolution.
     * @param args Array of arguments to substitute into the message template placeholders.
     *            Can be null or empty if no substitution is needed.
     * @return The resolved and parameterized message string, or null if the message code
     *         is empty, MessageSource is unavailable, or resolution fails
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     * @see java.text.MessageFormat
     * @see LocaleContextHolder#getLocale()
     */
    public static String resolveMessage(String messageCode, Object[] args) {
        return resolveMessage(messageCode, null, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Resolves a parameterized internationalized message with fallback support using current thread locale.
     *
     * This method combines the power of parameterized message resolution with the safety
     * of fallback message support. It represents the most commonly used pattern for
     * exception message resolution in production applications where both dynamic content
     * and reliability are important.
     *
     * <p>The method first attempts to resolve the message code with parameter substitution.
     * If this fails for any reason (MessageSource unavailable, message code not found,
     * parameter substitution errors), it falls back to returning the default message.
     *
     * <p><strong>Use Cases:</strong>
     * <ul>
     *   <li>Exception messages with dynamic content (user names, IDs, timestamps)</li>
     *   <li>Validation error messages with field names and values</li>
     *   <li>Business rule violation messages with context information</li>
     *   <li>System notifications with variable parameters</li>
     * </ul>
     *
     * <p><strong>Fallback Strategy:</strong>
     * The default message is returned as-is without parameter substitution. This ensures
     * that even if the internationalization system fails, users still receive meaningful
     * feedback. For parameterized default messages, consider using FormatUtils or
     * String.format() before passing the default message.
     *
     * @param messageCode The message code to resolve from the configured MessageSource.
     *                   Can be null or empty, in which case defaultMessage is returned.
     * @param defaultMessage The fallback message to return if resolution fails.
     *                      This message is not parameterized and is returned as-is.
     * @param args Array of arguments to substitute into the message template placeholders.
     *            Only used for MessageSource resolution, not for default message.
     * @return The resolved and parameterized message string, or defaultMessage if
     *         resolution fails, or null if both resolution and fallback fail
     *
     * @see #resolveMessage(String, String, Locale, Object[])
     * @see java.text.MessageFormat
     * @see LocaleContextHolder#getLocale()
     */
    public static String resolveMessage(String messageCode, String defaultMessage, Object[] args) {
        return resolveMessage(messageCode, defaultMessage, LocaleContextHolder.getLocale(), args);
    }

    /**
     * Core message resolution method providing complete control over all resolution parameters.
     *
     * This is the fundamental method that all other overloaded convenience methods delegate to.
     * It implements the complete message resolution algorithm with full parameter control,
     * including custom locale specification, argument substitution, and fallback handling.
     *
     * <p><strong>Resolution Algorithm:</strong>
     * <ol>
     *   <li><strong>Input Validation:</strong> Check if messageCode is not null or empty</li>
     *   <li><strong>MessageSource Acquisition:</strong> Retrieve cached MessageSource or load lazily</li>
     *   <li><strong>Message Resolution:</strong> Attempt to resolve message using MessageSource.getMessage()</li>
     *   <li><strong>Parameter Substitution:</strong> Apply argument substitution if resolution succeeds</li>
     *   <li><strong>Fallback Handling:</strong> Return defaultMessage if resolution fails at any step</li>
     * </ol>
     *
     * <p><strong>Error Handling Strategy:</strong>
     * The method implements a defensive programming approach where all potential failure points
     * are handled gracefully without throwing exceptions. This ensures that the message resolution
     * system never becomes a source of additional errors in exception handling scenarios.
     *
     * <p><strong>Failure Scenarios Handled:</strong>
     * <ul>
     *   <li>Spring application context not initialized</li>
     *   <li>MessageSource bean not available in context</li>
     *   <li>Message code not found in message bundles</li>
     *   <li>Invalid message format or syntax errors</li>
     *   <li>Argument substitution failures (type mismatches, missing arguments)</li>
     *   <li>Locale-specific message bundle unavailable</li>
     * </ul>
     *
     * <p><strong>Performance Considerations:</strong>
     * <ul>
     *   <li>MessageSource is cached after first successful retrieval</li>
     *   <li>Empty or null message codes are handled early to avoid unnecessary processing</li>
     *   <li>Exception handling is lightweight with no logging to avoid performance impact</li>
     * </ul>
     *
     * @param messageCode The message code to resolve from the configured MessageSource.
     *                   If null or empty, resolution is skipped and defaultMessage is returned.
     * @param defaultMessage The fallback message to return if resolution fails.
     *                      Can be null if no fallback is desired.
     * @param locale The locale to use for message resolution. Must not be null.
     *              Determines which language-specific message bundle is used.
     * @param args Array of arguments to substitute into the message template placeholders.
     *            Can be null or empty if no substitution is needed.
     * @return The resolved and localized message string with parameter substitution applied,
     *         or defaultMessage if resolution fails, or null if both resolution and fallback fail
     *
     * @see MessageSource#getMessage(String, Object[], Locale)
     * @see StringUtils#isEmpty(Object)
     * @see #getMessageSource()
     */
    public static String resolveMessage(String messageCode, String defaultMessage, Locale locale, Object[] args) {
        String message = null;

        if (!StringUtils.isEmpty(messageCode)) {
            MessageSource msgSource = getMessageSource();

            if (msgSource != null) {
                try {
                    message = msgSource.getMessage(messageCode, args, locale);
                } catch (Exception e) {
                    // Message resolution failed - continue with fallback handling
                    // Common failure scenarios:
                    // - NoSuchMessageException: message code not found in bundle
                    // - IllegalArgumentException: invalid message format
                    // - ClassCastException: argument type mismatch
                    // - Any other runtime exception during resolution
                    //
                    // Note: We intentionally do not log this exception to avoid
                    // cluttering logs with expected resolution failures
                }
            }
        }

        if (message == null) {
            message = defaultMessage;
        }

        return message;
    }

    /**
     * Resolves the message for a BusinessException instance using the current thread's locale.
     *
     * This convenience method provides specialized handling for BusinessException objects,
     * which contain rich metadata about their internationalization requirements and message
     * parameters. It automatically extracts the relevant properties from the exception and
     * delegates to the appropriate resolution strategy.
     *
     * <p>The method uses the current thread's locale as determined by Spring's LocaleContextHolder,
     * making it suitable for web applications where the locale is typically set per request
     * based on user preferences or Accept-Language headers.
     *
     * <p><strong>BusinessException Integration:</strong>
     * BusinessException instances contain several properties that influence message resolution:
     * <ul>
     *   <li><code>needsInternationalization</code> - determines resolution strategy</li>
     *   <li><code>messageCode</code> - the i18n message key</li>
     *   <li><code>defaultMessage</code> - fallback message</li>
     *   <li><code>messageArgs</code> - parameters for message substitution</li>
     * </ul>
     *
     * @param exception The BusinessException instance to resolve the message for.
     *                 Must not be null.
     * @return The resolved message string based on the exception's properties and
     *         current thread locale
     *
     * @throws NullPointerException if exception is null
     * @see #resolveMessage(BusinessException, Locale)
     * @see BusinessException
     * @see LocaleContextHolder#getLocale()
     */
    public static String resolveMessage(BusinessException exception) {
        return resolveMessage(exception, LocaleContextHolder.getLocale());
    }

    /**
     * Resolves the message for a BusinessException instance using a specific locale.
     *
     * This method implements intelligent message resolution for BusinessException objects
     * based on their internationalization configuration. It provides two distinct resolution
     * strategies depending on whether the exception requires internationalization support.
     *
     * <p><strong>Resolution Strategy Decision:</strong>
     * The method examines the exception's <code>needsInternationalization</code> flag to
     * determine the appropriate resolution approach:
     *
     * <p><strong>Non-Internationalized Exceptions:</strong>
     * When <code>needsInternationalization</code> is false, the method bypasses the
     * MessageSource resolution entirely and returns messages directly from the exception:
     * <ol>
     *   <li>Return <code>defaultMessage</code> if it's not null</li>
     *   <li>Fall back to <code>exception.getMessage()</code> if defaultMessage is null</li>
     * </ol>
     *
     * <p><strong>Internationalized Exceptions:</strong>
     * When <code>needsInternationalization</code> is true, the method performs full
     * internationalization resolution using the exception's metadata:
     * <ol>
     *   <li>Extract <code>messageCode</code> for MessageSource lookup</li>
     *   <li>Use <code>defaultMessage</code> as fallback</li>
     *   <li>Apply <code>messageArgs</code> for parameter substitution</li>
     *   <li>Resolve using the specified locale</li>
     * </ol>
     *
     * <p><strong>Performance Optimization:</strong>
     * The dual-strategy approach optimizes performance by avoiding unnecessary MessageSource
     * lookups for exceptions that don't require internationalization. This is particularly
     * beneficial for internal system exceptions or debug messages that are not user-facing.
     *
     * <p><strong>Use Cases:</strong>
     * <ul>
     *   <li>User-facing validation errors (internationalized)</li>
     *   <li>Business rule violations (internationalized)</li>
     *   <li>System errors with user messages (internationalized)</li>
     *   <li>Internal debugging exceptions (non-internationalized)</li>
     *   <li>Technical errors for developers (non-internationalized)</li>
     * </ul>
     *
     * @param exception The BusinessException instance to resolve the message for.
     *                 Must not be null and should contain valid message metadata.
     * @param locale The locale to use for internationalized message resolution.
     *              Must not be null. Ignored for non-internationalized exceptions.
     * @return The resolved message string based on the exception's properties and
     *         internationalization requirements
     *
     * @throws NullPointerException if exception or locale is null
     * @see BusinessException#isNeedsInternationalization()
     * @see BusinessException#getMessageCode()
     * @see BusinessException#getDefaultMessage()
     * @see BusinessException#getMessageArgs()
     * @see #resolveMessage(String, String, Locale, Object[])
     */
    public static String resolveMessage(BusinessException exception, Locale locale) {
        if (!exception.isNeedsInternationalization()) {
            String defaultMessage = exception.getDefaultMessage();
            return defaultMessage != null ? defaultMessage : exception.getMessage();
        }

        return resolveMessage(
                exception.getMessageCode(),      // i18n message key
                exception.getDefaultMessage(),   // fallback message
                locale,                          // target locale
                exception.getMessageArgs()      // substitution parameters
        );
    }

    /**
     * Retrieves the MessageSource bean using lazy loading with thread-safe caching.
     *
     * This method implements the double-checked locking pattern to provide thread-safe
     * lazy initialization of the MessageSource while minimizing synchronization overhead.
     * The MessageSource is retrieved from the Spring application context on first access
     * and cached for all subsequent calls.
     *
     * <p><strong>Double-Checked Locking Pattern:</strong>
     * This implementation uses the classic double-checked locking pattern which provides
     * optimal performance characteristics:
     * <ol>
     *   <li><strong>First Check (Unsynchronized):</strong> Quick check if already initialized</li>
     *   <li><strong>Synchronization:</strong> Enter critical section only if initialization needed</li>
     *   <li><strong>Second Check (Synchronized):</strong> Verify still needs initialization</li>
     *   <li><strong>Initialization:</strong> Retrieve and cache MessageSource</li>
     * </ol>
     *
     * <p><strong>Performance Benefits:</strong>
     * <ul>
     *   <li>No synchronization overhead after first initialization</li>
     *   <li>Minimal contention during concurrent first access</li>
     *   <li>Avoids repeated Spring context lookups</li>
     *   <li>Volatile field ensures memory visibility across threads</li>
     * </ul>
     *
     * <p><strong>Error Handling Strategy:</strong>
     * The method implements a fail-safe approach where any exception during MessageSource
     * retrieval results in returning null. This ensures that the message resolution system
     * degrades gracefully when Spring context is not available or properly configured.
     *
     * <p><strong>Common Failure Scenarios:</strong>
     * <ul>
     *   <li>Spring application context not yet initialized</li>
     *   <li>MessageSource bean not configured in Spring context</li>
     *   <li>Bean creation failures due to configuration errors</li>
     *   <li>ClassLoader issues preventing bean instantiation</li>
     * </ul>
     *
     * <p><strong>Thread Safety Guarantees:</strong>
     * <ul>
     *   <li>Volatile field ensures proper memory ordering</li>
     *   <li>Synchronization prevents race conditions during initialization</li>
     *   <li>Double-checked pattern prevents duplicate initialization</li>
     *   <li>Safe publication of MessageSource reference</li>
     * </ul>
     *
     * @return The cached MessageSource instance for message resolution, or null if
     *         the MessageSource is unavailable due to Spring context issues
     *
     * @see SpringBeans#getBean(Class)
     * @see MessageSource
     */
    private static MessageSource getMessageSource() {
        if (messageSource == null) {
            synchronized (ExceptionMessageResolver.class) {
                if (messageSource == null) {
                    try {
                        messageSource = SpringBeans.getBean(MessageSource.class);
                    } catch (Exception e) {
                        // MessageSource retrieval failed - return null to indicate unavailability
                        // Common causes:
                        // - Spring context not initialized
                        // - MessageSource bean not configured
                        // - Bean creation/wiring failures
                        //
                        // Note: We don't log this exception as it's expected during
                        // application startup or in non-Spring environments
                        return null;
                    }
                }
            }
        }

        return messageSource;
    }

    /**
     * Clears the cached MessageSource instance to force re-initialization on next access.
     *
     * This method provides a mechanism to reset the MessageSource cache, primarily for
     * testing scenarios where different MessageSource configurations need to be tested
     * or where Spring context reloading occurs. After calling this method, the next
     * call to any message resolution method will trigger a fresh MessageSource lookup.
     *
     * <p><strong>Primary Use Cases:</strong>
     * <ul>
     *   <li><strong>Unit Testing:</strong> Testing with different MessageSource configurations</li>
     *   <li><strong>Integration Testing:</strong> Simulating Spring context reloads</li>
     *   <li><strong>Fallback Testing:</strong> Testing behavior when MessageSource is unavailable</li>
     *   <li><strong>Configuration Changes:</strong> Forcing re-detection of MessageSource beans</li>
     * </ul>
     *
     * <p><strong>Thread Safety Considerations:</strong>
     * This method is intentionally not synchronized to avoid performance overhead in
     * production code. It should only be called from controlled test environments where
     * concurrent access can be managed. In production, the MessageSource should remain
     * stable throughout the application lifecycle.
     *
     * <p><strong>Impact on Concurrent Operations:</strong>
     * Calling this method while other threads are performing message resolution may
     * cause those threads to re-initialize the MessageSource. This is generally safe
     * but may cause temporary performance degradation during re-initialization.
     *
     * <p><strong>Example Usage:</strong>
     * <pre>{@code
     * // In test setup
     * ExceptionMessageResolver.clearCache();
     * // Configure new MessageSource
     * ExceptionMessageResolver.setMessageSource(mockMessageSource);
     * }</pre>
     *
     * @see #setMessageSource(MessageSource)
     * @see #getMessageSource()
     */
    public static void clearCache() {
        messageSource = null;
    }

    /**
     * Sets the MessageSource instance directly, bypassing Spring context lookup.
     *
     * This method allows direct injection of a MessageSource instance, which is
     * particularly useful for testing scenarios where you need to provide a specific
     * MessageSource implementation without configuring a full Spring context.
     *
     * <p><strong>Testing Applications:</strong>
     * <ul>
     *   <li><strong>Mock Testing:</strong> Inject mock MessageSource for predictable behavior</li>
     *   <li><strong>Custom Implementations:</strong> Test with specialized MessageSource implementations</li>
     *   <li><strong>Error Simulation:</strong> Test error handling by providing null or failing MessageSource</li>
     *   <li><strong>Performance Testing:</strong> Use lightweight MessageSource for performance tests</li>
     * </ul>
     *
     * <p><strong>Production Considerations:</strong>
     * This method should not be used in production code as it bypasses the normal
     * Spring dependency injection mechanism. In production, MessageSource should be
     * configured through Spring's application context and retrieved automatically.
     *
     * <p><strong>Thread Safety Considerations:</strong>
     * Like {@link #clearCache()}, this method is not synchronized and should only be
     * called from controlled test environments. The volatile nature of the messageSource
     * field ensures that the new value is visible to all threads immediately.
     *
     * <p><strong>Example Usage:</strong>
     * <pre>{@code
     * // Create mock MessageSource for testing
     * MessageSource mockMessageSource = Mockito.mock(MessageSource.class);
     * when(mockMessageSource.getMessage(anyString(), any(), any()))
     *     .thenReturn("Test message");
     *
     * // Inject mock for testing
     * ExceptionMessageResolver.setMessageSource(mockMessageSource);
     *
     * // Test message resolution
     * String result = ExceptionMessageResolver.resolveMessage("test.code");
     * assertEquals("Test message", result);
     * }</pre>
     *
     * @param messageSource The MessageSource instance to use for message resolution.
     *                     Can be null to simulate MessageSource unavailability.
     *
     * @see #clearCache()
     * @see #getMessageSource()
     * @see MessageSource
     */
    public static void setMessageSource(MessageSource messageSource) {
        ExceptionMessageResolver.messageSource = messageSource;
    }
}

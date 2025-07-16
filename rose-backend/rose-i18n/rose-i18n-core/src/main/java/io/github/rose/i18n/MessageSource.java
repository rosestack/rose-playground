package io.github.rose.i18n;

/**
 * Simplified alias for {@link MessageSource}
 * 
 * <p>This interface provides a shorter, more convenient name for MessageSource
 * while maintaining full compatibility. It can be used interchangeably with 
 * MessageSource in all contexts.</p>
 * 
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Using the alias
 * MessageSource source = MessageSourceBuilder.create("app").yaml().build();
 * 
 * // Still compatible with MessageSource
 * MessageSource serviceSource = source; // Works seamlessly
 * 
 * // Can be used in collections
 * List<MessageSource> sources = Arrays.asList(source1, source2);
 * }</pre>
 * 
 * <p><strong>Note:</strong> This is different from Spring's 
 * {@code org.springframework.context.MessageSource}. If you need to distinguish
 * between them, use the fully qualified names or appropriate imports.</p>
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 * @see MessageSource The main interface this aliases
 * @see org.springframework.context.MessageSource Spring's MessageSource (different interface)
 */
public interface MessageSource extends ServiceMessageSource {
    // Empty interface - serves purely as an alias for MessageSource
    // All functionality is inherited from MessageSource
}
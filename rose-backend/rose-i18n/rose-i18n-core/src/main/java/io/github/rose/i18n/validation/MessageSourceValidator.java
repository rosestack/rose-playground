package io.github.rose.i18n.validation;

import io.github.rose.i18n.MessageSource;
import io.github.rose.i18n.ResourceMessageSource;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Validator for Message Sources
 * 
 * @author <a href="mailto:your-email@example.com">Your Name</a>
 * @since 1.0.0
 */
public class MessageSourceValidator {

    /**
     * Pattern for valid message codes
     */
    private static final Pattern VALID_CODE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9._-]*$");

    /**
     * Pattern for valid source names
     */
    private static final Pattern VALID_SOURCE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");

    /**
     * Validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = Collections.unmodifiableList(errors);
            this.warnings = Collections.unmodifiableList(warnings);
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{valid=").append(valid);
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * Validate a MessageSource
     */
    public static ValidationResult validate(MessageSource messageSource) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate source name
        validateSourceName(messageSource.getSource(), errors);

        // Validate supported locales
        validateSupportedLocales(messageSource.getSupportedLocales(), errors, warnings);

        // Validate default locale
        validateDefaultLocale(messageSource.getDefaultLocale(), messageSource.getSupportedLocales(), errors, warnings);

        // Additional validation for ResourceMessageSource
        if (messageSource instanceof ResourceMessageSource) {
            validateResourceMessageSource((ResourceMessageSource) messageSource, errors, warnings);
        }

        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors, warnings);
    }

    /**
     * Validate source name
     */
    private static void validateSourceName(String source, List<String> errors) {
        if (source == null || source.trim().isEmpty()) {
            errors.add("Source name cannot be null or empty");
        } else if (!VALID_SOURCE_PATTERN.matcher(source).matches()) {
            errors.add("Source name '" + source + "' contains invalid characters. Must match pattern: " + VALID_SOURCE_PATTERN.pattern());
        }
    }

    /**
     * Validate supported locales
     */
    private static void validateSupportedLocales(List<Locale> supportedLocales, List<String> errors, List<String> warnings) {
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            errors.add("Supported locales cannot be null or empty");
        } else {
            // Check for duplicates
            Set<Locale> uniqueLocales = new HashSet<>(supportedLocales);
            if (uniqueLocales.size() != supportedLocales.size()) {
                warnings.add("Duplicate locales found in supported locales list");
            }

            // Check for null locales
            if (supportedLocales.contains(null)) {
                errors.add("Supported locales list contains null values");
            }
        }
    }

    /**
     * Validate default locale
     */
    private static void validateDefaultLocale(Locale defaultLocale, List<Locale> supportedLocales, 
                                            List<String> errors, List<String> warnings) {
        if (defaultLocale == null) {
            errors.add("Default locale cannot be null");
        } else if (supportedLocales != null && !supportedLocales.contains(defaultLocale)) {
            warnings.add("Default locale '" + defaultLocale + "' is not in the supported locales list");
        }
    }

    /**
     * Validate ResourceMessageSource
     */
    private static void validateResourceMessageSource(ResourceMessageSource messageSource, 
                                                            List<String> errors, List<String> warnings) {
        // Validate encoding
        if (messageSource.getEncoding() == null) {
            errors.add("Resource encoding cannot be null");
        }

        // Validate initialized resources
        Set<String> resources = messageSource.getInitializeResources();
        if (resources == null) {
            errors.add("Initialized resources cannot be null");
        } else if (resources.isEmpty()) {
            warnings.add("No resources have been initialized");
        }
    }

    /**
     * Validate message codes
     */
    public static ValidationResult validateMessageCodes(Map<String, String> messages, String sourcePrefix) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (messages == null) {
            errors.add("Messages map cannot be null");
            return new ValidationResult(false, errors, warnings);
        }

        for (Map.Entry<String, String> entry : messages.entrySet()) {
            String code = entry.getKey();
            String message = entry.getValue();

            // Validate code format
            if (code == null || code.trim().isEmpty()) {
                errors.add("Message code cannot be null or empty");
                continue;
            }

            if (!VALID_CODE_PATTERN.matcher(code).matches()) {
                errors.add("Message code '" + code + "' contains invalid characters");
                continue;
            }

            // Validate source prefix
            if (sourcePrefix != null && !code.startsWith(sourcePrefix)) {
                errors.add("Message code '" + code + "' does not start with expected prefix '" + sourcePrefix + "'");
            }

            // Validate message content
            if (message == null) {
                warnings.add("Message for code '" + code + "' is null");
            } else if (message.trim().isEmpty()) {
                warnings.add("Message for code '" + code + "' is empty");
            }

            // Check for unmatched placeholders
            validateMessagePlaceholders(code, message, warnings);
        }

        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors, warnings);
    }

    /**
     * Validate message placeholders
     */
    private static void validateMessagePlaceholders(String code, String message, List<String> warnings) {
        if (message == null) return;

        // Count {} placeholders
        int placeholderCount = 0;
        int index = 0;
        while ((index = message.indexOf("{}", index)) != -1) {
            placeholderCount++;
            index += 2;
        }

        // Check for unmatched braces
        long openBraces = message.chars().filter(c -> c == '{').count();
        long closeBraces = message.chars().filter(c -> c == '}').count();
        
        if (openBraces != closeBraces) {
            warnings.add("Message for code '" + code + "' has unmatched braces");
        }

        // Check for other placeholder patterns that might indicate errors
        if (message.contains("{0}") || message.contains("{1}")) {
            warnings.add("Message for code '" + code + "' uses numbered placeholders (should use {})");
        }
    }

    /**
     * Validate locale coverage
     */
    public static ValidationResult validateLocaleCoverage(Map<Locale, Map<String, String>> localeMessages) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (localeMessages == null || localeMessages.isEmpty()) {
            errors.add("No locale messages provided");
            return new ValidationResult(false, errors, warnings);
        }

        // Get all message codes from all locales
        Set<String> allCodes = new HashSet<>();
        for (Map<String, String> messages : localeMessages.values()) {
            if (messages != null) {
                allCodes.addAll(messages.keySet());
            }
        }

        // Check coverage for each locale
        for (Map.Entry<Locale, Map<String, String>> entry : localeMessages.entrySet()) {
            Locale locale = entry.getKey();
            Map<String, String> messages = entry.getValue();

            if (messages == null) {
                errors.add("Messages for locale '" + locale + "' are null");
                continue;
            }

            Set<String> missingCodes = new HashSet<>(allCodes);
            missingCodes.removeAll(messages.keySet());

            if (!missingCodes.isEmpty()) {
                warnings.add("Locale '" + locale + "' is missing codes: " + missingCodes);
            }
        }

        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors, warnings);
    }
}
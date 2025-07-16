package io.github.rose.i18n;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public interface ResourceMessageSource extends MessageSource {

    /**
     * Initializes the resource
     *
     * @param resource the resource to be loaded for messages
     */
    void initializeResource(String resource);

    /**
     * Initializes the multiple resources
     *
     * @param resources the resources to be loaded for messages
     */
    default void initializeResources(Iterable<String> resources) {
        resources.forEach(this::initializeResource);
    }

    /**
     * Gets a list of all initialized {@link Locale} resources
     *
     * @return
     */
    Set<String> getInitializeResources();

    /**
     * Gets the resource content character encoding
     *
     * @return The default is utf-8
     */
    default Charset getEncoding() {
        return StandardCharsets.UTF_8;
    }
}
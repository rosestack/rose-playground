package io.github.rosestack.i18n;

import java.util.Set;

public interface ReloadedResourceMessageSource extends ResourceMessageSource {
    /**
     * Reload if {@link #canReload(String)} returns <code>true</code>, The calling {@link
     * #initializeResource(String)} as default
     *
     * @param changedResource Changes in the resource
     */
    default void reload(String changedResource) {
        initializeResource(changedResource);
    }

    /**
     * Reload if {@link #canReload(Iterable)} returns <code>true</code>, The calling {@link
     * #initializeResources(Iterable)} as default
     *
     * @param changedResources Changes in the resources
     */
    default void reload(Iterable<String> changedResources) {
        initializeResources(changedResources);
    }

    /**
     * Whether the specified resource can be overloaded
     *
     * @param changedResource Changes in the resource
     * @return Supported by default, returning <code>true<code>
     */
    default boolean canReload(String changedResource) {
        Set<String> resources = getInitializeResources();
        return resources.contains(changedResource);
    }

    /**
     * Whether the specified resource list can be overloaded
     *
     * @param changedResources Changes in the resource
     * @return Supported by default, returning <code>true<code>
     */
    default boolean canReload(Iterable<String> changedResources) {
        Set<String> resources = getInitializeResources();
        boolean reloadable = false;
        for (String changedResource : changedResources) {
            if (reloadable = resources.contains(changedResource)) {
                break;
            }
        }
        return reloadable;
    }
}

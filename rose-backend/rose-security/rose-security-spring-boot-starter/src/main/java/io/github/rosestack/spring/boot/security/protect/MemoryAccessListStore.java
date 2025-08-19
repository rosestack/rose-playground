package io.github.rosestack.spring.boot.security.protect;

import java.util.Collections;
import java.util.Set;

public class MemoryAccessListStore implements AccessListStore {

    @Override
    public Set<String> allowedIps() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deniedIps() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> allowedUsernames() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> deniedUsernames() {
        return Collections.emptySet();
    }
}

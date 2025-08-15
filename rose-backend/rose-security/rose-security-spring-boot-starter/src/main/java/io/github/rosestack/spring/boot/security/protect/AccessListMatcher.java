package io.github.rosestack.spring.boot.security.protect;

import io.github.rosestack.spring.boot.security.config.RoseSecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

public class AccessListMatcher {
    private final AccessListStore store;
    private final RoseSecurityProperties.Protect.AccessList props;

    public AccessListMatcher(AccessListStore store, RoseSecurityProperties properties) {
        this.store = store;
        this.props = properties.getProtect().getAccessList();
    }

    public boolean isAllowed(HttpServletRequest request, String username) {
        if (!props.isEnabled()) {
            return true;
        }
        String clientIp = request.getRemoteAddr();
        Set<String> allowIp = store.allowedIps();
        Set<String> denyIp = store.deniedIps();
        Set<String> allowUser = store.allowedUsernames();
        Set<String> denyUser = store.deniedUsernames();

        boolean ipAllowed = allowIp.isEmpty() || allowIp.contains(clientIp);
        boolean userAllowed = allowUser.isEmpty() || (username != null && allowUser.contains(username));
        boolean ipDenied = denyIp.contains(clientIp);
        boolean userDenied = username != null && denyUser.contains(username);

        if ("ALL".equalsIgnoreCase(props.getCombine())) {
            return ipAllowed && userAllowed && !(ipDenied || userDenied);
        }
        // default ANY
        boolean anyAllowed = ipAllowed || userAllowed;
        boolean anyDenied = ipDenied || userDenied;
        return anyAllowed && !anyDenied;
    }
}

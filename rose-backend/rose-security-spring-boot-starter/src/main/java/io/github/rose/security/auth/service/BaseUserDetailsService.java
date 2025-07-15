package io.github.rose.security.auth.service;

import io.github.rose.security.model.SecurityUser;
import io.github.rose.security.model.UserPrincipal;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface BaseUserDetailsService extends UserDetailsService {
    UserDetails loadUserByEmail(String username) throws UsernameNotFoundException;

    UserDetails loadUserByPhone(String username) throws UsernameNotFoundException;

    default SecurityUser loadUserByUserPrincipal(UserPrincipal userPrincipal) {
        UserDetails userDetails = null;

        if (userPrincipal.getType() == UserPrincipal.Type.USER_NAME) {
            userDetails = loadUserByUsername(userPrincipal.getValue());
        } else if (userPrincipal.getType() == UserPrincipal.Type.EMAIL) {
            userDetails = loadUserByEmail(userPrincipal.getValue());
        } else if (userPrincipal.getType() == UserPrincipal.Type.PHONE) {
            userDetails = loadUserByPhone(userPrincipal.getValue());
        }
        if (userDetails == null) {
            throw new UsernameNotFoundException("User not found by refresh token");
        }

        SecurityUser securityUser = (SecurityUser) userDetails;
        if (securityUser.getAuthority() == null) {
            throw new InsufficientAuthenticationException("User has no authority assigned");
        }

        return securityUser;
    }
}

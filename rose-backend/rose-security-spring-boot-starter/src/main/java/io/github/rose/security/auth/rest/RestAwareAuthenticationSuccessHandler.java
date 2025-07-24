/**
 * Copyright © 2016-2025 The Thingsboard Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.rose.security.auth.rest;

import io.github.rose.core.util.JsonUtils;
import io.github.rose.security.model.JwtPair;
import io.github.rose.security.model.SecurityUser;
import io.github.rose.security.model.token.JwtTokenFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component(value = "defaultAuthenticationSuccessHandler")
@RequiredArgsConstructor
public class RestAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenFactory tokenFactory;
//    private final TwoFaConfigManager twoFaConfigManager;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        JwtPair tokenPair = new JwtPair();

//        if (authentication instanceof MfaAuthenticationToken) {
//            int preVerificationTokenLifetime = twoFaConfigManager.getPlatformTwoFaSettings(securityUser.getTenantId(), true)
//                    .flatMap(settings -> Option.ofNullable(settings.getTotalAllowedTimeForVerification())
//                            .filter(time -> time > 0))
//                    .orElse((int) TimeUnit.MINUTES.toSeconds(30));
//            tokenPair.setToken(tokenFactory.createPreVerificationToken(securityUser, preVerificationTokenLifetime).getToken());
//            tokenPair.setRefreshToken(null);
//            tokenPair.setScope(Authority.PRE_VERIFICATION_TOKEN);
//        } else {
        tokenPair = tokenFactory.createTokenPair(securityUser);
//        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JsonUtils.writeValue(response.getWriter(), tokenPair);

        clearAuthenticationAttributes(request);
    }

    /**
     * Removes temporary authentication-related data which may have been stored
     * in the session during the authentication process..
     */
    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}

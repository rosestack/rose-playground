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
package io.github.rose.security.auth.service;

import io.github.rose.security.event.UserAuthDataChangedEvent;
import io.github.rose.security.model.token.JwtTokenFactory;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Service
@RequiredArgsConstructor
public class DefaultTokenOutdatingService implements TokenOutdatingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenFactory tokenFactory;


    @EventListener(classes = UserAuthDataChangedEvent.class)
    public void onUserAuthDataChanged(UserAuthDataChangedEvent event) {
        if (StringUtils.hasText(event.getId())) {
            redisTemplate.opsForValue().set(event.getId(), event.getTs());
        }
    }

    @Override
    public boolean isOutdated(String token, Long userId) {
        Claims claims = tokenFactory.parseTokenClaims(token).getBody();
        long issueTime = claims.getIssuedAt().getTime();
        String sessionId = claims.get("sessionId", String.class);
        if (isTokenOutdated(issueTime, userId.toString())) {
            return true;
        } else {
            return sessionId != null && isTokenOutdated(issueTime, sessionId);
        }
    }

    private Boolean isTokenOutdated(long issueTime, String sessionId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(sessionId)).map(outdatageTime -> isTokenOutdated(issueTime, (Long) outdatageTime)).orElse(false);
    }

    private boolean isTokenOutdated(long issueTime, Long outdatageTime) {
        return MILLISECONDS.toSeconds(issueTime) < MILLISECONDS.toSeconds(outdatageTime);
    }
}

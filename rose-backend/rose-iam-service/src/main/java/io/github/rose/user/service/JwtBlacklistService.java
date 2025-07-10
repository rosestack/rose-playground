package io.github.rose.user.service;

public interface JwtBlacklistService {
    void add(String token);
    boolean isBlacklisted(String token);
}

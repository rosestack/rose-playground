package io.github.rose.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("rose-very-secret-key-rose-very-secret-key-rose-very-secret-key-rose-very-secret-key-rose-very-secret-key-rose-very-secret-key-rose-very-secret-key-rose-very-secret-key".getBytes());
    private static final long EXPIRATION = 86400000L; // 1天

    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(userId == null ? null : userId.toString())
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

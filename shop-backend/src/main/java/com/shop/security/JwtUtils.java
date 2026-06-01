package com.shop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtUtils(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.access-expiration}") long accessExpiration,
                    @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public long getAccessExpirationMs() {
        return accessExpiration;
    }

    public long getRefreshExpirationMs() {
        return refreshExpiration;
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
}

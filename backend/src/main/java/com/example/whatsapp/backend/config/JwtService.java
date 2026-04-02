package com.example.whatsapp.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirySeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-seconds}") long expirySeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirySeconds = expirySeconds;
    }

    public String generate(Long userId, String phoneNumber) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phoneNumber)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}


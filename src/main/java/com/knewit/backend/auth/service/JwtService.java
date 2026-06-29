package com.knewit.backend.auth.service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {
    @Value("${secret-key}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30L;    /* 30 days */

    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    private SecretKey getSignInKey() {
        System.out.println("JWT SERVICE GET SIGNIN KEY METHOD CALLED");
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        } catch (Exception e) {
            keyBytes = SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                keyBytes = digest.digest(keyBytes);
            } catch (java.security.NoSuchAlgorithmException ex) {
                throw new RuntimeException("SHA-256 algorithm not found", ex);
            }
        }

        System.out.println("KEYBYTES ARRAY LENGTH = " + keyBytes.length);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        System.out.println("SECRET KEY GENERATED = " + secretKey);
        return secretKey;
    }

    public String generateToken(Long userId, String email, boolean isProfileCompleted) {
        System.out.println("GENERATING AUTHENTICATION TOKEN");
        String token = Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("isProfileCompleted", isProfileCompleted)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();

        System.out.println("JWT SERVICE TOKEN GENERATED = " + token);
        return token;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch(JwtException e) {
            return false;
        }
    }

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
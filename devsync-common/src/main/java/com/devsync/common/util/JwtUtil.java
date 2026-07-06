package com.devsync.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWT utility for token generation and validation.
 */
@Component
public class JwtUtil {

    @Value("${devsync.jwt.secret}")
    private String jwtSecret;

    @Value("${devsync.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration; // 15 minutes

    @Value("${devsync.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration; // 7 days

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String email, String role, UUID orgId) {
        return buildToken(Map.of(
                "userId", userId.toString(),
                "email", email,
                "role", role,
                "orgId", orgId != null ? orgId.toString() : ""
        ), email, accessTokenExpiration);
    }

    public String generateRefreshToken(UUID userId, String email) {
        return buildToken(Map.of(
                "userId", userId.toString(),
                "type", "refresh"
        ), email, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, claims -> claims.get("userId", String.class)));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public UUID extractOrgId(String token) {
        String orgId = extractClaim(token, claims -> claims.get("orgId", String.class));
        return (orgId != null && !orgId.isEmpty()) ? UUID.fromString(orgId) : null;
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

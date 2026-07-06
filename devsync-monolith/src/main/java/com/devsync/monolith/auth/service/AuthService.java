package com.devsync.monolith.auth.service;

import com.devsync.common.exception.BadRequestException;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.ResourceNotFoundException;
import com.devsync.common.exception.UnauthorizedException;
import com.devsync.common.util.JwtUtil;
import com.devsync.monolith.auth.dto.*;
import com.devsync.monolith.auth.entity.RefreshToken;
import com.devsync.monolith.auth.entity.User;
import com.devsync.monolith.auth.repository.RefreshTokenRepository;
import com.devsync.monolith.auth.repository.UserRepository;
import com.devsync.monolith.org.entity.Organization;
import com.devsync.monolith.org.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${devsync.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${devsync.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        UUID orgId = null;
        String roleName = "DEVELOPER";

        // If org name provided, create org and assign ORG_ADMIN role
        if (request.getOrganizationName() != null && !request.getOrganizationName().isBlank()) {
            String slug = request.getOrganizationName().toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-");

            if (organizationRepository.existsBySlugAndDeletedFalse(slug)) {
                slug = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
            }

            Organization org = Organization.builder()
                    .name(request.getOrganizationName())
                    .slug(slug)
                    .subscriptionTier("FREE")
                    .build();
            org = organizationRepository.save(org);
            orgId = org.getId();
            roleName = "ORG_ADMIN";
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .organizationId(orgId)
                .roleName(roleName)
                .active(true)
                .build();
        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRoleName(), user.getOrganizationId());
        String refreshToken = createRefreshToken(user.getId());

        log.info("User registered: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRoleName())
                .organizationId(user.getOrganizationId())
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRoleName(), user.getOrganizationId());
        String refreshToken = createRefreshToken(user.getId());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRoleName())
                .organizationId(user.getOrganizationId())
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new UnauthorizedException("Refresh token has expired");
        }

        User user = userRepository.findByIdAndDeletedFalse(storedToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", storedToken.getUserId()));

        // Revoke old and create new refresh token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRoleName(), user.getOrganizationId());
        String newRefreshToken = createRefreshToken(user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRoleName())
                .organizationId(user.getOrganizationId())
                .expiresIn(accessTokenExpiration / 1000)
                .build();
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Blacklist the access token in Redis
        if (accessToken != null && !accessToken.isBlank()) {
            String tokenKey = "blacklist:" + accessToken;
            redisTemplate.opsForValue().set(tokenKey, "blacklisted", accessTokenExpiration, TimeUnit.MILLISECONDS);
        }

        // Revoke refresh token
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }

        log.info("User logged out");
    }

    public boolean isTokenBlacklisted(String token) {
        String tokenKey = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }

    private String createRefreshToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}

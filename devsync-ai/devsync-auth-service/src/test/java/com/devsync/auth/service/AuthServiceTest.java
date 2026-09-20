package com.devsync.auth.service;

import com.devsync.auth.dto.LoginRequest;
import com.devsync.auth.dto.RegisterRequest;
import com.devsync.auth.dto.AuthResponse;
import com.devsync.auth.entity.User;
import com.devsync.auth.repository.OrganizationRepository;
import com.devsync.auth.repository.RefreshTokenRepository;
import com.devsync.auth.repository.UserRepository;
import com.devsync.common.exception.DuplicateResourceException;
import com.devsync.common.exception.UnauthorizedException;
import com.devsync.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@devsync.io");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        User savedUser = User.builder()
                .email("test@devsync.io")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .roleName("DEVELOPER")
                .active(true)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", UUID.randomUUID());

        when(userRepository.existsByEmailAndDeletedFalse("test@devsync.io")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo("test@devsync.io");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@devsync.io");

        when(userRepository.existsByEmailAndDeletedFalse("existing@devsync.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_shouldReturnTokensForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@devsync.io");
        request.setPassword("password123");

        User user = User.builder()
                .email("test@devsync.io")
                .passwordHash("hashed")
                .roleName("DEVELOPER")
                .active(true)
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        when(userRepository.findByEmailAndDeletedFalse("test@devsync.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(), any(), any(), any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void login_shouldThrowForInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@devsync.io");
        request.setPassword("wrong");

        User user = User.builder()
                .email("test@devsync.io")
                .passwordHash("hashed")
                .active(true)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("test@devsync.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_shouldThrowForDeactivatedAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@devsync.io");
        request.setPassword("password123");

        User user = User.builder()
                .email("test@devsync.io")
                .passwordHash("hashed")
                .active(false)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("test@devsync.io")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("deactivated");
    }

    @Test
    void logout_shouldBlacklistToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        authService.logout("some-token", null);

        verify(valueOperations).set(eq("blacklist:some-token"), eq("blacklisted"), anyLong(), any());
    }

    @Test
    void isTokenBlacklisted_shouldReturnTrueForBlacklistedToken() {
        when(redisTemplate.hasKey("blacklist:some-token")).thenReturn(true);

        assertThat(authService.isTokenBlacklisted("some-token")).isTrue();
    }
}

package com.moviebooking.user.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails testUser;
    private final String secretKey = "mySecretKeyForTestingPurposesOnly123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final long tokenValidityInSeconds = 3600; // 1 hour

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secretKey, tokenValidityInSeconds);

        testUser = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void createToken_ShouldGenerateValidToken() {
        // When
        String token = jwtTokenProvider.createToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtTokenProvider.createToken(testUser);

        // When
        String extractedUsername = jwtTokenProvider.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("test@example.com");
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUserMatches() {
        // Given
        String token = jwtTokenProvider.createToken(testUser);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenUserDoesNotMatch() {
        // Given
        String token = jwtTokenProvider.createToken(testUser);
        UserDetails differentUser = User.builder()
                .username("different@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        // When
        boolean isValid = jwtTokenProvider.validateToken(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Given
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(secretKey, -1); // Expired immediately
        String expiredToken = shortLivedProvider.createToken(testUser);

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken, testUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void extractUsername_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.token.here";

        // Then
        assertThatThrownBy(() -> jwtTokenProvider.extractUsername(invalidToken))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    void createToken_ShouldIncludeRolesInClaims() {
        // Given
        String token = jwtTokenProvider.createToken(testUser);

        // When
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Then
        assertThat(claims.get("roles")).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void createToken_ShouldHaveCorrectExpiration() {
        // Given
        long beforeCreation = System.currentTimeMillis();

        // When
        String token = jwtTokenProvider.createToken(testUser);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long expectedExpiration = beforeCreation + (tokenValidityInSeconds * 1000);
        long actualExpiration = claims.getExpiration().getTime();

        // Allow 1000ms tolerance for test execution time
        assertThat(actualExpiration).isBetween(expectedExpiration - 1000, expectedExpiration + 1000);
    }

    @Test
    void validateToken_ShouldHandleNullToken() {
        // Then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(null, testUser))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void extractUsername_ShouldHandleNullToken() {
        // Then
        assertThatThrownBy(() -> jwtTokenProvider.extractUsername(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
package com.moviebooking.gateway.security;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenValidator {

    private final SecretKey key;

    public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @CircuitBreaker(name = "jwt-validation", fallbackMethod = "fallbackValidateToken")
    @RateLimiter(name = "user-auth")
    @Retry(name = "user-service")
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.warn("Empty or null JWT token provided");
                return false;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            boolean isValid = !claims.getExpiration().before(new Date());

            if (!isValid) {
                log.warn("JWT token is expired. Subject: {}, Expiration: {}",
                    claims.getSubject(), claims.getExpiration());
            } else {
                log.debug("JWT token validated successfully for subject: {}", claims.getSubject());
            }

            return isValid;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during JWT token validation: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean fallbackValidateToken(String token, Exception ex) {
        log.error("JWT validation circuit breaker activated. Fallback method called. Error: {}",
            ex.getMessage(), ex);

        // In fallback, we could:
        // 1. Return false (deny access - safe approach)
        // 2. Check a cache of recently validated tokens
        // 3. Use a simplified validation

        // For security, we deny access when validation service is down
        return false;
    }

    @CircuitBreaker(name = "jwt-validation", fallbackMethod = "fallbackGetClaims")
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to extract claims from JWT token: {}", e.getMessage());
            throw new SecurityException("Invalid JWT token", e);
        }
    }

    public Claims fallbackGetClaims(String token, Exception ex) {
        log.error("JWT claims extraction circuit breaker activated. Error: {}", ex.getMessage());
        throw new SecurityException("JWT validation service unavailable", ex);
    }

    public String extractSubject(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract subject from token: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return getClaims(token).getExpiration();
        } catch (Exception e) {
            log.warn("Failed to extract expiration from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Failed to check token expiration: {}", e.getMessage());
            return true; // Assume expired if we can't determine
        }
    }

    /**
     * Validates token format without signature verification (for pre-validation)
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            log.warn("Invalid JWT format: expected 3 parts, got {}", parts.length);
            return false;
        }

        // Each part should be base64 encoded
        for (String part : parts) {
            if (part.isEmpty()) {
                log.warn("Invalid JWT format: empty part found");
                return false;
            }
        }

        return true;
    }
}

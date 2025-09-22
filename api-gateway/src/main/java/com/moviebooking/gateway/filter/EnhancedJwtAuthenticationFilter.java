package com.moviebooking.gateway.filter;

import com.moviebooking.gateway.security.JwtTokenValidator;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Component
@Slf4j
public class EnhancedJwtAuthenticationFilter extends AbstractGatewayFilterFactory<EnhancedJwtAuthenticationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimiter userAuthRateLimiter;
    private final CircuitBreaker jwtValidationCircuitBreaker;

    public EnhancedJwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator,
                                         ReactiveStringRedisTemplate redisTemplate,
                                         RateLimiter userAuthRateLimiter) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
        this.redisTemplate = redisTemplate;
        this.userAuthRateLimiter = userAuthRateLimiter;

        // Create a circuit breaker specifically for JWT validation
        this.jwtValidationCircuitBreaker = CircuitBreaker.ofDefaults("jwt-validation");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String clientIp = getClientIp(request);

            log.debug("Enhanced JWT Filter applied to: {} from IP: {}", path, clientIp);

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {} from IP: {}", path, clientIp);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            String userId = extractUserIdFromToken(token);

            return validateTokenWithResilience(token, userId, clientIp)
                    .flatMap(isValid -> {
                        if (isValid) {
                            log.debug("JWT token validated successfully for user: {} from IP: {}", userId, clientIp);

                            // Add user context to request headers for downstream services
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-ID", userId)
                                    .header("X-Client-IP", clientIp)
                                    .header("X-Auth-Time", String.valueOf(System.currentTimeMillis()))
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            log.warn("Invalid JWT token for user: {} from IP: {}", userId, clientIp);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                    })
                    .onErrorResume(throwable -> {
                        log.error("Error during JWT validation for user: {} from IP: {}: {}",
                                userId, clientIp, throwable.getMessage(), throwable);
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private Mono<Boolean> validateTokenWithResilience(String token, String userId, String clientIp) {
        String cacheKey = "jwt:validation:" + token.hashCode();

        return redisTemplate.opsForValue().get(cacheKey)
                .cast(String.class)
                .map(cached -> "valid".equals(cached))
                .switchIfEmpty(
                    Mono.fromCallable(() -> jwtTokenValidator.validateToken(token))
                            .transformDeferred(CircuitBreakerOperator.of(jwtValidationCircuitBreaker))
                            .transformDeferred(RateLimiterOperator.of(userAuthRateLimiter))
                            .doOnNext(isValid -> {
                                if (isValid) {
                                    // Cache valid tokens for 5 minutes
                                    redisTemplate.opsForValue().set(cacheKey, "valid", Duration.ofMinutes(5))
                                            .subscribe(
                                                result -> log.debug("Cached JWT validation result for user: {}", userId),
                                                error -> log.warn("Failed to cache JWT validation result: {}", error.getMessage())
                                            );
                                }
                            })
                            .doOnError(error -> log.error("JWT validation failed for user: {} from IP: {}: {}",
                                    userId, clientIp, error.getMessage()))
                );
    }

    private String extractUserIdFromToken(String token) {
        try {
            return jwtTokenValidator.getClaims(token).getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract user ID from token: {}", e.getMessage());
            return "unknown";
        }
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
    }

    public static class Config {
        private boolean enableCaching = true;
        private boolean enableRateLimiting = true;
        private boolean enableCircuitBreaker = true;

        // Getters and setters
        public boolean isEnableCaching() { return enableCaching; }
        public void setEnableCaching(boolean enableCaching) { this.enableCaching = enableCaching; }

        public boolean isEnableRateLimiting() { return enableRateLimiting; }
        public void setEnableRateLimiting(boolean enableRateLimiting) { this.enableRateLimiting = enableRateLimiting; }

        public boolean isEnableCircuitBreaker() { return enableCircuitBreaker; }
        public void setEnableCircuitBreaker(boolean enableCircuitBreaker) { this.enableCircuitBreaker = enableCircuitBreaker; }
    }
}
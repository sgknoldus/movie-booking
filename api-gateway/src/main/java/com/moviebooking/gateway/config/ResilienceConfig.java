package com.moviebooking.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }

    @Bean
    public RateLimiter userAuthRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // Allow 100 requests
                .limitRefreshPeriod(Duration.ofMinutes(1)) // Per minute
                .timeoutDuration(Duration.ofSeconds(1)) // Wait up to 1 second for a permit
                .build();

        return RateLimiter.of("user-auth", config);
    }

    @Bean
    public HealthIndicator circuitBreakerHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        return () -> {
            boolean allHealthy = circuitBreakerRegistry.getAllCircuitBreakers()
                    .stream()
                    .allMatch(cb -> cb.getState().name().equals("CLOSED"));

            if (allHealthy) {
                return Health.up()
                        .withDetail("circuit-breakers", "All circuit breakers are CLOSED")
                        .build();
            } else {
                return Health.down()
                        .withDetail("circuit-breakers", "Some circuit breakers are OPEN or HALF_OPEN")
                        .build();
            }
        };
    }

    @Bean
    public HealthIndicator rateLimiterHealthIndicator(RateLimiterRegistry rateLimiterRegistry) {
        return () -> {
            boolean allHealthy = rateLimiterRegistry.getAllRateLimiters()
                    .stream()
                    .allMatch(rl -> rl.getMetrics().getAvailablePermissions() > 0);

            return allHealthy ?
                Health.up().withDetail("rate-limiters", "All rate limiters have available permits").build() :
                Health.down().withDetail("rate-limiters", "Some rate limiters are exhausted").build();
        };
    }

    @Bean
    public HealthIndicator bulkheadHealthIndicator(BulkheadRegistry bulkheadRegistry) {
        return () -> {
            boolean allHealthy = bulkheadRegistry.getAllBulkheads()
                    .stream()
                    .allMatch(bh -> bh.getMetrics().getAvailableConcurrentCalls() > 0);

            return allHealthy ?
                Health.up().withDetail("bulkheads", "All bulkheads have available capacity").build() :
                Health.down().withDetail("bulkheads", "Some bulkheads are at maximum capacity").build();
        };
    }
}
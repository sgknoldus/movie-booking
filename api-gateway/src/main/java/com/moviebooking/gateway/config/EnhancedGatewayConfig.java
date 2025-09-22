package com.moviebooking.gateway.config;

import com.moviebooking.gateway.filter.EnhancedJwtAuthenticationFilter;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class EnhancedGatewayConfig {

    private final EnhancedJwtAuthenticationFilter enhancedJwtAuthenticationFilter;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;
    private final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Bean("enhancedRouteLocator")
    public RouteLocator enhancedRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Theatre Service Routes with full resilience patterns
                .route("theatre-service-cities-enhanced", r -> r
                        .path("/api/v1/cities/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("theatre-service")
                                        .setFallbackUri("forward:/fallback/theatre"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                                .requestRateLimiter(rl -> rl
                                        .setKeyResolver(exchange ->
                                            exchange.getRequest().getHeaders().getFirst("X-User-ID") != null ?
                                                reactor.core.publisher.Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-ID")) :
                                                reactor.core.publisher.Mono.just("anonymous")))
                        )
                        .uri("lb://theatre-service"))

                .route("theatre-service-theatres-enhanced", r -> r
                        .path("/api/v1/theatres/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("theatre-service")
                                        .setFallbackUri("forward:/fallback/theatre"))
                                .retry(retryConfig -> retryConfig.setRetries(3))
                        )
                        .uri("lb://theatre-service"))

                .route("theatre-service-screens-enhanced", r -> r
                        .path("/api/v1/screens/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("theatre-service")
                                        .setFallbackUri("forward:/fallback/theatre"))
                        )
                        .uri("lb://theatre-service"))

                .route("theatre-service-shows-enhanced", r -> r
                        .path("/api/v1/shows/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("theatre-service")
                                        .setFallbackUri("forward:/fallback/theatre"))
                        )
                        .uri("lb://theatre-service"))

                // Booking Service Routes with enhanced resilience
                .route("booking-service-enhanced", r -> r
                        .path("/api/bookings/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("booking-service")
                                        .setFallbackUri("forward:/fallback/booking"))
                                .retry(retryConfig -> retryConfig.setRetries(4))
                                .requestRateLimiter(rl -> rl
                                        .setKeyResolver(exchange ->
                                            reactor.core.publisher.Mono.just(
                                                exchange.getRequest().getHeaders().getFirst("X-User-ID")
                                            )))
                        )
                        .uri("lb://booking-service"))

                // Payment Service Routes with strict resilience
                .route("payment-service-enhanced", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("payment-service")
                                        .setFallbackUri("forward:/fallback/payment"))
                                .retry(retryConfig -> retryConfig.setRetries(2)) // Fewer retries for payments
                        )
                        .uri("lb://payment-service"))

                // User Service Routes
                .route("user-service-public-enhanced", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("user-service")
                                        .setFallbackUri("forward:/fallback/auth"))
                                .requestRateLimiter(rl -> rl
                                        .setKeyResolver(exchange ->
                                            reactor.core.publisher.Mono.just(
                                                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                            )))
                        )
                        .uri("lb://user-service"))

                .route("user-service-protected-enhanced", r -> r
                        .path("/api/users/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("user-service")
                                        .setFallbackUri("forward:/fallback/user"))
                        )
                        .uri("lb://user-service"))

                // Search Service Routes (public, basic resilience)
                .route("search-service-enhanced", r -> r
                        .path("/api/v1/search/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("search-service")
                                        .setFallbackUri("forward:/fallback/search"))
                                .requestRateLimiter(rl -> rl
                                        .setKeyResolver(exchange ->
                                            reactor.core.publisher.Mono.just("search-" +
                                                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress())))
                        )
                        .uri("lb://search-service"))

                // Ticket Service Routes
                .route("ticket-service-enhanced", r -> r
                        .path("/api/tickets/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("ticket-service")
                                        .setFallbackUri("forward:/fallback/ticket"))
                        )
                        .uri("lb://ticket-service"))

                // Notification Service Routes
                .route("notification-service-enhanced", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .filter(enhancedJwtAuthFilter())
                                .circuitBreaker(c -> c
                                        .setName("notification-service")
                                        .setFallbackUri("forward:/fallback/notification"))
                        )
                        .uri("lb://notification-service"))

                .build();
    }

    private GatewayFilter enhancedJwtAuthFilter() {
        return enhancedJwtAuthenticationFilter.apply(new EnhancedJwtAuthenticationFilter.Config());
    }

    private Object createResilienceMetadata(String serviceName) {
        return new Object() {
            public String getServiceName() { return serviceName; }
            public CircuitBreaker getCircuitBreaker() { return circuitBreakerRegistry.circuitBreaker(serviceName); }
            public Retry getRetry() { return retryRegistry.retry(serviceName); }
            public TimeLimiter getTimeLimiter() { return timeLimiterRegistry.timeLimiter(serviceName); }
            public RateLimiter getRateLimiter() { return rateLimiterRegistry.rateLimiter("general-api"); }
            public Bulkhead getBulkhead() { return bulkheadRegistry.bulkhead(serviceName); }
        };
    }
}
package com.moviebooking.gateway.filter;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class ResilienceGatewayFilterFactory extends AbstractGatewayFilterFactory<ResilienceGatewayFilterFactory.Config> {

    public ResilienceGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String serviceName = config.getServiceName();

            log.debug("Applying resilience patterns for service: {}", serviceName);

            return chain.filter(exchange)
                    .transformDeferred(mono -> {
                        Mono<Void> resilientMono = mono;

                        // Apply TimeLimiter first
                        if (config.getTimeLimiter() != null) {
                            resilientMono = resilientMono.transformDeferred(
                                    TimeLimiterOperator.of(config.getTimeLimiter())
                            );
                        }

                        // Apply CircuitBreaker
                        if (config.getCircuitBreaker() != null) {
                            resilientMono = resilientMono.transformDeferred(
                                    CircuitBreakerOperator.of(config.getCircuitBreaker())
                            );
                        }

                        // Apply Retry
                        if (config.getRetry() != null) {
                            resilientMono = resilientMono.transformDeferred(
                                    RetryOperator.of(config.getRetry())
                            );
                        }

                        // Apply RateLimiter
                        if (config.getRateLimiter() != null) {
                            resilientMono = resilientMono.transformDeferred(
                                    RateLimiterOperator.of(config.getRateLimiter())
                            );
                        }

                        // Apply Bulkhead
                        if (config.getBulkhead() != null) {
                            resilientMono = resilientMono.transformDeferred(
                                    BulkheadOperator.of(config.getBulkhead())
                            );
                        }

                        return resilientMono;
                    })
                    .doOnSuccess(result -> log.debug("Request completed successfully for service: {}", serviceName))
                    .onErrorResume(throwable -> {
                        log.error("Request failed for service: {} with error: {}", serviceName, throwable.getMessage());

                        // Handle different types of failures
                        if (throwable instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
                            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                            exchange.getResponse().getHeaders().add("X-Circuit-Breaker", "OPEN");
                        } else if (throwable instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
                            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            exchange.getResponse().getHeaders().add("X-Rate-Limit", "EXCEEDED");
                        } else if (throwable instanceof io.github.resilience4j.bulkhead.BulkheadFullException) {
                            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                            exchange.getResponse().getHeaders().add("X-Bulkhead", "FULL");
                        } else {
                            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        }

                        return exchange.getResponse().setComplete();
                    });
        };
    }

    public static class Config {
        private String serviceName;
        private CircuitBreaker circuitBreaker;
        private Retry retry;
        private TimeLimiter timeLimiter;
        private RateLimiter rateLimiter;
        private Bulkhead bulkhead;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public CircuitBreaker getCircuitBreaker() { return circuitBreaker; }
        public void setCircuitBreaker(CircuitBreaker circuitBreaker) { this.circuitBreaker = circuitBreaker; }

        public Retry getRetry() { return retry; }
        public void setRetry(Retry retry) { this.retry = retry; }

        public TimeLimiter getTimeLimiter() { return timeLimiter; }
        public void setTimeLimiter(TimeLimiter timeLimiter) { this.timeLimiter = timeLimiter; }

        public RateLimiter getRateLimiter() { return rateLimiter; }
        public void setRateLimiter(RateLimiter rateLimiter) { this.rateLimiter = rateLimiter; }

        public Bulkhead getBulkhead() { return bulkhead; }
        public void setBulkhead(Bulkhead bulkhead) { this.bulkhead = bulkhead; }
    }
}
package com.moviebooking.gateway.metrics;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResilienceMetricsCustomizer {

    private final MeterRegistry meterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    @PostConstruct
    public void bindMetrics() {
        // Bind Circuit Breaker metrics
        TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry)
                .bindTo(meterRegistry);

        // Bind Retry metrics
        TaggedRetryMetrics.ofRetryRegistry(retryRegistry)
                .bindTo(meterRegistry);

        // Bind TimeLimiter metrics
        TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(timeLimiterRegistry)
                .bindTo(meterRegistry);

        // Bind RateLimiter metrics
        TaggedRateLimiterMetrics.ofRateLimiterRegistry(rateLimiterRegistry)
                .bindTo(meterRegistry);

        // Bind Bulkhead metrics
        TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry)
                .bindTo(meterRegistry);

        log.info("Resilience4j metrics bound to Micrometer registry");
    }


    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void recordCustomMetrics() {
        // Record circuit breaker states
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            String serviceName = circuitBreaker.getName();
            CircuitBreaker.State state = circuitBreaker.getState();

            meterRegistry.gauge("circuit_breaker_state",
                    io.micrometer.core.instrument.Tags.of(
                            "service", serviceName,
                            "state", state.name()),
                    state == CircuitBreaker.State.CLOSED ? 1 : 0);

            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            Timer.Sample sample = Timer.start(meterRegistry);

            meterRegistry.gauge("circuit_breaker_failure_rate",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getFailureRate());

            meterRegistry.gauge("circuit_breaker_slow_call_rate",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getSlowCallRate());
        });

        // Record rate limiter metrics
        rateLimiterRegistry.getAllRateLimiters().forEach(rateLimiter -> {
            String serviceName = rateLimiter.getName();
            RateLimiter.Metrics metrics = rateLimiter.getMetrics();

            meterRegistry.gauge("rate_limiter_available_permissions",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getAvailablePermissions());

            meterRegistry.gauge("rate_limiter_waiting_threads",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getNumberOfWaitingThreads());
        });

        // Record bulkhead metrics
        bulkheadRegistry.getAllBulkheads().forEach(bulkhead -> {
            String serviceName = bulkhead.getName();
            Bulkhead.Metrics metrics = bulkhead.getMetrics();

            meterRegistry.gauge("bulkhead_available_concurrent_calls",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getAvailableConcurrentCalls());

            meterRegistry.gauge("bulkhead_max_allowed_concurrent_calls",
                    io.micrometer.core.instrument.Tags.of("service", serviceName),
                    metrics.getMaxAllowedConcurrentCalls());
        });

        log.debug("Custom resilience metrics recorded");
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void logResilienceHealth() {
        StringBuilder healthReport = new StringBuilder("Resilience Health Report:\n");

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            CircuitBreaker.State state = cb.getState();
            CircuitBreaker.Metrics metrics = cb.getMetrics();

            healthReport.append(String.format(
                    "CircuitBreaker[%s]: State=%s, FailureRate=%.2f%%, SlowCallRate=%.2f%%\n",
                    cb.getName(),
                    state,
                    metrics.getFailureRate(),
                    metrics.getSlowCallRate()
            ));

            // Alert if circuit breaker is open
            if (state == CircuitBreaker.State.OPEN) {
                log.warn("ALERT: Circuit breaker {} is OPEN - Service may be unavailable", cb.getName());
            }
        });

        rateLimiterRegistry.getAllRateLimiters().forEach(rl -> {
            RateLimiter.Metrics metrics = rl.getMetrics();
            healthReport.append(String.format(
                    "RateLimiter[%s]: AvailablePermissions=%d, WaitingThreads=%d\n",
                    rl.getName(),
                    metrics.getAvailablePermissions(),
                    metrics.getNumberOfWaitingThreads()
            ));

            // Alert if rate limiter is exhausted
            if (metrics.getAvailablePermissions() == 0) {
                log.warn("ALERT: Rate limiter {} is exhausted - Requests may be throttled", rl.getName());
            }
        });

        bulkheadRegistry.getAllBulkheads().forEach(bh -> {
            Bulkhead.Metrics metrics = bh.getMetrics();
            healthReport.append(String.format(
                    "Bulkhead[%s]: AvailableCalls=%d/%d\n",
                    bh.getName(),
                    metrics.getAvailableConcurrentCalls(),
                    metrics.getMaxAllowedConcurrentCalls()
            ));

            // Alert if bulkhead is full
            if (metrics.getAvailableConcurrentCalls() == 0) {
                log.warn("ALERT: Bulkhead {} is full - Requests may be rejected", bh.getName());
            }
        });

        log.info(healthReport.toString());
    }
}
# API Gateway Resilience Patterns Implementation

This document describes the comprehensive resilience patterns implemented in the Movie Booking System API Gateway using Resilience4j and enhanced JWT authentication.

## 🔧 Overview

The API Gateway implements multiple resilience patterns to ensure high availability, fault tolerance, and graceful degradation when downstream services fail. The implementation includes:

- **Circuit Breaker Pattern**
- **Retry Pattern**
- **Rate Limiting**
- **Time Limiting**
- **Bulkhead Pattern**
- **Enhanced JWT Authentication with Caching**
- **Fallback Mechanisms**

## 🏗️ Architecture Components

### 1. Circuit Breaker Configuration

Circuit breakers are configured per service with different thresholds based on service criticality:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      booking-service:
        slidingWindowSize: 15
        failureRateThreshold: 40
        waitDurationInOpenState: 60s
        slowCallRateThreshold: 80
        slowCallDurationThreshold: 3s
```

**Service-specific configurations:**
- **Booking Service**: Stricter thresholds (40% failure rate) due to business criticality
- **Payment Service**: Most conservative (30% failure rate, 2-minute wait time)
- **Search Service**: More permissive (70% failure rate) for non-critical operations
- **User Service**: Balanced settings (50% failure rate)

### 2. Rate Limiting Strategy

Rate limiting is applied at different levels:

```yaml
resilience4j:
  ratelimiter:
    instances:
      user-auth: 10 requests/second
      booking-requests: 5 requests/second
      general-api: 100 requests/second
```

**Implementation Details:**
- **User Authentication**: 10 requests/second to prevent brute force attacks
- **Booking Requests**: 5 requests/second to manage high-value operations
- **General API**: 100 requests/second for normal operations

### 3. Enhanced JWT Authentication

The enhanced JWT authentication filter provides:

```java
@Component
public class EnhancedJwtAuthenticationFilter {
    // Redis caching for token validation results
    // Rate limiting per user
    // Circuit breaker for JWT validation
    // Client IP extraction and tracking
}
```

**Key Features:**
- **Redis Caching**: Valid tokens cached for 5 minutes
- **Rate Limiting**: Applied per user to prevent abuse
- **Circuit Breaker**: Protects JWT validation service
- **IP Tracking**: Extracts real client IP through proxy headers
- **Request Enrichment**: Adds user context to downstream requests

### 4. Retry Configuration

Retry patterns are customized per service:

```yaml
resilience4j:
  retry:
    instances:
      booking-service:
        maxAttempts: 4
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        enableRandomizedWait: true
```

**Service-specific retry strategies:**
- **Booking Service**: 4 attempts with exponential backoff
- **Payment Service**: Only 2 attempts (financial operations)
- **User Service**: 3 attempts with randomized wait

### 5. Time Limiting

Timeout configurations prevent hanging requests:

```yaml
resilience4j:
  timelimiter:
    instances:
      payment-service: 15s  # Longest timeout for payment processing
      booking-service: 10s  # Moderate timeout for bookings
      search-service: 3s    # Fast timeout for search operations
```

### 6. Bulkhead Pattern

Bulkhead isolation limits concurrent calls per service:

```yaml
resilience4j:
  bulkhead:
    instances:
      booking-service:
        maxConcurrentCalls: 50
      payment-service:
        maxConcurrentCalls: 20
```

## 🛡️ Fallback Mechanisms

### Fallback Controllers

Each service has dedicated fallback endpoints that provide meaningful responses during failures:

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/booking")
    public Mono<ResponseEntity<Map<String, Object>>> bookingFallback() {
        // Returns user-friendly error message
        // Includes support contact information
        // Provides actionable suggestions
    }
}
```

**Fallback Features:**
- **User-friendly Error Messages**: Clear explanation of the issue
- **Support Information**: Contact details for assistance
- **Actionable Suggestions**: What users can do next
- **Service Status**: Which specific service is affected

### Circuit Breaker Fallbacks

Service-specific fallbacks maintain functionality during outages:

- **Booking Service**: "Service temporarily unavailable, no charges made"
- **Payment Service**: Includes emergency contact information
- **Search Service**: Returns empty results with suggestions
- **Authentication**: Advises retry with caching considerations

## 📊 Monitoring & Observability

### Metrics Collection

Comprehensive metrics are exposed through:

1. **Prometheus Metrics**: All resilience patterns expose metrics
2. **Health Indicators**: Custom health checks for each pattern
3. **Actuator Endpoints**: Dedicated endpoints for each resilience component

### Available Endpoints

```
/actuator/health          - Overall health including resilience patterns
/actuator/metrics         - Detailed metrics for all patterns
/actuator/prometheus      - Prometheus-formatted metrics
/actuator/circuitbreakers - Circuit breaker status
/actuator/ratelimiters    - Rate limiter status
/actuator/retries         - Retry status
/actuator/timelimiters    - Time limiter status
/actuator/bulkheads       - Bulkhead status
```

### Custom Metrics

The system records custom metrics every 30 seconds:

```java
@Scheduled(fixedRate = 30000)
public void recordCustomMetrics() {
    // Circuit breaker states
    // Rate limiter available permissions
    // Bulkhead available capacity
    // Failure rates and response times
}
```

### Alerting

Automated health monitoring with logging:

- **Circuit Breaker OPEN**: Service unavailability alerts
- **Rate Limiter Exhausted**: Traffic spike alerts
- **Bulkhead Full**: Capacity alerts
- **High Failure Rates**: Performance degradation alerts

## 🚀 Usage Examples

### 1. Making Resilient API Calls

```java
// Routes automatically include resilience patterns
.route("booking-service-enhanced", r -> r
    .path("/api/bookings/**")
    .filters(f -> f
        .filter(enhancedJwtAuthFilter())
        .circuitBreaker(c -> c
            .setName("booking-service")
            .setFallbackUri("forward:/fallback/booking"))
        .retry(retryConfig -> retryConfig.setRetries(4))
        .requestRateLimiter(rl -> rl
            .setRateLimiter(rateLimiterRegistry.rateLimiter("booking-requests")))
    )
    .uri("lb://booking-service"))
```

### 2. JWT Authentication with Caching

```java
// Automatic caching and rate limiting
String authHeader = "Bearer eyJhbGciOiJIUzUxMiJ9...";
// Token validation uses Redis cache
// Rate limiting prevents abuse
// Circuit breaker protects validation service
```

### 3. Monitoring Circuit Breaker State

```bash
# Check circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# Check rate limiter status
curl http://localhost:8080/actuator/ratelimiters

# View Prometheus metrics
curl http://localhost:8080/actuator/prometheus | grep resilience4j
```

## 🔧 Configuration Guidelines

### Environment-Specific Tuning

**Development:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        failureRateThreshold: 80  # More permissive
        waitDurationInOpenState: 10s  # Faster recovery
```

**Production:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        failureRateThreshold: 50  # Stricter
        waitDurationInOpenState: 60s  # Conservative recovery
```

### Performance Considerations

1. **Cache TTL**: JWT tokens cached for 5 minutes (balance security vs. performance)
2. **Rate Limits**: Set based on expected traffic patterns
3. **Circuit Breaker Windows**: Sized for meaningful statistical sampling
4. **Retry Delays**: Exponential backoff to prevent thundering herd

## 🧪 Testing

### Unit Tests

- **JwtTokenValidatorTest**: Tests resilience annotations and fallbacks
- **EnhancedJwtAuthenticationFilterTest**: Tests caching and rate limiting
- **ResilienceGatewayFilterFactoryTest**: Tests pattern combinations

### Integration Tests

- **ResilienceIntegrationTest**: Tests actual pattern behavior
- **CircuitBreakerIntegrationTest**: Tests state transitions
- **RateLimiterIntegrationTest**: Tests throttling behavior

### Load Testing

```bash
# Test rate limiting
for i in {1..20}; do curl -H "Authorization: Bearer token" localhost:8080/api/bookings & done

# Test circuit breaker
# (Simulate downstream service failures)
```

## 🔮 Future Enhancements

1. **Adaptive Rate Limiting**: Dynamic limits based on system load
2. **Machine Learning**: Predictive circuit breaker thresholds
3. **Distributed Tracing**: Enhanced observability with Zipkin/Jaeger
4. **Chaos Engineering**: Automated failure injection testing
5. **Multi-Region Fallbacks**: Geographic failover capabilities

## 📚 References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Rate Limiting Strategies](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)

---

*This implementation provides enterprise-grade resilience patterns suitable for high-traffic production environments while maintaining simplicity and observability.*
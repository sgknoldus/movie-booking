# Movie Booking System - Microservices Architecture

A robust, scalable movie ticket booking platform built using Spring Boot microservices architecture and Domain-Driven Design (DDD) principles.

## System Architecture

![Architecture Diagram](arc.png)

### Architecture Flow Across Microservices

The Movie Booking System follows a distributed microservices architecture with the following request flow:

#### 1. User Registration & Authentication Flow
```
Client → API Gateway → User Service → PostgreSQL (user_db)
                   ↓
               JWT Token Generation
                   ↓
            Response to Client
```

#### 2. Movie Search & Discovery Flow
```
Client → API Gateway → Search Service → Elasticsearch
                           ↑
                   Kafka Event Listener
                           ↑
              Theatre Service (publishes events)
```

#### 3. Theatre & Show Management Flow
```
Admin/System → API Gateway → Theatre Service → PostgreSQL (theatre_db)
                                    ↓
                            Kafka Producer (publishes events)
                                    ↓
                    Search Service (updates Elasticsearch index)
```

#### 4. Booking Process Flow
```
Client → API Gateway → Booking Service → PostgreSQL (booking_service)
                           ↓
                    Payment Service → PostgreSQL (payment_service)
                           ↓
                    Kafka Producer (publishes booking events)
                           ↓
              ┌─ Ticket Service (generates tickets)
              └─ Notification Service (sends confirmations)
```

#### 5. Complete Booking Transaction Flow
```
1. User selects seats → Booking Service (seat locking)
2. Payment processing → Payment Service
3. Booking confirmation → Kafka Event (BookingConfirmedEvent)
4. Parallel processing:
   ├─ Ticket Service → Generates QR code tickets
   ├─ Notification Service → Sends email confirmation
   └─ Search Service → Updates seat availability
```

#### 6. Service Communication Patterns

**Synchronous Communication (REST APIs):**
- Client ↔ API Gateway ↔ Individual Services
- Inter-service calls for immediate responses

**Asynchronous Communication (Kafka Events):**
- Theatre Service → Search Service (theatre/show updates)
- Booking Service → Notification Service (booking confirmations)
- Booking Service → Ticket Service (ticket generation)
- Payment Service → Notification Service (payment confirmations)

#### 7. Data Consistency & Transaction Management

**Database per Service Pattern:**
- Each microservice maintains its own database
- Eventual consistency through event-driven updates
- Transactional outbox pattern for reliable event publishing

**Cross-Service Data Synchronization:**
- Kafka events ensure data consistency across services
- Search Service maintains denormalized data for fast queries
- Compensation patterns for distributed transaction failures



## Technology Stack

- **Framework**: Spring Boot 3.2+, Spring Cloud 2023.0+
- **Database**: PostgreSQL (primary), Redis (caching), Elasticsearch (search)
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **Documentation**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5, Testcontainers

## Project Structure

```
movie-booking-system/
├── api-gateway/               # API Gateway Service
├── booking-service/           # Booking Management Service
├── common-lib/               # Shared Library
├── discovery-service/        # Eureka Service Discovery
├── notification-service/     # Notification Service
├── payment-service/          # Payment Processing Service
├── search-service/           # Search Service with Elasticsearch
├── theatre-service/          # Theatre Management Service
├── ticket-service/           # Ticket Management Service
└── user-service/            # User Management Service
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

## Local Development Setup

For detailed setup instructions, sample API calls, and testing procedures, see [LOCAL_SETUP.md](LOCAL_SETUP.md)

Quick Start:

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd movie-booking-system
   ```

2. Install prerequisites (PostgreSQL, Redis, Kafka, Elasticsearch):
   ```bash
   # Using provided script
   ./scripts/install-prerequisites.sh
   ```

3. Start infrastructure services:
   ```bash
   docker compose up -d
   ```

4. Build all services:
   ```bash
   ./mvnw clean install
   ```

5. Start all services in correct order:
   ```bash
   # Using provided script
   ./start-services.sh
   ```

6. Verify setup at http://localhost:8761 (Eureka Dashboard)

## Detailed Service Descriptions

### 1. Discovery Service (Port: 8761)
- Service registry and discovery using Netflix Eureka
- Enables dynamic service registration and discovery
- Dashboard available at: http://localhost:8761

### 2. API Gateway (Port: 8080)
- Single entry point for all client requests
- Features:
  - Route management
  - Authentication & Authorization
  - Rate limiting
  - Request/Response transformation
  - Circuit breaking
- All API endpoints are prefixed with `/api/v1`

### 3. User Service (Port: 8083)
- Handles user management and authentication
- Key Features:
  - User registration and login
  - JWT token generation and validation
  - User profile management
- Key Endpoints:
  - POST `/api/v1/auth/register`: User registration
  - POST `/api/v1/auth/login`: User authentication
  - GET `/api/v1/users/profile`: Get user profile
  - PUT `/api/v1/users/profile`: Update user profile

### 4. Theatre Service (Port: 8081)
- Manages theatres and show schedules
- Key Features:
  - Theatre management
  - Screen management
  - Show scheduling
  - Seat layout management
- Key Endpoints:
  - GET `/api/v1/theatres`: List theatres
  - GET `/api/v1/theatres/{id}/shows`: Get theatre shows
  - GET `/api/v1/shows/{id}/seats`: Get show seat layout

### 5. Search Service (Port: 8082)
- Handles movie search functionality using Elasticsearch
- Key Features:
  - Advanced movie search capabilities
  - Elasticsearch-based indexing and querying
  - Event-driven data synchronization via Kafka
- Key Endpoints:
  - GET `/api/v1/search/movies`: Search movies
  - GET `/api/v1/search/movies/{id}`: Get movie details

### 6. Ticket Service (Port: 8084)
- Manages ticket generation and lifecycle
- Key Features:
  - Ticket creation and validation
  - QR code generation
  - Ticket status management
- Key Endpoints:
  - POST `/api/v1/tickets`: Generate ticket
  - GET `/api/v1/tickets/{id}`: Get ticket details
  - PUT `/api/v1/tickets/{id}/validate`: Validate ticket

### 7. Booking Service (Port: 8087)
- Handles ticket booking and seat management
- Key Features:
  - Seat selection and locking
  - Booking workflow management
  - Integration with payment service
- Key Endpoints:
  - POST `/api/v1/bookings`: Create booking
  - GET `/api/v1/bookings/{id}`: Get booking details
  - PUT `/api/v1/bookings/{id}/cancel`: Cancel booking

### 8. Payment Service (Port: 8085)
- Manages payment processing
- Key Features:
  - Payment processing
  - Multiple payment method support
  - Payment status tracking
- Key Endpoints:
  - POST `/api/v1/payments`: Process payment
  - GET `/api/v1/payments/{id}`: Get payment status

### 9. Notification Service (Port: 8086)
- Handles system notifications
- Key Features:
  - Email notifications
  - Event-driven architecture using Kafka
  - HTML email templates
- Key Endpoints:
  - POST `/api/v1/notifications`: Send notification
  - GET `/api/v1/notifications/user/{userId}`: Get user notifications

## Infrastructure Services

- **PostgreSQL**: 5432 (Multiple databases for each service)
- **Redis**: 6379 (Caching and distributed locking)
- **Elasticsearch**: 9200 (Search functionality)
- **Apache Kafka**: 9092 (Event streaming)
- **Zookeeper**: 2181 (Kafka coordination)
- **Kafka UI**: 8090 (Kafka management interface)

## Kafka Event Driven Architecture

The system leverages Apache Kafka for asynchronous, event-driven communication between microservices, ensuring loose coupling and scalability.

### Event Flow Architecture

#### Core Kafka Topics
- **theatre-events**: Theatre, screen, show, and seat availability events
- **booking-confirmed**: Successful booking confirmation events
- **booking-cancelled**: Booking cancellation events
- **payment-completed**: Payment processing completion events

### Event Producers & Consumers

#### Theatre Service (Producer)
- **Publishes Events:**
  - City creation/update/deletion → `theatre-events`
  - Theatre creation/update/deletion → `theatre-events`
  - Screen creation/update/deletion → `theatre-events`
  - Show creation/update/deletion → `theatre-events`
  - Seat availability changes → `theatre-events`

```java
// Example: Theatre Service publishes events
@Service
public class TheatreEventPublisher {
    @KafkaTransactional
    public void publishTheatreEvent(String eventType, Object eventData) {
        kafkaTemplate.send("theatre-events", eventKey, jsonData);
    }
}
```

#### Search Service (Consumer)
- **Consumes Events:**
  - Theatre events → Updates Elasticsearch indices
  - Show events → Maintains search-optimized show data
  - Seat availability → Updates show seat counts

```java
// Example: Search Service consumes theatre events
@KafkaListener(topics = "theatre-events", groupId = "search-service-group")
public void handleTheatreEvent(String eventData) {
    // Process and index in Elasticsearch
}
```

#### Booking Service (Producer)
- **Publishes Events:**
  - Booking confirmation → `booking-confirmed`
  - Booking cancellation → `booking-cancelled`

#### Notification Service (Consumer)
- **Consumes Events:**
  - Booking confirmations → Sends email notifications
  - Booking cancellations → Sends cancellation emails
  - Payment completions → Sends payment confirmations

#### Ticket Service (Consumer)
- **Consumes Events:**
  - Booking confirmations → Generates QR code tickets
  - Updates ticket status based on booking events

### Event Processing Patterns

#### 1. Event Sourcing
- All domain events are stored in Kafka for audit and replay
- Event store serves as source of truth for state changes
- Services can reconstruct state by replaying events

#### 2. SAGA Pattern Implementation
```
Booking Process SAGA:
1. Booking Service → Create Booking (Compensatable)
2. Payment Service → Process Payment (Compensatable)
3. Ticket Service → Generate Ticket (Compensatable)
4. Notification Service → Send Confirmation (Final)

On Failure: Compensation events trigger rollback
```

#### 3. Eventual Consistency
- Services maintain local consistency
- Cross-service consistency achieved through event propagation
- Compensating actions handle inconsistencies

#### 4. Dead Letter Queue (DLQ)
- Failed event processing → Retry mechanism
- Max retries exceeded → Dead letter topic
- Manual intervention for poison messages

### Kafka Configuration Highlights

#### Producer Configuration (Exactly-Once Semantics)
```yaml
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      enable-idempotence: true
      transaction-id-prefix: service-tx-
```

#### Consumer Configuration (At-Least-Once Delivery)
```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest
      enable-auto-commit: false
      group-id: service-group
      isolation-level: read_committed
```


## Service Management

1. Service Registry (Eureka):
   - Dashboard: http://localhost:8761

## API Documentation

All services expose their API documentation through the API Gateway:
- **User Service**: http://localhost:8080/user-service/swagger-ui.html
- **Theatre Service**: http://localhost:8080/theatre-service/swagger-ui.html
- **Search Service**: http://localhost:8080/search-service/swagger-ui.html
- **Booking Service**: http://localhost:8080/booking-service/swagger-ui.html
- **Ticket Service**: http://localhost:8080/ticket-service/swagger-ui.html
- **Payment Service**: http://localhost:8080/payment-service/swagger-ui.html
- **Notification Service**: http://localhost:8080/notification-service/swagger-ui.html

Direct service access:
```
http://localhost:{service-port}/swagger-ui.html
```

## Testing
Each service includes:
- Unit Tests
- Integration Tests
- API Tests using RestAssured
- Performance Tests using JMeter

To run tests:
```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify -P integration-test

# Run all tests including performance tests
./mvnw verify -P all-tests
```


## Security Measures
- JWT-based authentication
- Role-based access control (RBAC)
- API Gateway security policies
- SSL/TLS encryption
- Rate limiting per user/IP
- Input validation and sanitization
- XSS protection
- CORS configuration
- Secure password hashing
- Audit logging

## Database Structure

Each service maintains its own PostgreSQL database:
- **user_service**: User profiles, authentication data
- **theatre_db**: Theatres, screens, shows, seats
- **booking_service**: Bookings, seat reservations
- **payment_service**: Payment records, transaction history
- **notification_service**: Notification logs, templates
- **ticket_service**: Ticket details, QR codes
- **search_service**: No database (uses Elasticsearch)

## Deployment
- Containerized using Docker
- Docker Compose for local development
- Environment-specific configurations
- Service mesh ready
- Database per service pattern

## Contributing
1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Add/update tests
5. Update documentation
6. Submit a pull request

## Troubleshooting
Common issues and solutions:
1. Services not registering with Eureka:
   - Check if Eureka server is running
   - Verify network connectivity
   - Check service configurations

2. Database connection issues:
   - Verify PostgreSQL is running
   - Check connection credentials
   - Validate database exists

3. Kafka connectivity:
   - Ensure Kafka broker is running
   - Check topic configurations
   - Verify consumer group IDs

## License
This project is licensed under the MIT License - see the LICENSE file for details

## Support
For bug reports and feature requests:
- Create an issue in the GitHub repository
- Contact the maintainers
- Check the documentation

---
Last Updated: September 2025

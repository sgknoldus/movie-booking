# Movie Booking System - Microservices Architecture

A robust, scalable movie ticket booking platform built using Spring Boot microservices architecture and Domain-Driven Design (DDD) principles.

## System Architecture



![Architecture Diagram](arc.png)



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

# Event Management System

A comprehensive RESTful API application for creating and managing events with features for user authentication, event registration, notifications, and email confirmations.

## 🌟 Features

### Core Features
- **User Management** - Registration, authentication, and profile management
- **Event Management** - Create, update, delete, and browse events
- **Event Registration** - Register/cancel event attendance with email notifications
- **Category System** - Organize events by categories
- **Ticket Management** - Generate and validate event tickets
- **Notifications** - Real-time notifications for user activities
- **Email Service** - Automated email confirmations for registrations

### Security & Authentication
- JWT-based authentication and authorization
- Role-based access control (USER, ORGANIZER, ADMIN)
- Secure password encryption with BCrypt
- Protected API endpoints with Spring Security

### API & Documentation
- RESTful API design
- Comprehensive API documentation with Swagger/OpenAPI 3
- Interactive API testing via Swagger UI
- Paginated responses for large datasets

### Email Integration
- SendGrid Web API integration (works on Render free tier)
- Email notifications for registrations and cancellations
- HTML email templates
- Async email sending for better performance

## 🛠 Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database ORM
- **Hibernate** - JPA implementation

### Database
- **PostgreSQL** - Production database
- **H2** - In-memory database for testing

### Email Service
- **Spring Mail** - SMTP email support (local development)
- **SendGrid Java SDK 4.10.2** - Web API for cloud deployment

### Security
- **JWT (jjwt 0.11.5)** - Token-based authentication
- **BCrypt** - Password hashing

### Documentation
- **SpringDoc OpenAPI 3 (v2.8.5)** - API documentation

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Security Test** - Security testing
- **Testcontainers 1.19.3** - Integration testing with containers
- **REST Assured 5.3.2** - API testing

### Build & Deployment
- **Maven** - Dependency management and build
- **Docker** - Containerization
- **Render** - Cloud deployment platform

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/taingy/eventmanagementsystem/
│   │   ├── config/              # Application configuration
│   │   │   ├── SwaggerConfig.java
│   │   │   └── AsyncConfig.java
│   │   ├── controller/          # REST API endpoints
│   │   │   ├── AuthController.java
│   │   │   ├── EventController.java
│   │   │   ├── RegistrationController.java
│   │   │   ├── UserController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── TicketController.java
│   │   │   └── SummaryController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── AuthRequests.java
│   │   │   ├── EventRequestDTO.java
│   │   │   ├── EventResponseDTO.java
│   │   │   ├── RegistrationRequestDTO.java
│   │   │   └── ...
│   │   ├── enums/               # Enumerations
│   │   │   ├── Role.java
│   │   │   ├── EventStatus.java
│   │   │   ├── RegistrationStatus.java
│   │   │   └── TicketStatus.java
│   │   ├── exception/           # Custom exceptions
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── ...
│   │   ├── mapper/              # DTO mapping
│   │   │   ├── EventMapper.java
│   │   │   ├── UserMapper.java
│   │   │   └── ...
│   │   ├── model/               # JPA entities
│   │   │   ├── User.java
│   │   │   ├── Event.java
│   │   │   ├── Registration.java
│   │   │   ├── Category.java
│   │   │   ├── Ticket.java
│   │   │   └── Notification.java
│   │   ├── repository/          # Data access layer
│   │   │   ├── UserRepository.java
│   │   │   ├── EventRepository.java
│   │   │   └── ...
│   │   ├── security/            # Security configuration
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtUtil.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CustomUserDetailsService.java
│   │   ├── service/             # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── EventService.java
│   │   │   ├── RegistrationService.java
│   │   │   ├── EmailService.java
│   │   │   ├── SendGridEmailService.java
│   │   │   └── ...
│   │   └── util/                # Utility classes
│   │       └── AuthUtil.java
│   └── resources/
│       ├── application.properties
│       ├── application-local.properties
│       └── application-prod.properties
└── test/                        # Test classes
    ├── java/
    └── resources/
```

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **PostgreSQL** database
- **Maven 3.6+** (or use included Maven Wrapper)
- **SendGrid Account** (for email features in production)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd event-management-system
   ```

2. **Configure database**

   Update `src/main/resources/application-local.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/event_management_system
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Configure email (optional for local)**

   For local email testing, update `application-local.properties` with your SendGrid API key:
   ```properties
   spring.mail.username=apikey
   spring.mail.password=YOUR_SENDGRID_API_KEY
   ```

4. **Run the application**
   ```bash
   # Using Maven Wrapper (recommended)
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local

   # Or using Maven
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

5. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/v3/api-docs`

### Building for Production

```bash
# Build JAR file
./mvnw clean package

# Build Docker image
docker build -t event-management-system .

# Run Docker container
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/dbname \
  -e DATABASE_PASSWORD=password \
  -e JWT_SECRET=your-secret \
  -e SENDGRID_API_KEY=your-api-key \
  event-management-system
```

## 🧪 Testing

### Run all tests
```bash
./mvnw test
```

### Run specific test class
```bash
./mvnw test -Dtest=EventControllerTest
```

### Run integration tests
```bash
./mvnw test -Dtest="*IntegrationTest"
```

## 📧 Email Configuration

This application supports two email sending methods:

### Local Development (SMTP)
Uses Spring Mail with SendGrid SMTP for local testing.

### Production (SendGrid Web API)
Uses SendGrid HTTP API to bypass SMTP port restrictions on cloud platforms like Render.

## 🌐 Deployment

### Render Platform

This application is configured for deployment on Render with the following setup:

**Required Environment Variables:**
```
DATABASE_URL=<neon-postgres-url>
DATABASE_PASSWORD=<database-password>
JWT_SECRET=<secure-random-string>
SENDGRID_API_KEY=<sendgrid-api-key>
SENDGRID_FROM_EMAIL=<verified-email>
SPRING_PROFILES_ACTIVE=prod
PORT=10000
```

**Build Command:**
```bash
./mvnw clean package -DskipTests
```

**Start Command:**
```bash
java -Dserver.port=$PORT -jar target/event-management-system-0.0.1-SNAPSHOT.jar
```

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login (returns JWT token)

### Event Endpoints
- `GET /api/events` - List all events (with pagination)
- `GET /api/events/{id}` - Get event details
- `POST /api/events` - Create new event (ORGANIZER)
- `PUT /api/events/{id}` - Update event (ORGANIZER)
- `DELETE /api/events/{id}` - Delete event (ORGANIZER)

### Registration Endpoints
- `POST /api/registrations` - Register for an event
- `GET /api/registrations/user/{userId}` - Get user's registrations
- `GET /api/registrations/event/{eventId}` - Get event's registrations
- `PUT /api/registrations/{id}/cancel` - Cancel registration

### User Endpoints
- `GET /api/users` - List all users (ADMIN)
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user profile
- `DELETE /api/users/{id}` - Delete user (ADMIN)

### Category Endpoints
- `GET /api/categories` - List all categories
- `POST /api/categories` - Create category (ADMIN)
- `PUT /api/categories/{id}` - Update category (ADMIN)
- `DELETE /api/categories/{id}` - Delete category (ADMIN)

### Ticket Endpoints
- `GET /api/tickets/registration/{registrationId}` - Get ticket for registration
- `PUT /api/tickets/{id}/validate` - Validate ticket (ORGANIZER)

For complete API documentation with request/response examples, visit the Swagger UI when the application is running.

## 🔐 Security

### Authentication Flow
1. User registers or logs in via `/api/auth/register` or `/api/auth/login`
2. Server returns JWT token in response
3. Client includes token in `Authorization: Bearer <token>` header for protected endpoints
4. Server validates token and grants access based on user role

### Roles & Permissions
- **USER** - Can register for events, manage own profile
- **ORGANIZER** - Can create and manage events, validate tickets
- **ADMIN** - Full system access, user management

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


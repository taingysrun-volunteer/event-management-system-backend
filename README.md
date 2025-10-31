# Event Management System

An application to create and manage events, including features for user authentication, event creation, registration, and notifications.

## Tech Stack

### Backend Framework
- **Spring Boot 3.5.7** - Main application framework
- **Java 17** - Programming language

### Core Dependencies

#### Spring Modules
- **Spring Boot Starter Web** - RESTful web services and MVC support
- **Spring Boot Starter Data JPA** - Database access with JPA/Hibernate
- **Spring Boot Starter Data REST** - Automatic REST API generation for repositories
- **Spring Boot Starter Security** - Authentication and authorization
- **Spring Boot Starter Actuator** - Application monitoring and management

#### Database
- **PostgreSQL** - Primary relational database

#### Security & Authentication
- **JWT (JSON Web Tokens)** - Token-based authentication
  - jjwt-api 0.11.5
  - jjwt-impl 0.11.5
  - jjwt-jackson 0.11.5

#### API Documentation
- **SpringDoc OpenAPI 3** (v2.8.5) - API documentation and Swagger UI
  - Access Swagger UI at: `/swagger-ui.html`
  - OpenAPI JSON available at: `/v3/api-docs`

#### Development Tools
- **Lombok 1.18.42** - Reduces boilerplate code with annotations
- **Spring Boot DevTools** - Development-time features

#### Testing
- **Spring Boot Starter Test** - Testing support with JUnit, Mockito, and more

### Build Tool
- **Maven** - Dependency management and build automation
  - Maven Wrapper included (`mvnw` / `mvnw.cmd`)

## Project Structure

```
src/
├── main/
│   └── java/com/taingy/eventmanagementsystem/
│       ├── config/          # Configuration classes (Swagger, etc.)
│       ├── controller/      # REST API controllers
│       ├── dto/            # Data Transfer Objects
│       ├── exception/      # Custom exceptions and error handlers
│       ├── model/          # Entity classes
│       ├── repository/     # Data access layer
│       ├── security/       # Security configuration and filters
│       └── service/        # Business logic layer
└── test/                   # Test classes
```

## Features

- User authentication and authorization with JWT
- Role-based access control (RBAC)
- RESTful API endpoints
- API documentation with Swagger/OpenAPI
- Database persistence with PostgreSQL
- Exception handling with global error handlers
- Application monitoring with Spring Actuator

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL database
- Maven (or use included Maven Wrapper)

### Running the Application

Using Maven Wrapper:
```bash
./mvnw spring-boot:run
```

Using Maven:
```bash
mvn spring-boot:run
```

### Building the Application

```bash
./mvnw clean package
```

The built JAR file will be available in the `target/` directory.

## API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## Configuration

Configure your database and other settings in `application.properties` file.


# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Package the application
./mvnw clean package

# Run a specific test class
./mvnw test -Dtest=CarRentalApplicationTests
```

## Database Setup

The application uses PostgreSQL. Start the database with Docker Compose:

```bash
# Start PostgreSQL database
docker-compose up -d

# Stop database
docker-compose down
```

**Database Configuration**:
- Database: `carrental_db`
- Username: `carrental`
- Password: `123456` (in application.properties)
- Port: 5432
- Flyway migrations are disabled (`spring.flyway.enabled=false`)
- Hibernate DDL auto-update is enabled

## Project Architecture

**Spring Boot Car Rental System** with layered architecture:

- **Controllers** (`/controller`): REST endpoints with `@RestController`
- **Services** (`/services`): Business logic layer
- **Repositories** (`/repository`): JPA data access layer extending standard repositories
- **Models** (`/model`): JPA entities with inheritance hierarchy

**Key Architecture Patterns**:
- **Inheritance Mapping**: `UserModel` is abstract base class using `JOINED` inheritance strategy
- **Entity Hierarchy**: `ClientModel` and `EmployeeModel` extend `UserModel`
- **Dependency Injection**: Constructor-based injection used throughout
- **RESTful APIs**: Standard CRUD operations exposed via REST controllers

## Security Configuration

- **Basic HTTP Authentication** enabled via Spring Security
- **CSRF disabled** for development (`csrf().disable()`)
- **Default credentials**: username=`mateo`, password=`1234`, role=`ADMIN`
- All endpoints require authentication (`anyRequest().authenticated()`)

## Key Technologies

- **Spring Boot**: 3.5.5
- **Java**: 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Build Tool**: Maven
- **Testing**: JUnit 5 with Spring Boot Test
- **Code Generation**: Lombok annotations (`@Getter`, `@Setter`, `@Entity`, etc.)
- **Server Port**: 8083

## Database Schema Notes

- Uses JPA inheritance with separate tables for each entity (`JOINED` strategy)
- Primary keys are auto-generated using `IDENTITY` strategy
- Entity scanning configured for `com.example.carrental.model` package
- SQL logging enabled in development (`spring.jpa.show-sql=true`)
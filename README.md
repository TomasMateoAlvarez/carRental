# 🚗 Car Rental SaaS Platform

Modern multi-tenant car rental management system built with Spring Boot.

## ⚡ Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (for local development)
- Maven 3.8+ (for local development)

### 🚀 Start Development Environment
```bash
# Clone and setup
git clone <your-repo>
cd CarRental

# Copy environment template
cp .env.example .env
# Edit .env with your credentials

# Start all services
./start-dev.sh
```

### 🌐 Access Points
- **Application**: http://localhost:8083
- **Health Check**: http://localhost:8083/actuator/health
- **API Documentation**: http://localhost:8083/api/v1
- **Prometheus Metrics**: http://localhost:9090
- **Database**: localhost:5432

### 🔐 Default Credentials
- **Username**: admin
- **Password**: admin123

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.5.5, Java 17
- **Database**: PostgreSQL 16
- **Security**: Spring Security with BCrypt
- **Monitoring**: Actuator + Prometheus
- **Containerization**: Docker + Docker Compose
- **Caching**: Redis (ready for implementation)

### Project Structure
```
src/main/java/com/example/carrental/
├── controller/          # REST API endpoints
├── service/            # Business logic
├── repository/         # Data access layer
├── model/              # JPA entities
├── dto/                # Data transfer objects
├── mapper/             # Entity ↔ DTO conversion
├── exception/          # Custom exceptions & error handling
├── config/             # Spring configuration
├── enums/              # Enumerations (VehicleStatus, etc.)
├── health/             # Custom health indicators
└── info/               # Application info contributors
```

## 🚙 API Endpoints

### Vehicle Management
```http
GET    /api/v1/vehicles                 # List all vehicles
POST   /api/v1/vehicles                 # Create vehicle
GET    /api/v1/vehicles/{id}            # Get vehicle by ID
PUT    /api/v1/vehicles/{id}            # Update vehicle
DELETE /api/v1/vehicles/{id}            # Delete vehicle
PATCH  /api/v1/vehicles/{id}/status     # Change vehicle status

# Business operations
GET    /api/v1/vehicles/available       # Available vehicles
GET    /api/v1/vehicles/available/category/{category}
GET    /api/v1/vehicles/available/price-range?minRate=50&maxRate=100
GET    /api/v1/vehicles/maintenance-needed
GET    /api/v1/vehicles/search?q=Toyota
```

### Client Management
```http
GET    /clients                         # List all clients
POST   /clients                         # Create client
GET    /clients/{id}                    # Get client by ID
PUT    /clients/{id}                    # Update client
DELETE /clients/{id}                    # Delete client
```

## 🔄 Vehicle Status Flow

```
AVAILABLE → RESERVED → RENTED → AVAILABLE
    ↓           ↓          ↓
OUT_OF_SERVICE  ↓     MAINTENANCE
    ↓           ↓          ↓
MAINTENANCE     ↓      WASHING
    ↓           ↓          ↓
IN_REPAIR   AVAILABLE  AVAILABLE
```

## 📊 Monitoring & Health

### Health Checks
```bash
# Application health
curl http://localhost:8083/actuator/health

# Database health
curl http://localhost:8083/actuator/health/db

# Custom health indicators
curl http://localhost:8083/actuator/health/database
```

### Metrics
```bash
# Prometheus metrics
curl http://localhost:8083/actuator/prometheus

# Application metrics
curl http://localhost:8083/actuator/metrics

# Specific metric
curl http://localhost:8083/actuator/metrics/jvm.memory.used
```

## 🛠️ Development

### Local Development (without Docker)
```bash
# Start PostgreSQL
docker-compose up -d db

# Set environment variables
export DATABASE_PASSWORD=secretpassword123
export ADMIN_PASSWORD=admin123

# Run application
./mvnw spring-boot:run
```

### Build & Test
```bash
# Compile
./mvnw clean compile

# Run tests
./mvnw test

# Package
./mvnw clean package

# Run with profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker
```

### Docker Commands
```bash
# Build image
docker build -t carrental-app .

# Run services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up --build -d
```

## 🔒 Security Features

- ✅ Environment-based configuration
- ✅ Password hashing with BCrypt
- ✅ Input validation with Bean Validation
- ✅ Global exception handling
- ✅ Structured logging
- ✅ Health checks with authentication
- ✅ CORS configuration
- 🔄 JWT authentication (planned)
- 🔄 Role-based access control (planned)

## 🚀 Next Steps (Roadmap)

### Phase 1: Multi-Tenancy
- [ ] Tenant management system
- [ ] Row-level security
- [ ] Tenant-aware repositories

### Phase 2: Advanced Features
- [ ] JWT authentication
- [ ] Role-based permissions
- [ ] Rental management system
- [ ] Payment processing
- [ ] Notification system

### Phase 3: SaaS Features
- [ ] Subscription management
- [ ] Analytics dashboard
- [ ] API rate limiting
- [ ] White-label support

## 📝 Environment Variables

Create `.env` file from `.env.example`:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/carrental_db
DATABASE_USERNAME=carrental
DATABASE_PASSWORD=your_secure_password

# Admin User
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_admin_password

# JWT (future)
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License.

---

Built with ❤️ for modern car rental businesses
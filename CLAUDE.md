# CLAUDE.md - Documentation v3

This file provides guidance to Claude Code (claude.ai/code) when working with the CarRental Enterprise SaaS platform.

## Project Overview

**CarRental** is a comprehensive multi-tenant SaaS platform for car rental businesses with:
- **Backend**: Spring Boot REST API with enterprise features and JWT authentication
- **Frontend**: React + TypeScript + Vite application with Ant Design UI
- **Mobile App**: React Native cross-platform mobile application
- **Infrastructure**: Production-ready containerized deployment with monitoring
- **Integration**: Full JWT authentication, Stripe payments, and multi-tenant architecture

## Enterprise Features (v3)

### 🚀 **Advanced Business Capabilities**
- **Multi-Tenancy**: Complete isolation for multiple car rental companies
- **Stripe Payments**: Full payment processing with refunds and fee management
- **Advanced Analytics**: ML-powered insights and predictive analytics
- **Automated Reporting**: Daily, weekly, and monthly business reports
- **Notifications**: Multi-channel (Email/SMS/Push) notification system
- **Mobile-First**: React Native app with full feature parity

### 📊 **Business Intelligence & Analytics**
- Customer Lifetime Value (CLV) calculation with segmentation
- Demand forecasting with seasonal patterns
- Vehicle performance analytics
- Customer churn prediction
- Price optimization insights
- Revenue optimization recommendations

### 💳 **Payment & Billing System**
- Stripe integration for secure payments
- Subscription billing with usage fees
- Automated invoice generation
- Refund processing
- Fee breakdown (tax, processing, discounts)
- Revenue reporting and analytics

## Application Structure

### Frontend Application

**Location**: `/Users/mateoalvarez/IdeaProjects/carrental-frontend/`

#### Frontend Development Commands

```bash
# Navigate to frontend directory
cd /Users/mateoalvarez/IdeaProjects/carrental-frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

#### Frontend Architecture

- **Framework**: React 18 + TypeScript + Vite
- **UI Library**: Ant Design (antd)
- **State Management**: Zustand for auth, React Query for server state
- **Routing**: React Router v6
- **API Client**: Axios with JWT interceptors
- **Dev Server**: http://localhost:5173 or 5174

#### Frontend Key Features

- **Authentication**: JWT-based login/register with token management
- **Vehicle Management**: CRUD operations for vehicles with search/filter
- **Reservation System**: Create and manage vehicle reservations
- **Dashboard**: Business KPIs and analytics overview
- **Payment Integration**: Stripe checkout and payment history
- **Responsive Design**: Mobile-friendly UI with Ant Design

### Backend Application

**Location**: `/Users/mateoalvarez/IdeaProjects/CarRental/` (current directory)

#### Backend Development Commands

```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application (PostgreSQL)
./mvnw spring-boot:run

# Run with H2 in-memory database (for development)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Package the application
./mvnw clean package
```

#### Backend Architecture

**Enterprise Spring Boot SaaS Platform** with layered architecture:

- **Controllers** (`/controller`): REST endpoints with enterprise features
- **Services** (`/services`): Business logic with advanced analytics
- **Repositories** (`/repository`): JPA data access with multi-tenant support
- **Models** (`/model`): JPA entities with tenant isolation
- **Security**: JWT authentication with Spring Security
- **Payments**: Stripe integration for payment processing
- **Analytics**: ML-powered business insights
- **Notifications**: Multi-channel notification system
- **API Base Path**: `/api/v1`
- **Server Port**: 8083

#### Backend Key Features

- **Multi-Tenant SaaS**: Complete tenant isolation and management
- **Stripe Payments**: Full payment processing with webhooks
- **Advanced Analytics**: ML insights and predictive analytics
- **Automated Reporting**: Scheduled report generation
- **Notification System**: Email/SMS notifications with templates
- **PDF Generation**: Invoice and report PDF generation
- **JWT Authentication**: Secure token-based authentication
- **Vehicle Management**: Full CRUD API for vehicles
- **Reservation System**: Advanced booking and rental management
- **Dashboard APIs**: Business KPIs and metrics

### Mobile Application

**Location**: `/Users/mateoalvarez/IdeaProjects/CarRental/mobile-app/`

#### Mobile App Features

- **React Native**: Cross-platform iOS and Android support
- **Authentication**: JWT login with biometric support
- **Vehicle Browsing**: Browse and filter available vehicles
- **Reservations**: Create and manage bookings
- **Payments**: Stripe integration for mobile payments
- **Push Notifications**: Real-time booking updates
- **Profile Management**: User preferences and settings
- **Multi-tenant**: Support for different company accounts

## API Endpoints (v3)

### Authentication
- `POST /api/v1/auth/login` - User login
- `POST /api/v1/auth/register` - User registration
- `GET /api/v1/auth/me` - Get current user
- `PUT /api/v1/auth/profile` - Update user profile

### Vehicles
- `GET /api/v1/vehicles` - List all vehicles
- `POST /api/v1/vehicles` - Create vehicle
- `PUT /api/v1/vehicles/{id}` - Update vehicle
- `DELETE /api/v1/vehicles/{id}` - Delete vehicle
- `GET /api/v1/vehicles/available` - Get available vehicles

### Reservations
- `GET /api/v1/reservations` - List reservations
- `POST /api/v1/reservations` - Create reservation
- `PUT /api/v1/reservations/{id}` - Update reservation
- `PUT /api/v1/reservations/{id}/confirm` - Confirm reservation
- `PUT /api/v1/reservations/{id}/cancel` - Cancel reservation

### Payments (NEW)
- `POST /api/v1/payments/create-payment-intent` - Create Stripe payment intent
- `POST /api/v1/payments/confirm` - Confirm payment
- `POST /api/v1/payments/refund` - Process refund
- `GET /api/v1/payments/user/{userId}` - Get user payments
- `GET /api/v1/payments/revenue/daily` - Daily revenue report
- `GET /api/v1/payments/revenue/monthly` - Monthly revenue report
- `POST /api/v1/payments/webhook` - Stripe webhook handler

### Analytics (NEW)
- `GET /api/v1/analytics/customer-lifetime-value` - CLV analysis
- `GET /api/v1/analytics/demand-forecast` - Demand forecasting
- `GET /api/v1/analytics/vehicle-performance` - Vehicle analytics
- `GET /api/v1/analytics/churn-prediction` - Churn prediction
- `GET /api/v1/analytics/price-optimization` - Price optimization
- `GET /api/v1/analytics/revenue-optimization` - Revenue insights

### Reports (NEW)
- `GET /api/v1/reports/custom` - Generate custom reports
- `POST /api/v1/reports/daily/manual` - Generate daily reports
- `POST /api/v1/reports/weekly/manual` - Generate weekly reports
- `POST /api/v1/reports/monthly/manual` - Generate monthly invoices

### Tenants (NEW)
- `GET /api/v1/tenants` - List all tenants
- `POST /api/v1/tenants` - Create new tenant
- `GET /api/v1/tenants/{id}` - Get tenant details
- `PUT /api/v1/tenants/{id}` - Update tenant
- `PUT /api/v1/tenants/{id}/activate` - Activate tenant
- `PUT /api/v1/tenants/{id}/suspend` - Suspend tenant
- `GET /api/v1/tenants/active` - Get active tenants

### Dashboard (NEW)
- `GET /api/v1/dashboard/kpis` - Business KPIs and metrics

## Database Configuration

### H2 In-Memory Database (Development)
```bash
# Start backend with H2
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Access H2 Console
# URL: http://localhost:8083/h2-console
# JDBC URL: jdbc:h2:mem:carrental_db
# Username: sa
# Password: (empty)
```

### PostgreSQL Database (Production)
```bash
# Start PostgreSQL with Docker Compose
docker-compose up -d

# Stop database
docker-compose down
```

**Database Configuration**:
- Database: `carrental_db`
- Username: `carrental`
- Password: `123456`
- Port: 5432

## Development Workflow

### Starting All Services

```bash
# Terminal 1: Start Backend (H2 database)
cd /Users/mateoalvarez/IdeaProjects/CarRental
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Terminal 2: Start Frontend
cd /Users/mateoalvarez/IdeaProjects/carrental-frontend
npm run dev

# Terminal 3: Start Mobile App (Optional)
cd /Users/mateoalvarez/IdeaProjects/CarRental/mobile-app
npm start
```

### Service URLs
- **Frontend**: http://localhost:5174 (or 5173 if available)
- **Backend API**: http://localhost:8083/api/v1
- **H2 Console**: http://localhost:8083/h2-console
- **Mobile App**: Metro bundler on port 8081

### Test Credentials
- **Admin User**: `admin` / `admin123`
- **Demo User**: `demo` / `demo123`

## Key Technologies

### Backend
- **Spring Boot**: 3.5.5
- **Java**: 17+
- **Database**: PostgreSQL/H2 with JPA/Hibernate
- **Security**: Spring Security + JWT
- **Payments**: Stripe Java SDK
- **Email**: Spring Mail + Thymeleaf templates
- **SMS**: Twilio integration
- **PDF**: Flying Saucer / iText
- **Scheduling**: Spring @Scheduled
- **Build Tool**: Maven
- **Testing**: JUnit 5

### Frontend
- **React**: 18+ with TypeScript
- **Build Tool**: Vite
- **UI Framework**: Ant Design
- **HTTP Client**: Axios
- **State Management**: Zustand + React Query
- **Charts**: Recharts for analytics

### Mobile
- **React Native**: 0.72.3
- **Navigation**: React Navigation v6
- **UI**: React Native Paper
- **State**: Zustand
- **HTTP**: Axios
- **Maps**: React Native Maps
- **Payments**: Stripe React Native

### Infrastructure
- **Containerization**: Docker + Docker Compose
- **Database**: PostgreSQL 15
- **Cache**: Redis
- **Monitoring**: Prometheus + Grafana
- **Reverse Proxy**: Nginx
- **CI/CD**: GitHub Actions

## Enterprise Architecture Features

### Multi-Tenancy
- **Tenant Isolation**: Complete data separation between companies
- **Subscription Plans**: Starter, Business, Enterprise with feature flags
- **Custom Branding**: Logo, colors, and domain customization
- **Usage Limits**: Users, vehicles, and reservation limits per plan

### Payment Processing
- **Stripe Integration**: Secure payment processing
- **Subscription Billing**: Automated monthly/annual billing
- **Usage-Based Fees**: Per-reservation and per-vehicle fees
- **Refund Management**: Full and partial refund processing
- **Invoice Generation**: PDF invoices with branding

### Analytics & Business Intelligence
- **Predictive Analytics**: ML-powered demand forecasting
- **Customer Segmentation**: Automatic CLV-based segmentation
- **Churn Prediction**: Early warning system for customer retention
- **Performance Metrics**: Vehicle utilization and revenue optimization
- **Real-time Dashboards**: Live business KPIs

### Notification System
- **Multi-Channel**: Email, SMS, and push notifications
- **Template Engine**: Thymeleaf templates for emails
- **Event-Driven**: Automatic notifications for booking events
- **Preferences**: User-configurable notification settings

## Production Deployment

### Docker Compose (Production)
```bash
# Start full production stack
docker-compose -f docker-compose.production.yml up -d

# Monitor services
docker-compose logs -f

# Scale services
docker-compose up -d --scale backend=3
```

### CI/CD Pipeline
- **GitHub Actions**: Automated testing and deployment
- **Multi-stage builds**: Optimized Docker images
- **Security scanning**: Vulnerability checks
- **Database migrations**: Flyway integration

## Current Status (Documentation v3 - Oct 2025)

✅ **Enterprise SaaS Platform Fully Operational**:
- **Frontend**: http://localhost:5174/ - Complete dashboard with analytics
- **Backend**: http://localhost:8083/api/v1/ - All enterprise APIs working
- **Mobile App**: React Native foundation ready
- **Payments**: Stripe integration completed
- **Analytics**: ML insights and predictive analytics
- **Multi-tenancy**: Complete tenant management
- **Notifications**: Email/SMS system operational
- **Reports**: Automated reporting system

### Enterprise Improvements (v3)

1. **Stripe Payment System**: Complete payment processing with refunds
2. **Advanced Analytics**: ML-powered business insights and forecasting
3. **Multi-Tenant Architecture**: Full SaaS capability for multiple companies
4. **Automated Reporting**: Daily/weekly/monthly business reports
5. **Notification System**: Multi-channel notifications with templates
6. **Mobile Application**: React Native app with full feature parity
7. **Production Infrastructure**: Docker, monitoring, and CI/CD

## Development Notes

### Enterprise Considerations
- **Multi-tenancy**: All entities include tenant relationships
- **Feature Flags**: Subscription-based feature access control
- **Security**: Enhanced JWT with tenant context
- **Scalability**: Horizontal scaling support
- **Monitoring**: Comprehensive metrics and alerting
- **Compliance**: GDPR and data protection ready

### Development Best Practices
- **Type Safety**: Full TypeScript coverage
- **Error Handling**: Comprehensive error responses
- **Testing**: Unit and integration tests
- **Documentation**: OpenAPI/Swagger integration
- **Code Quality**: ESLint, Prettier, and SonarQube
- **Performance**: Database indexing and query optimization

## Troubleshooting

### Common Issues
1. **Stripe Errors**: Check API keys in application.properties
2. **Multi-tenant Issues**: Verify tenant context in requests
3. **Analytics Errors**: Ensure sufficient historical data
4. **Notification Failures**: Check SMTP/Twilio configuration
5. **Mobile Connection**: Verify API URL in mobile app config

### Performance Optimization
```bash
# Database optimization
./mvnw flyway:migrate

# Cache warming
curl http://localhost:8083/api/v1/dashboard/kpis

# Memory profiling
java -XX:+FlightRecorder -jar target/CarRental-0.0.1-SNAPSHOT.jar
```

This documentation reflects the complete enterprise SaaS platform with all advanced features implemented and operational.
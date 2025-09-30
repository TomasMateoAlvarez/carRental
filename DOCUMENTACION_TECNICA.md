# Documentación Técnica - Sistema de Alquiler de Vehículos SaaS

## 1. Modelo Entidad-Relación (MER)

### 1.1 Entidades Actuales (Stage 1)

```
┌─────────────────────────────────────────────────────────────┐
│                        VEHICLES                             │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│    │ license_plate (VARCHAR(20)) UNIQUE NOT NULL           │
│    │ brand (VARCHAR(50)) NOT NULL                          │
│    │ model (VARCHAR(50)) NOT NULL                          │
│    │ model_year (INTEGER) NOT NULL                         │
│    │ color (VARCHAR(30))                                   │
│    │ mileage (INTEGER) NOT NULL                            │
│    │ status (VARCHAR(20)) NOT NULL                         │
│    │ daily_rate (DECIMAL(10,2)) NOT NULL                   │
│    │ category (VARCHAR(50))                                │
│    │ seats (INTEGER) NOT NULL                              │
│    │ transmission (VARCHAR(20))                            │
│    │ fuel_type (VARCHAR(20))                               │
│    │ description (TEXT)                                    │
│    │ created_at (TIMESTAMP) NOT NULL                       │
│    │ updated_at (TIMESTAMP) NOT NULL                       │
│    │ last_maintenance_date (TIMESTAMP)                     │
│    │ next_maintenance_date (TIMESTAMP)                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                        CLIENTS                              │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│    │ nombre (VARCHAR(100)) NOT NULL                        │
│    │ apellido (VARCHAR(100)) NOT NULL                      │
│    │ email (VARCHAR(150)) UNIQUE NOT NULL                  │
│    │ telefono (VARCHAR(20))                                │
│    │ fecha_nacimiento (DATE)                               │
│    │ numero_licencia (VARCHAR(50)) UNIQUE                  │
│    │ direccion (TEXT)                                      │
│    │ activo (BOOLEAN) DEFAULT TRUE                         │
│    │ created_at (TIMESTAMP) NOT NULL                       │
│    │ updated_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Entidades Planificadas (Stages 2-5)

```
┌─────────────────────────────────────────────────────────────┐
│                       TENANTS                               │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│    │ name (VARCHAR(100)) NOT NULL                          │
│    │ subdomain (VARCHAR(50)) UNIQUE NOT NULL               │
│    │ database_schema (VARCHAR(50)) NOT NULL                │
│    │ subscription_plan (VARCHAR(20)) NOT NULL              │
│    │ subscription_status (VARCHAR(20)) NOT NULL            │
│    │ max_vehicles (INTEGER)                                │
│    │ max_users (INTEGER)                                   │
│    │ created_at (TIMESTAMP) NOT NULL                       │
│    │ updated_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ 1:N
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        USERS                                │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│ FK │ tenant_id (BIGINT) NOT NULL                           │
│    │ username (VARCHAR(50)) NOT NULL                       │
│    │ email (VARCHAR(150)) NOT NULL                         │
│    │ password_hash (VARCHAR(255)) NOT NULL                 │
│    │ first_name (VARCHAR(100))                             │
│    │ last_name (VARCHAR(100))                              │
│    │ is_active (BOOLEAN) DEFAULT TRUE                      │
│    │ last_login (TIMESTAMP)                                │
│    │ created_at (TIMESTAMP) NOT NULL                       │
│    │ updated_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ N:M
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        ROLES                                │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│    │ name (VARCHAR(50)) NOT NULL                           │
│    │ description (VARCHAR(255))                            │
│    │ created_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ N:M
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     PERMISSIONS                             │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│    │ name (VARCHAR(50)) NOT NULL                           │
│    │ resource (VARCHAR(50)) NOT NULL                       │
│    │ action (VARCHAR(50)) NOT NULL                         │
│    │ created_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                       RENTALS                               │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│ FK │ vehicle_id (BIGINT) NOT NULL                          │
│ FK │ client_id (BIGINT) NOT NULL                           │
│ FK │ tenant_id (BIGINT) NOT NULL                           │
│    │ rental_number (VARCHAR(50)) UNIQUE NOT NULL           │
│    │ start_date (DATE) NOT NULL                            │
│    │ end_date (DATE) NOT NULL                              │
│    │ actual_return_date (DATE)                             │
│    │ pickup_location (VARCHAR(255))                        │
│    │ return_location (VARCHAR(255))                        │
│    │ daily_rate (DECIMAL(10,2)) NOT NULL                   │
│    │ total_days (INTEGER) NOT NULL                         │
│    │ subtotal (DECIMAL(10,2)) NOT NULL                     │
│    │ taxes (DECIMAL(10,2)) NOT NULL                        │
│    │ total_amount (DECIMAL(10,2)) NOT NULL                 │
│    │ deposit_amount (DECIMAL(10,2))                        │
│    │ status (VARCHAR(20)) NOT NULL                         │
│    │ notes (TEXT)                                          │
│    │ created_at (TIMESTAMP) NOT NULL                       │
│    │ updated_at (TIMESTAMP) NOT NULL                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     AUDIT_LOGS                              │
├─────────────────────────────────────────────────────────────┤
│ PK │ id (BIGINT)                                           │
│ FK │ tenant_id (BIGINT) NOT NULL                           │
│ FK │ user_id (BIGINT)                                      │
│    │ entity_type (VARCHAR(50)) NOT NULL                    │
│    │ entity_id (BIGINT) NOT NULL                           │
│    │ action (VARCHAR(20)) NOT NULL                         │
│    │ old_values (JSON)                                     │
│    │ new_values (JSON)                                     │
│    │ timestamp (TIMESTAMP) NOT NULL                        │
│    │ ip_address (VARCHAR(45))                              │
│    │ user_agent (VARCHAR(500))                             │
└─────────────────────────────────────────────────────────────┘
```

## 2. Mapa de Base de Datos

### 2.1 Esquema Actual (PostgreSQL)

```sql
-- Esquema principal para Stage 1
CREATE SCHEMA carrental_main;

-- Tabla de vehículos
CREATE TABLE carrental_main.vehicles (
    id BIGSERIAL PRIMARY KEY,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    model_year INTEGER NOT NULL,
    color VARCHAR(30),
    mileage INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    daily_rate DECIMAL(10,2) NOT NULL,
    category VARCHAR(50),
    seats INTEGER NOT NULL,
    transmission VARCHAR(20),
    fuel_type VARCHAR(20),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_maintenance_date TIMESTAMP,
    next_maintenance_date TIMESTAMP,

    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'RENTED', 'MAINTENANCE', 'OUT_OF_SERVICE')),
    CONSTRAINT chk_vehicle_year CHECK (model_year >= 1900 AND model_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 1),
    CONSTRAINT chk_vehicle_seats CHECK (seats > 0 AND seats <= 15),
    CONSTRAINT chk_vehicle_mileage CHECK (mileage >= 0),
    CONSTRAINT chk_daily_rate CHECK (daily_rate > 0)
);

-- Tabla de clientes
CREATE TABLE carrental_main.clients (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    fecha_nacimiento DATE,
    numero_licencia VARCHAR(50) UNIQUE,
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_birth_date CHECK (fecha_nacimiento <= CURRENT_DATE - INTERVAL '18 years')
);

-- Índices para rendimiento
CREATE INDEX idx_vehicles_status ON carrental_main.vehicles(status);
CREATE INDEX idx_vehicles_brand_model ON carrental_main.vehicles(brand, model);
CREATE INDEX idx_vehicles_daily_rate ON carrental_main.vehicles(daily_rate);
CREATE INDEX idx_clients_email ON carrental_main.clients(email);
CREATE INDEX idx_clients_license ON carrental_main.clients(numero_licencia);
```

### 2.2 Esquema Multi-tenant Planificado

```sql
-- Esquema para multi-tenancy (Stage 3)
CREATE SCHEMA tenant_management;
CREATE SCHEMA tenant_a_data;
CREATE SCHEMA tenant_b_data;
-- ... más esquemas por tenant

-- Patrón de aislamiento: Schema per Tenant
-- Cada tenant tendrá su propio esquema con tablas idénticas
```

## 3. Arquitectura de Componentes

### 3.1 Capas de la Aplicación

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Controllers   │  │      DTOs       │  │ Exception   │ │
│  │                 │  │   Validation    │  │  Handlers   │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     BUSINESS LAYER                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │    Services     │  │     Mappers     │  │ Business    │ │
│  │                 │  │                 │  │   Logic     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  PERSISTENCE LAYER                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │  Repositories   │  │    Entities     │  │ Database    │ │
│  │                 │  │                 │  │ Migrations  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                 INFRASTRUCTURE LAYER                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   Security      │  │   Monitoring    │  │   Config    │ │
│  │                 │  │                 │  │             │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Componentes Detallados

#### 3.2.1 Capa de Presentación

**Controllers** (`src/main/java/com/example/carrental/controller/`)
- **ClientController.java**: Maneja operaciones CRUD de clientes
- **VehicleController.java**: Gestiona el inventario de vehículos
- **HealthController.java**: Endpoints de monitoreo de salud

**DTOs** (`src/main/java/com/example/carrental/dto/`)
- **ClientRequestDTO.java**: Validación de entrada para clientes
- **ClientResponseDTO.java**: Formato de respuesta para clientes
- **VehicleRequestDTO.java**: Validación de entrada para vehículos
- **VehicleResponseDTO.java**: Formato de respuesta para vehículos

**Exception Handling** (`src/main/java/com/example/carrental/exception/`)
- **GlobalExceptionHandler.java**: Manejo centralizado de errores
- **ErrorResponse.java**: Formato estándar de respuestas de error
- **ValidationException.java**: Excepciones de validación personalizadas

#### 3.2.2 Capa de Negocio

**Services** (`src/main/java/com/example/carrental/services/`)
- **ClientService.java**: Lógica de negocio para clientes
- **VehicleService.java**: Lógica de negocio para vehículos
- **RentalService.java**: Lógica de negocio para alquileres (planificado)

**Mappers** (`src/main/java/com/example/carrental/mapper/`)
- **ClientMapper.java**: Conversión entre DTOs y entidades
- **VehicleMapper.java**: Conversión entre DTOs y entidades

#### 3.2.3 Capa de Persistencia

**Entities** (`src/main/java/com/example/carrental/model/`)
- **VehicleModel.java**: Entidad JPA para vehículos
- **ClientModel.java**: Entidad JPA para clientes

**Repositories** (`src/main/java/com/example/carrental/repository/`)
- **VehicleRepository.java**: Acceso a datos de vehículos
- **ClientRepository.java**: Acceso a datos de clientes

#### 3.2.4 Capa de Infraestructura

**Security** (`src/main/java/com/example/carrental/config/`)
- **SecurityConfig.java**: Configuración de Spring Security
- **PasswordEncoder**: BCrypt para hash de contraseñas

**Configuration** (`src/main/resources/`)
- **application.properties**: Configuración principal
- **application-test.properties**: Configuración para pruebas

## 4. Decisiones Arquitectónicas y Justificaciones

### 4.1 Tecnologías Seleccionadas

#### Spring Boot 3.5.5
**Decisión**: Usar Spring Boot como framework principal
**Justificación**:
- Ecosistema maduro y ampliamente adoptado
- Auto-configuración reduce complejidad
- Excelente integración con herramientas de monitoreo
- Soporte nativo para microservicios y contenedores

#### PostgreSQL + H2
**Decisión**: PostgreSQL para producción, H2 para pruebas
**Justificación**:
- PostgreSQL: Robustez, ACID compliance, extensibilidad
- H2: Velocidad en pruebas, no requiere instalación externa
- Compatibilidad mediante abstracciones JPA

#### Bean Validation
**Decisión**: Usar Bean Validation en DTOs
**Justificación**:
- Separación clara entre validación y lógica de negocio
- Validación declarativa y reutilizable
- Integración automática con Spring MVC
- Mensajes de error personalizables

### 4.2 Patrones de Diseño Implementados

#### DTO Pattern
**Implementación**: DTOs separados para Request/Response
**Justificación**:
- Desacoplamiento entre API y modelo de dominio
- Validación específica por operación
- Flexibilidad para evolución de API sin afectar base de datos

#### Repository Pattern
**Implementación**: Spring Data JPA Repositories
**Justificación**:
- Abstracción de la capa de persistencia
- Consultas automáticas basadas en nombres de métodos
- Fácil testing mediante mocking

#### State Machine Pattern
**Implementación**: Enum VehicleStatus con validación de transiciones
```java
public boolean canTransitionTo(VehicleStatus newStatus) {
    return switch (this) {
        case AVAILABLE -> newStatus == RESERVED || newStatus == RENTED || newStatus == MAINTENANCE;
        case RESERVED -> newStatus == RENTED || newStatus == AVAILABLE;
        case RENTED -> newStatus == AVAILABLE || newStatus == MAINTENANCE;
        case MAINTENANCE -> newStatus == AVAILABLE || newStatus == OUT_OF_SERVICE;
        case OUT_OF_SERVICE -> newStatus == MAINTENANCE || newStatus == AVAILABLE;
    };
}
```
**Justificación**:
- Previene transiciones de estado inválidas
- Centraliza reglas de negocio
- Fácil auditoría de cambios de estado

### 4.3 Seguridad

#### BCrypt Password Encoding
**Decisión**: Usar BCryptPasswordEncoder
**Justificación**:
- Resistente a ataques de fuerza bruta
- Salt automático por contraseña
- Estándar de la industria

#### Basic Authentication (Temporal)
**Decisión**: HTTP Basic Auth para Stage 1
**Justificación**:
- Simplicidad para desarrollo inicial
- Fácil testing con herramientas como curl/Postman
- Base sólida para migración a JWT

### 4.4 Monitoreo y Observabilidad

#### Spring Boot Actuator
**Decisión**: Implementar Actuator con endpoints específicos
**Justificación**:
- Monitoreo de salud de aplicación
- Métricas automáticas de rendimiento
- Integración con Prometheus para alertas

#### Structured Logging
**Decisión**: Logging estructurado con patrones específicos
**Justificación**:
- Facilita análisis automático de logs
- Mejor debugging en producción
- Integración con sistemas de monitoreo

## 5. Máquina de Estados de Vehículos

### 5.1 Estados Definidos

```
┌─────────────┐    reserve()    ┌─────────────┐
│  AVAILABLE  │ ──────────────► │  RESERVED   │
│             │                 │             │
└─────────────┘                 └─────────────┘
       │                               │
       │ rent()                        │ rent()
       ▼                               ▼
┌─────────────┐                 ┌─────────────┐
│   RENTED    │ ◄───────────────│   RENTED    │
│             │                 │             │
└─────────────┘                 └─────────────┘
       │                               │
       │ return()                      │ cancel()
       ▼                               ▼
┌─────────────┐                 ┌─────────────┐
│  AVAILABLE  │                 │  AVAILABLE  │
│             │                 │             │
└─────────────┘                 └─────────────┘
       │
       │ maintenance()
       ▼
┌─────────────┐   fix()    ┌─────────────────┐
│ MAINTENANCE │ ─────────► │ OUT_OF_SERVICE  │
│             │            │                 │
└─────────────┘            └─────────────────┘
       ▲                           │
       │ repair()                  │ scrap()
       └───────────────────────────┘
```

### 5.2 Implementación en Código

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 20)
@Builder.Default
private VehicleStatus status = VehicleStatus.AVAILABLE;

public void changeStatus(VehicleStatus newStatus) {
    if (!canChangeStatusTo(newStatus)) {
        throw new IllegalStateException(
            String.format("Cannot change status from %s to %s for vehicle %s",
                this.status, newStatus, this.licensePlate)
        );
    }
    this.status = newStatus;
    this.updatedAt = LocalDateTime.now();
}
```

## 6. Configuración de Seguridad

### 6.1 Arquitectura de Autenticación

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {});
        return http.build();
    }
}
```

### 6.2 Justificación de Decisiones de Seguridad

#### Disable CSRF
**Decisión**: Deshabilitar CSRF para APIs REST
**Justificación**:
- APIs REST son stateless
- Autenticación por token no vulnerable a CSRF
- Facilita integración con clientes externos

#### Endpoints Públicos
**Decisión**: Permitir acceso público a health endpoints
**Justificación**:
- Load balancers necesitan verificar salud
- Monitoreo automatizado requiere acceso sin autenticación
- No expone información sensible

## 7. Roadmap de Implementación

### Stage 1: ✅ Completado
- Configuración básica de seguridad
- DTOs con validación
- Manejo global de errores
- Monitoreo con Actuator
- APIs CRUD básicas

### Stage 2: JWT Authentication + RBAC
- Implementación de JWT tokens
- Sistema de roles y permisos
- Refresh token mechanism
- Password reset functionality

### Stage 3: Multi-tenancy
- Schema per tenant pattern
- Tenant context resolver
- Data isolation mechanisms
- Subscription management

### Stage 4: Vehicle Management + Rentals
- Rental entity implementation
- Payment integration
- Notification system
- Advanced vehicle search

### Stage 5: Business Intelligence
- Analytics dashboard
- Reporting system
- Performance metrics
- Cost optimization

## 8. Consideraciones de Rendimiento

### 8.1 Índices de Base de Datos
- Índices en campos de búsqueda frecuente
- Índices compuestos para consultas complejas
- Monitoreo de query performance

### 8.2 Caching Strategy
- Redis para sesiones (Stage 2)
- Application-level caching para catálogos
- Database query result caching

### 8.3 Escalabilidad
- Stateless application design
- Database connection pooling
- Horizontal scaling preparation

Esta documentación proporciona una base sólida para el desarrollo continuo del sistema y sirve como referencia para futuras decisiones arquitectónicas.
# CarRental SaaS - Arquitectura Técnica

## 📋 Resumen Ejecutivo

**CarRental** es una plataforma SaaS empresarial para gestión de flotas de vehículos de alquiler, construida con arquitectura moderna full-stack.

### Tecnologías Principales
- **Backend**: Spring Boot 3.5.5 + Java 17 + H2/PostgreSQL + JWT + Spring Security
- **Frontend**: React 18 + TypeScript + Vite + Ant Design + Zustand + React Query
- **Base de Datos**: H2 (desarrollo) / PostgreSQL (producción)
- **Autenticación**: JWT con refresh tokens
- **Deployment**: Docker + Docker Compose

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                    CARRENTAL SAAS PLATFORM                  │
├─────────────────────────────────────────────────────────────┤
│  Frontend (React + TypeScript)     │  Backend (Spring Boot) │
│  ┌─────────────────────────────┐    │  ┌───────────────────┐ │
│  │ ● Dashboard                 │    │  │ ● REST APIs       │ │
│  │ ● Vehicle Management       │◄───┼──┤ ● JWT Security    │ │
│  │ ● User Authentication      │    │  │ ● Business Logic  │ │
│  │ ● Maintenance Tracking     │    │  │ ● Data Validation │ │
│  │ ● Notification Center      │    │  │ ● File Upload     │ │
│  │ ● Photo Management         │    │  └───────────────────┘ │
│  │ ● Responsive UI             │    │           │           │
│  └─────────────────────────────┘    │  ┌───────▼───────┐   │
│                                     │  │ H2 / PostgreSQL│   │
│  Port: 5174 (Vite Dev Server)      │  │ ● 13 Tables     │   │
│                                     │  │ ● Relationships │   │
│                                     │  │ ● Constraints   │   │
│                                     │  └─────────────────┘   │
│                                     │  Port: 8083            │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Backend - Spring Boot Architecture

### Estructura de Capas

```
src/main/java/com/example/carrental/
├── controller/          # REST Controllers (Endpoints)
├── services/           # Business Logic Layer
├── repository/         # Data Access Layer (JPA)
├── model/             # Entity Models (JPA Entities)
├── dto/               # Data Transfer Objects
├── config/            # Configuration Classes
├── security/          # Security Configuration
├── exception/         # Exception Handling
└── enums/             # Enumerations
```

### Componentes Principales

#### 1. **Controllers (REST API Layer)**
```java
@RestController
@RequestMapping("/api/v1")
public class VehicleController {
    // 15+ endpoints para gestión de vehículos
    // CRUD operations + business-specific endpoints
}
```

**Controllers Implementados:**
- `AuthController` - Autenticación JWT
- `VehicleController` - Gestión de vehículos
- `ReservationController` - Sistema de reservas
- `VehiclePhotoController` - Manejo de imágenes
- `MaintenanceController` - Gestión de mantenimiento
- `NotificationController` - Sistema de notificaciones

#### 2. **Services (Business Logic)**
```java
@Service
@Transactional
public class VehicleService {
    // Lógica de negocio
    // Validaciones empresariales
    // Orchestación de operaciones
}
```

**Services Principales:**
- `AuthService` - Lógica de autenticación
- `VehicleService` - Operaciones de vehículos
- `ReservationService` - Gestión de reservas
- `MaintenanceService` - Alertas y seguimiento
- `NotificationService` - Notificaciones en tiempo real

#### 3. **Security Layer**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // JWT authentication
        // Role-based authorization
        // CORS configuration
    }
}
```

**Características de Seguridad:**
- JWT Authentication con refresh tokens
- Role-based access control (ADMIN, EMPLOYEE, CUSTOMER)
- 26 permisos granulares
- CORS configurado para frontend
- Endpoints protegidos por roles

#### 4. **Data Layer (JPA/Hibernate)**

**Entidades Principales:**
```java
@Entity
@Table(name = "vehicles")
public class VehicleModel {
    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    // 15+ campos con validaciones
}
```

**Relaciones de Base de Datos:**
```
User (1) ──── (N) Reservation (N) ──── (1) VehicleModel
  │                                         │
  └── (1:N) Notification                   └── (1:N) VehiclePhoto
  └── (1:N) MaintenanceRecord              └── (1:N) MaintenanceRecord
```

---

## 🎨 Frontend - React Architecture

### Estructura de Proyecto

```
src/
├── components/          # Componentes reutilizables
│   ├── ui/             # Componentes base (Layout, Guards)
│   ├── vehicle/        # Componentes de vehículos
│   ├── maintenance/    # Dashboard de mantenimiento
│   └── notifications/  # Centro de notificaciones
├── pages/              # Páginas principales
├── services/           # API clients y servicios
├── stores/            # Estado global (Zustand)
├── hooks/             # Custom hooks
├── types/             # TypeScript definitions
└── App.tsx            # Aplicación principal
```

### Arquitectura de Estado

```
┌─────────────────────────────────────────────────────────┐
│                   FRONTEND STATE                        │
├─────────────────────────────────────────────────────────┤
│  Zustand Store        │  React Query Cache             │
│  ┌─────────────────┐  │  ┌──────────────────────────┐  │
│  │ ● AuthStore     │  │  │ ● Vehicles Data          │  │
│  │   - user        │  │  │ ● Reservations Data      │  │
│  │   - tokens      │  │  │ ● Maintenance Records    │  │
│  │   - permissions │  │  │ ● Notifications          │  │
│  │   - roles       │  │  │ ● Photos                 │  │
│  └─────────────────┘  │  └──────────────────────────┘  │
│         │              │              │                │
│  ┌─────▼───────┐      │      ┌──────▼────────┐       │
│  │ UI Components│      │      │ API Services  │       │
│  │ ● Dashboard  │      │      │ ● vehiclesAPI │       │
│  │ ● VehiclesPage│     │      │ ● authAPI     │       │
│  │ ● Maintenance │     │      │ ● photosAPI   │       │
│  └─────────────┘      │      └───────────────┘       │
└─────────────────────────────────────────────────────────┘
```

### Componentes Principales

#### 1. **Authentication System**
```typescript
// AuthStore con Zustand
export const useAuthStore = create<AuthStore>((set, get) => ({
  user: null,
  login: async (credentials) => { /* JWT login */ },
  logout: () => { /* Clear tokens */ },
  checkAuth: async () => { /* Validate with backend */ }
}));
```

#### 2. **Route Protection**
```typescript
// ProtectedRoute component
const ProtectedRoute = ({ children }) => {
  const { user, isLoading } = useAuthStore();

  if (!user?.isAuthenticated) {
    return <Navigate to="/login" />;
  }

  return <>{children}</>;
};
```

#### 3. **API Integration**
```typescript
// Axios interceptor for JWT
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth-token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### 4. **Role-Based UI**
```typescript
// Permission Guard Component
export const PermissionGuard = ({ permission, children }) => {
  const { hasPermission } = usePermissions();

  if (!hasPermission(permission)) {
    return <Navigate to="/unauthorized" />;
  }

  return <>{children}</>;
};
```

---

## 🗄️ Base de Datos - Schema Design

### Diagrama Entidad-Relación

```
                     ┌─────────────────┐
                     │      USERS      │
                     │ ─────────────── │
                     │ id (PK)         │
                     │ username        │
                     │ email           │
                     │ password        │
                     │ first_name      │
                     │ last_name       │
                     │ created_at      │
                     └─────────┬───────┘
                               │
                    ┌──────────┼──────────┐
                    │          │          │
          ┌─────────▼────────┐ │ ┌───────▼────────┐
          │  USER_ROLES      │ │ │ RESERVATIONS   │
          │ ──────────────── │ │ │ ────────────── │
          │ user_id (FK)     │ │ │ id (PK)        │
          │ role_id (FK)     │ │ │ user_id (FK)   │
          └─────────┬────────┘ │ │ vehicle_id (FK)│
                    │          │ │ start_date     │
          ┌─────────▼────────┐ │ │ end_date       │
          │      ROLES       │ │ │ status         │
          │ ──────────────── │ │ │ total_amount   │
          │ id (PK)          │ │ └───────┬────────┘
          │ name             │ │         │
          │ description      │ │         │
          └─────────┬────────┘ │ ┌───────▼────────┐
                    │          │ │   VEHICLES     │
          ┌─────────▼────────┐ │ │ ────────────── │
          │ ROLE_PERMISSIONS │ │ │ id (PK)        │
          │ ──────────────── │ │ │ license_plate  │
          │ role_id (FK)     │ │ │ brand          │
          │ permission_id(FK)│ │ │ model          │
          └─────────┬────────┘ │ │ year           │
                    │          │ │ status         │
          ┌─────────▼────────┐ │ │ daily_rate     │
          │   PERMISSIONS    │ │ │ mileage        │
          │ ──────────────── │ │ └───────┬────────┘
          │ id (PK)          │ │         │
          │ name             │ │    ┌────┼────┐
          │ resource         │ │    │         │
          │ action           │ │ ┌──▼───┐ ┌──▼─────────────┐
          └──────────────────┘ │ │PHOTOS│ │ MAINTENANCE    │
                               │ │──────│ │ ────────────── │
                               │ │id(PK)│ │ id (PK)        │
                               │ │v_id  │ │ vehicle_id(FK) │
                               │ │url   │ │ type           │
                               │ │type  │ │ status         │
                               │ └──────┘ │ service_date   │
                               │          │ cost           │
                               │          │ mileage        │
                               │          └────────────────┘
                               │
                     ┌─────────▼────────┐
                     │ NOTIFICATIONS    │
                     │ ──────────────── │
                     │ id (PK)          │
                     │ user_id (FK)     │
                     │ type             │
                     │ title            │
                     │ message          │
                     │ is_read          │
                     │ created_at       │
                     └──────────────────┘
```

### Tablas Principales

#### **1. USERS** - Gestión de Usuarios
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255),
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **2. VEHICLES** - Flota de Vehículos
```sql
CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    status ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    mileage INTEGER NOT NULL,
    seats INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **3. RESERVATIONS** - Sistema de Reservas
```sql
CREATE TABLE reservations (
    id BIGINT PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    vehicle_id BIGINT REFERENCES vehicles(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    reservation_code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔐 Sistema de Seguridad

### Autenticación JWT

```
┌─────────────────────────────────────────────────────────┐
│                JWT AUTHENTICATION FLOW                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Frontend          Backend               Database        │
│  ┌──────────┐    ┌──────────────┐    ┌─────────────┐   │
│  │          │    │              │    │             │   │
│  │ 1. Login │───▶│ Validate     │───▶│ Check User  │   │
│  │ Request  │    │ Credentials  │    │ Credentials │   │
│  │          │    │              │    │             │   │
│  │          │◄───│ 2. Generate  │◄───│             │   │
│  │ Token    │    │ JWT Token    │    │             │   │
│  │ Response │    │              │    │             │   │
│  │          │    │              │    │             │   │
│  │ 3. Store │    │              │    │             │   │
│  │ Token in │    │              │    │             │   │
│  │ Storage  │    │              │    │             │   │
│  │          │    │              │    │             │   │
│  │ 4. Send  │───▶│ 5. Validate  │    │             │   │
│  │ API Req  │    │ JWT Token    │    │             │   │
│  │ + Token  │    │              │    │             │   │
│  │          │    │              │    │             │   │
│  │          │◄───│ 6. Return    │    │             │   │
│  │ Response │    │ Data         │    │             │   │
│  │          │    │              │    │             │   │
│  └──────────┘    └──────────────┘    └─────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Roles y Permisos

#### **Roles del Sistema:**
1. **ADMIN** - Acceso completo al sistema
2. **EMPLOYEE** - Acceso limitado a cambios de estado
3. **CUSTOMER** - Acceso solo a sus reservas

#### **Permisos Granulares (26 total):**
```
USER_*: VIEW, CREATE, UPDATE, DELETE
VEHICLE_*: VIEW, CREATE, UPDATE, DELETE, STATUS_CHANGE
RESERVATION_*: VIEW, CREATE, UPDATE, DELETE, MANAGE
MAINTENANCE_*: VIEW, CREATE, UPDATE, RECORD_MANAGE
NOTIFICATION_*: VIEW, CREATE, MANAGE
VEHICLE_PHOTO_*: VIEW, UPLOAD, DELETE
ANALYTICS_VIEW, DASHBOARD_VIEW
```

---

## 🚀 APIs y Endpoints

### Estructura de APIs RESTful

**Base URL:** `http://localhost:8083/api/v1`

#### **Authentication APIs**
```http
POST /auth/login          # User login
POST /auth/register       # User registration
GET  /auth/me            # Get current user info
POST /auth/logout        # Logout user
POST /auth/refresh       # Refresh JWT token
```

#### **Vehicle Management APIs**
```http
GET    /vehicles              # List all vehicles
POST   /vehicles              # Create new vehicle
GET    /vehicles/{id}         # Get vehicle by ID
PUT    /vehicles/{id}         # Update vehicle
DELETE /vehicles/{id}         # Delete vehicle
PATCH  /vehicles/{id}/status  # Change vehicle status
GET    /vehicles/available    # Get available vehicles
GET    /vehicles/search       # Search vehicles
```

#### **Reservation APIs**
```http
GET    /reservations          # List user reservations
POST   /reservations          # Create reservation
GET    /reservations/{id}     # Get reservation details
PUT    /reservations/{id}     # Update reservation
DELETE /reservations/{id}     # Cancel reservation
POST   /reservations/{id}/confirm  # Confirm reservation
```

#### **Photo Management APIs**
```http
POST   /vehicle-photos/upload           # Upload vehicle photo
GET    /vehicle-photos/vehicle/{id}     # Get vehicle photos
DELETE /vehicle-photos/{id}             # Delete photo
PUT    /vehicle-photos/{id}/set-primary # Set as primary
```

#### **Maintenance APIs**
```http
GET    /maintenance/vehicle/{id}    # Get maintenance history
POST   /maintenance/create          # Create maintenance record
PUT    /maintenance/{id}            # Update maintenance
GET    /maintenance/vehicles-needing-maintenance  # Get vehicles needing service
```

#### **Notification APIs**
```http
GET    /notifications/user          # Get user notifications
GET    /notifications/user/unread   # Get unread notifications
PUT    /notifications/{id}/mark-read # Mark as read
DELETE /notifications/{id}          # Delete notification
```

---

## 📊 Performance y Optimización

### Backend Optimizations

#### **Database Optimization:**
- Índices en campos frecuentemente consultados
- Lazy loading para relaciones JPA
- Connection pooling con HikariCP
- Query optimization con JPA Criteria

#### **Caching Strategy:**
```java
@Cacheable("vehicles")
public List<Vehicle> getAllVehicles() {
    // Cache frequently accessed data
}
```

#### **API Response Optimization:**
- DTOs para limitar datos transferidos
- Paginación en endpoints de listado
- Compression habilitada
- CORS optimizado

### Frontend Optimizations

#### **React Performance:**
```typescript
// React Query for server state caching
const { data: vehicles } = useQuery({
  queryKey: ['vehicles'],
  queryFn: vehiclesAPI.getAll,
  staleTime: 5 * 60 * 1000 // 5 minutes
});

// Zustand for client state
const useAuthStore = create<AuthStore>((set, get) => ({
  // Optimized state management
}));
```

#### **Bundle Optimization:**
- Vite for fast builds and HMR
- Code splitting por rutas
- Tree shaking automático
- Asset optimization

#### **UI Performance:**
- Virtualized tables para listas grandes
- Debounced search inputs
- Optimistic updates con React Query
- Lazy loading de componentes

---

## 🔄 Data Flow Architecture

### Request/Response Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    FULL REQUEST FLOW                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│ Frontend (React)                Backend (Spring Boot)           │
│                                                                 │
│ 1. User Action         ┌─→ 5. Controller       ┌─→ 9. Repository│
│ (Click, Form Submit)   │   (@RestController)   │   (JPA/Hibernate)│
│         │              │           │           │          │      │
│         ▼              │           ▼           │          ▼      │
│ 2. API Call           ─┘   6. Service         ─┘  10. Database   │
│ (Axios + JWT Token)        (@Service)             (H2/PostgreSQL)│
│         │                          │                     │      │
│         ▼                          ▼                     │      │
│ 3. HTTP Request       ──── 7. Business Logic             │      │
│ (JSON + Headers)           (Validation, Rules)           │      │
│         │                          │                     │      │
│         ▼                          ▼                     │      │
│ 4. Spring Security    ──── 8. Data Access               ─┘      │
│ (JWT Validation)           (Repository Layer)                   │
│                                    │                            │
│                            ┌───────▼                            │
│                            │                                    │
│ 13. UI Update     ◄─── 12. JSON Response  ◄─── 11. Entity Mapping│
│ (React Re-render)      (DTO + HTTP Status)    (Entity → DTO)    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### State Management Flow

```
┌─────────────────────────────────────────────────────────────┐
│                 FRONTEND STATE FLOW                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  User Action                                                │
│      │                                                      │
│      ▼                                                      │
│  Component Event                                            │
│      │                                                      │
│  ┌───▼─────────────────────────────────────────────────┐    │
│  │               STATE MANAGEMENT                      │    │
│  │                                                     │    │
│  │  Zustand Store          React Query Cache          │    │
│  │  ┌─────────────┐        ┌─────────────────────┐     │    │
│  │  │ Auth State  │        │ Server Data Cache   │     │    │
│  │  │ - user      │        │ - vehicles          │     │    │
│  │  │ - tokens    │        │ - reservations      │     │    │
│  │  │ - permissions│       │ - maintenance       │     │    │
│  │  └─────────────┘        │ - notifications     │     │    │
│  │                         └─────────────────────┘     │    │
│  └─────────────────────────────────────────────────────┘    │
│                              │                              │
│                              ▼                              │
│                        API Services                         │
│                              │                              │
│                              ▼                              │
│                        Backend APIs                         │
│                              │                              │
│                              ▼                              │
│                        Component Re-render                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧪 Testing Strategy

### Backend Testing

#### **Unit Tests (JUnit 5)**
```java
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void shouldCreateVehicleSuccessfully() {
        // Test business logic
    }
}
```

#### **Integration Tests**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class VehicleControllerIntegrationTest {

    @Test
    void shouldReturnVehiclesList() {
        // Test full stack integration
    }
}
```

### Frontend Testing

#### **Component Tests (Jest + React Testing Library)**
```typescript
describe('VehiclesList', () => {
  test('renders vehicles correctly', () => {
    render(<VehiclesList />);
    expect(screen.getByText('Vehicle Management')).toBeInTheDocument();
  });
});
```

#### **E2E Tests (Playwright/Cypress)**
```typescript
test('user can login and view vehicles', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name="username"]', 'admin');
  await page.fill('[name="password"]', 'admin123');
  await page.click('button[type="submit"]');

  await expect(page).toHaveURL('/dashboard');
});
```

---

## 🐳 Deployment Architecture

### Docker Configuration

#### **Backend Dockerfile**
```dockerfile
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY target/CarRental-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8083
```

#### **Frontend Dockerfile**
```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "preview"]
```

#### **Docker Compose**
```yaml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DATABASE_URL=jdbc:postgresql://db:5432/carrental
    depends_on:
      - db

  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
    depends_on:
      - backend

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: carrental
      POSTGRES_USER: carrental
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
```

### Production Deployment

```
┌─────────────────────────────────────────────────────────┐
│                 PRODUCTION ARCHITECTURE                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Load Balancer (Nginx)                                 │
│           │                                             │
│           ▼                                             │
│  ┌─────────────────────────────────────────────────┐    │
│  │                 Docker Containers                │    │
│  │                                                 │    │
│  │  Frontend (React)    Backend (Spring Boot)     │    │
│  │  ┌─────────────┐     ┌─────────────────────┐    │    │
│  │  │ Nginx       │     │ Java Application    │    │    │
│  │  │ Static      │     │ + Embedded Tomcat   │    │    │
│  │  │ Assets      │     │                     │    │    │
│  │  │ Port: 80    │     │ Port: 8083          │    │    │
│  │  └─────────────┘     └─────────────────────┘    │    │
│  └─────────────────────────────────────────────────┘    │
│                                │                        │
│                                ▼                        │
│  ┌─────────────────────────────────────────────────┐    │
│  │               PostgreSQL Database               │    │
│  │  ┌─────────────────────────────────────────┐     │    │
│  │  │ Persistent Volume                       │     │    │
│  │  │ + Backup Strategy                       │     │    │
│  │  │ + Monitoring                            │     │    │
│  │  └─────────────────────────────────────────┘     │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

---

## 📈 Monitoring y Observabilidad

### Application Monitoring

#### **Spring Boot Actuator**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

#### **Logging Strategy**
```java
@Slf4j
@Service
public class VehicleService {

    public Vehicle createVehicle(VehicleRequest request) {
        log.info("Creating vehicle with license plate: {}", request.getLicensePlate());

        try {
            // Business logic
            log.info("Vehicle created successfully with ID: {}", vehicle.getId());
            return vehicle;
        } catch (Exception e) {
            log.error("Failed to create vehicle: {}", e.getMessage(), e);
            throw e;
        }
    }
}
```

### Performance Metrics

```
┌─────────────────────────────────────────────────────────┐
│                 MONITORING DASHBOARD                    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Backend Metrics              Frontend Metrics         │
│  ┌─────────────────┐          ┌───────────────────┐     │
│  │ ● Response Time │          │ ● Page Load Time  │     │
│  │ ● Throughput    │          │ ● Bundle Size     │     │
│  │ ● Error Rate    │          │ ● API Call Time   │     │
│  │ ● Memory Usage  │          │ ● User Interactions│    │
│  │ ● DB Queries    │          │ ● Error Tracking  │     │
│  └─────────────────┘          └───────────────────┘     │
│                                                         │
│  Database Metrics             Infrastructure           │
│  ┌─────────────────┐          ┌───────────────────┐     │
│  │ ● Query Time    │          │ ● CPU Usage       │     │
│  │ ● Connections   │          │ ● Memory Usage    │     │
│  │ ● Lock Waits    │          │ ● Disk I/O        │     │
│  │ ● Index Usage   │          │ ● Network Traffic │     │
│  │ ● Data Growth   │          │ ● Container Health│     │
│  └─────────────────┘          └───────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

---

## 🔮 Roadmap Técnico

### Milestone 2 - Funcionalidades Avanzadas
- [ ] **Sistema de Pagos** - Integración Stripe
- [ ] **Aplicación Móvil** - React Native
- [ ] **Analytics ML** - Predictive maintenance
- [ ] **Multi-tenancy** - SaaS multi-cliente
- [ ] **Real-time Features** - WebSockets

### Milestone 3 - Escalabilidad
- [ ] **Microservicios** - Decomposición de servicios
- [ ] **Message Queues** - RabbitMQ/Apache Kafka
- [ ] **Caching Layer** - Redis
- [ ] **CDN Integration** - CloudFront/CloudFlare
- [ ] **Auto-scaling** - Kubernetes

### Milestone 4 - Enterprise Features
- [ ] **SAML/SSO** - Enterprise authentication
- [ ] **Advanced Reporting** - BI Dashboard
- [ ] **API Gateway** - Rate limiting, analytics
- [ ] **Compliance** - GDPR, SOC2
- [ ] **Advanced Security** - Penetration testing

---

**📝 Esta documentación refleja el estado actual de MILESTONE 1 completado exitosamente.**

**🚀 CarRental SaaS - Arquitectura robusta, escalable y lista para producción.**
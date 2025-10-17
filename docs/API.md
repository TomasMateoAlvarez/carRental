# CarRental SaaS - API Documentation

## 📋 Overview

Esta documentación describe todas las APIs REST disponibles en la plataforma CarRental SaaS.

**Base URL:** `http://localhost:8083/api/v1`

**Authentication:** Bearer JWT Token

---

## 🔐 Authentication APIs

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "userId": 1,
  "username": "admin",
  "email": "admin@carrental.com",
  "firstName": "Admin",
  "lastName": "User",
  "fullName": "Admin User",
  "roles": ["ADMIN"],
  "permissions": ["USER_VIEW", "VEHICLE_CREATE", ...]
}
```

### Register
```http
POST /auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Get Current User
```http
GET /auth/me
Authorization: Bearer {token}
```

### Logout
```http
POST /auth/logout
Authorization: Bearer {token}
```

---

## 🚗 Vehicle APIs

### Get All Vehicles
```http
GET /vehicles
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": 1,
    "licensePlate": "ABC-123",
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2020,
    "color": "Blanco",
    "mileage": 45000,
    "status": "AVAILABLE",
    "statusDescription": "Disponible para alquiler",
    "dailyRate": 45.00,
    "category": "COMPACT",
    "seats": 5,
    "transmission": "AUTOMATIC",
    "fuelType": "GASOLINE",
    "description": "Vehículo en excelente estado",
    "createdAt": "2025-10-17T14:11:49.254086",
    "updatedAt": "2025-10-17T14:11:49.254087",
    "needsMaintenance": false,
    "availableForRental": true
  }
]
```

### Create Vehicle
```http
POST /vehicles
Authorization: Bearer {token}
Content-Type: application/json

{
  "licensePlate": "XYZ-789",
  "brand": "Honda",
  "model": "Civic",
  "year": 2023,
  "color": "Azul",
  "mileage": 0,
  "status": "AVAILABLE",
  "dailyRate": 50.00,
  "category": "COMPACT",
  "seats": 5,
  "transmission": "AUTOMATIC",
  "fuelType": "GASOLINE",
  "description": "Vehículo nuevo"
}
```

### Get Vehicle by ID
```http
GET /vehicles/{id}
Authorization: Bearer {token}
```

### Update Vehicle
```http
PUT /vehicles/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "licensePlate": "XYZ-789",
  "brand": "Honda",
  "model": "Civic",
  "year": 2023,
  "color": "Rojo",
  "mileage": 1000,
  "status": "AVAILABLE",
  "dailyRate": 55.00,
  "category": "COMPACT",
  "seats": 5,
  "transmission": "AUTOMATIC",
  "fuelType": "GASOLINE",
  "description": "Vehículo actualizado"
}
```

### Delete Vehicle
```http
DELETE /vehicles/{id}
Authorization: Bearer {token}
```

### Change Vehicle Status
```http
PATCH /vehicles/{id}/status?status=MAINTENANCE
Authorization: Bearer {token}
```

**Available Status Values:**
- `AVAILABLE` - Disponible para alquiler
- `RENTED` - Actualmente alquilado
- `MAINTENANCE` - En mantenimiento
- `RESERVED` - Reservado
- `OUT_OF_SERVICE` - Fuera de servicio
- `IN_REPAIR` - En reparación
- `WASHING` - En lavado

### Get Available Vehicles
```http
GET /vehicles/available
Authorization: Bearer {token}
```

### Search Vehicles
```http
GET /vehicles/search?q=toyota
Authorization: Bearer {token}
```

---

## 📋 Reservation APIs

### Get All Reservations
```http
GET /reservations/all
Authorization: Bearer {token}
```

### Get My Reservations
```http
GET /reservations/my
Authorization: Bearer {token}
```

### Create Reservation
```http
POST /reservations
Authorization: Bearer {token}
Content-Type: application/json

{
  "vehicleId": 1,
  "startDate": "2025-11-01",
  "endDate": "2025-11-05",
  "pickupLocation": "Aeropuerto Internacional",
  "returnLocation": "Aeropuerto Internacional",
  "specialRequests": "GPS incluido"
}
```

**Response:**
```json
{
  "id": 1,
  "reservationCode": "RES-20251017-001",
  "userId": 1,
  "vehicleId": 1,
  "vehicle": {
    "id": 1,
    "licensePlate": "ABC-123",
    "brand": "Toyota",
    "model": "Corolla"
  },
  "startDate": "2025-11-01",
  "endDate": "2025-11-05",
  "totalDays": 4,
  "dailyRate": 45.00,
  "totalAmount": 180.00,
  "status": "PENDING",
  "pickupLocation": "Aeropuerto Internacional",
  "returnLocation": "Aeropuerto Internacional",
  "specialRequests": "GPS incluido",
  "createdAt": "2025-10-17T15:30:00",
  "confirmedAt": null
}
```

### Get Reservation by ID
```http
GET /reservations/{id}
Authorization: Bearer {token}
```

### Update Reservation Status
```http
PUT /reservations/{id}/status?status=CONFIRMED
Authorization: Bearer {token}
```

### Confirm Reservation
```http
POST /reservations/{reservationCode}/confirm
Authorization: Bearer {token}
```

### Cancel Reservation
```http
POST /reservations/{reservationCode}/cancel
Authorization: Bearer {token}
Content-Type: application/json

{
  "reason": "Cambio de planes"
}
```

---

## 📷 Vehicle Photo APIs

### Upload Vehicle Photo
```http
POST /vehicle-photos/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

vehicleId: 1
file: [image file]
photoType: EXTERIOR
description: "Vista frontal del vehículo"
inspectionType: GENERAL
```

**Response:**
```json
{
  "id": 1,
  "vehicleId": 1,
  "photoUrl": "/uploads/vehicles/1/photo-001.jpg",
  "photoType": "EXTERIOR",
  "description": "Vista frontal del vehículo",
  "inspectionType": "GENERAL",
  "isPrimary": false,
  "takenAt": "2025-10-17T15:30:00",
  "createdAt": "2025-10-17T15:30:00"
}
```

### Get Vehicle Photos
```http
GET /vehicle-photos/vehicle/{vehicleId}
Authorization: Bearer {token}
```

### Get Photos by Type
```http
GET /vehicle-photos/vehicle/{vehicleId}/type/{photoType}
Authorization: Bearer {token}
```

**Photo Types:**
- `GENERAL` - Foto general
- `EXTERIOR` - Vista exterior
- `INTERIOR` - Vista interior
- `ENGINE` - Motor
- `DAMAGE` - Daños

### Set Primary Photo
```http
PUT /vehicle-photos/{photoId}/set-primary?vehicleId={vehicleId}
Authorization: Bearer {token}
```

### Delete Photo
```http
DELETE /vehicle-photos/{photoId}
Authorization: Bearer {token}
```

---

## 🔧 Maintenance APIs

### Get Vehicle Maintenance History
```http
GET /maintenance/vehicle/{vehicleId}
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": 1,
    "vehicleId": 1,
    "maintenanceType": "OIL_CHANGE",
    "description": "Cambio de aceite y filtros",
    "serviceProvider": "Taller Central",
    "status": "COMPLETED",
    "serviceDate": "2025-10-15T09:00:00",
    "completionDate": "2025-10-15T11:00:00",
    "cost": 75.00,
    "mileageAtService": 45000,
    "nextServiceMileage": 50000,
    "createdAt": "2025-10-15T09:00:00"
  }
]
```

### Create Maintenance Record
```http
POST /maintenance/create
Authorization: Bearer {token}
Content-Type: application/json

{
  "vehicleId": 1,
  "maintenanceType": "OIL_CHANGE",
  "description": "Cambio de aceite programado",
  "serviceProvider": "Taller Oficial Honda",
  "reason": "Mantenimiento preventivo",
  "cost": 85.00,
  "mileageAtService": 50000
}
```

### Schedule Maintenance
```http
POST /maintenance/schedule
Authorization: Bearer {token}
Content-Type: application/json

{
  "vehicleId": 1,
  "maintenanceType": "BRAKE_INSPECTION",
  "description": "Inspección de frenos",
  "scheduledDate": "2025-11-15",
  "estimatedMileage": 52000
}
```

### Update Maintenance Record
```http
PUT /maintenance/{recordId}?status=COMPLETED&completionDate=2025-10-17T14:00:00&notes=Trabajo completado sin problemas
Authorization: Bearer {token}
```

### Get Vehicles Needing Maintenance
```http
GET /maintenance/vehicles-needing-maintenance
Authorization: Bearer {token}
```

---

## 🔔 Notification APIs

### Get User Notifications
```http
GET /notifications/user
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "type": "MAINTENANCE_ALERT",
    "title": "Mantenimiento Requerido",
    "message": "El vehículo ABC-123 requiere mantenimiento en 500 km",
    "priority": "HIGH",
    "isRead": false,
    "relatedEntityType": "VEHICLE",
    "relatedEntityId": 1,
    "actionUrl": "/vehicles/1",
    "createdAt": "2025-10-17T10:00:00",
    "expiresAt": "2025-10-24T10:00:00"
  }
]
```

### Get Unread Notifications
```http
GET /notifications/user/unread
Authorization: Bearer {token}
```

### Get Unread Count
```http
GET /notifications/user/unread/count
Authorization: Bearer {token}
```

**Response:**
```json
5
```

### Mark Notification as Read
```http
PUT /notifications/{notificationId}/mark-read
Authorization: Bearer {token}
```

### Mark All as Read
```http
PUT /notifications/user/mark-all-read
Authorization: Bearer {token}
```

### Delete Notification
```http
DELETE /notifications/{notificationId}
Authorization: Bearer {token}
```

### Create Notification (Admin only)
```http
POST /notifications/create?userId=1&type=SYSTEM&title=Título&message=Mensaje&priority=MEDIUM
Authorization: Bearer {token}
```

---

## 👥 User Management APIs

### Get All Users (Admin only)
```http
GET /users
Authorization: Bearer {token}
```

### Create User (Admin only)
```http
POST /users
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "employee1",
  "email": "employee@carrental.com",
  "password": "password123",
  "firstName": "Jane",
  "lastName": "Smith",
  "roles": ["EMPLOYEE"]
}
```

### Delete User (Admin only)
```http
DELETE /users/{userId}
Authorization: Bearer {token}
```

---

## 📊 Statistics APIs

### Vehicle Stats
```http
GET /vehicles/stats/count-by-status?status=AVAILABLE
Authorization: Bearer {token}
```

### Dashboard KPIs
```http
GET /dashboard/kpis
Authorization: Bearer {token}
```

**Response:**
```json
{
  "totalVehicles": 11,
  "availableVehicles": 8,
  "rentedVehicles": 2,
  "maintenanceVehicles": 1,
  "totalReservations": 15,
  "pendingReservations": 3,
  "confirmedReservations": 10,
  "totalRevenue": 5250.00,
  "monthlyRevenue": 1200.00
}
```

---

## ⚠️ Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-10-17T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/vehicles",
  "details": {
    "licensePlate": "License plate is required",
    "dailyRate": "Daily rate must be greater than 0"
  }
}
```

### Common HTTP Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

---

## 🔒 Authorization Matrix

| Endpoint | ADMIN | EMPLOYEE | CUSTOMER |
|----------|-------|----------|----------|
| GET /vehicles | ✅ | ✅ | ✅ |
| POST /vehicles | ✅ | ❌ | ❌ |
| PUT /vehicles/{id} | ✅ | ❌ | ❌ |
| DELETE /vehicles/{id} | ✅ | ❌ | ❌ |
| PATCH /vehicles/{id}/status | ✅ | ✅ | ❌ |
| GET /reservations/all | ✅ | ✅ | ❌ |
| GET /reservations/my | ✅ | ✅ | ✅ |
| POST /reservations | ✅ | ✅ | ✅ |
| GET /maintenance/* | ✅ | ✅ | ❌ |
| POST /maintenance/* | ✅ | ✅ | ❌ |
| GET /notifications/user | ✅ | ✅ | ✅ |
| POST /notifications/create | ✅ | ❌ | ❌ |

---

## 📝 Request/Response Examples

### Complete Vehicle Creation Flow

1. **Login to get token:**
```bash
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

2. **Create vehicle with token:**
```bash
curl -X POST http://localhost:8083/api/v1/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "licensePlate": "NEW-001",
    "brand": "BMW",
    "model": "320i",
    "year": 2024,
    "color": "Negro",
    "mileage": 0,
    "status": "AVAILABLE",
    "dailyRate": 80.00,
    "category": "LUXURY",
    "seats": 5,
    "transmission": "AUTOMATIC",
    "fuelType": "GASOLINE",
    "description": "BMW nuevo modelo 2024"
  }'
```

3. **Upload photo for the vehicle:**
```bash
curl -X POST http://localhost:8083/api/v1/vehicle-photos/upload \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "vehicleId=12" \
  -F "file=@/path/to/photo.jpg" \
  -F "photoType=EXTERIOR" \
  -F "description=Vista frontal"
```

---

**📄 Esta documentación cubre todas las APIs disponibles en CarRental SaaS v1.0**

**🔗 Para más detalles técnicos, ver [ARCHITECTURE.md](./ARCHITECTURE.md)**
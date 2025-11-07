# Configuración de Supabase (Base de Datos Gratuita)

## 🎯 Paso 1: Crear Cuenta en Supabase

1. **Ir a Supabase**: https://supabase.com
2. **Sign Up** con GitHub (recomendado) o email
3. **Crear New Project**:
   - Name: `carrental-saas`
   - Database Password: `[genera uno seguro y guárdalo]`
   - Region: `West US (Oregon)` o el más cercano a ti
   - Plan: `Free` (ya seleccionado)

## 🔗 Obtener Credenciales de Conexión

Una vez creado el proyecto, ve a:
**Settings** → **Database** → **Connection info**

Necesitarás:
```
Host: db.xxx.supabase.co
Database: postgres
Username: postgres
Password: [el que pusiste al crear]
Port: 5432
```

## 🗄️ Configurar Esquema de Base de Datos

1. **Ir al SQL Editor**: https://supabase.com/dashboard/project/[tu-proyecto-id]/sql/new

2. **Ejecutar este SQL** para crear las tablas de CarRental:

```sql
-- Crear tabla de usuarios
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de vehículos
CREATE TABLE vehicles (
    id SERIAL PRIMARY KEY,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    daily_rate DECIMAL(10,2) NOT NULL,
    mileage INTEGER DEFAULT 0,
    last_maintenance_mileage INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de reservas
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    reservation_code VARCHAR(10) UNIQUE NOT NULL,
    user_id INTEGER REFERENCES users(id),
    vehicle_id INTEGER REFERENCES vehicles(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    return_location VARCHAR(255) NOT NULL,
    total_cost DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de mantenimiento
CREATE TABLE maintenance_records (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(id),
    maintenance_type VARCHAR(100) NOT NULL,
    description TEXT,
    cost DECIMAL(10,2),
    maintenance_date DATE NOT NULL,
    next_maintenance_mileage INTEGER,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar datos de prueba
INSERT INTO users (username, full_name, email, password_hash, role) VALUES
('admin', 'Admin User', 'admin@carrental.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
('demo', 'Demo User', 'demo@carrental.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CUSTOMER'),
('employee', 'Employee User', 'employee@carrental.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'EMPLOYEE');

INSERT INTO vehicles (license_plate, brand, model, year, color, daily_rate, mileage) VALUES
('ABC123', 'Toyota', 'Corolla', 2022, 'White', 45.00, 25000),
('DEF456', 'Honda', 'Civic', 2023, 'Blue', 50.00, 15000),
('GHI789', 'Ford', 'Focus', 2021, 'Red', 40.00, 35000),
('JKL012', 'Nissan', 'Sentra', 2023, 'Black', 48.00, 12000),
('MNO345', 'Chevrolet', 'Cruze', 2022, 'Gray', 47.00, 28000);
```

3. **Ejecutar** (botón "Run" o Ctrl+Enter)

## ✅ Verificar Instalación

En el SQL Editor, ejecuta:
```sql
SELECT * FROM users;
SELECT * FROM vehicles;
```

Deberías ver los datos de prueba.

## 🔐 Configurar Seguridad (Opcional para Demo)

Por defecto, Supabase tiene RLS (Row Level Security) activado. Para demos simples:

```sql
-- Deshabilitar RLS temporalmente para demos
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE vehicles DISABLE ROW LEVEL SECURITY;
ALTER TABLE reservations DISABLE ROW LEVEL SECURITY;
ALTER TABLE maintenance_records DISABLE ROW LEVEL SECURITY;
```

## 📝 Guardar Credenciales

Guarda esta información para el siguiente paso:

```
SUPABASE_URL=https://[tu-proyecto-id].supabase.co
SUPABASE_DB_HOST=db.[tu-proyecto-id].supabase.co
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=[tu-password]
SUPABASE_DB_PORT=5432
```

## 🎉 ¡Listo!

Tu base de datos PostgreSQL gratuita está lista. Capacidad:
- ✅ 500MB de almacenamiento
- ✅ 2 conexiones concurrentes
- ✅ Backup automático
- ✅ Dashboard web para administrar datos

**Siguiente paso**: Configurar el backend en Render.
# 🚀 CarRental SaaS - Deployment 100% GRATUITO

## 🎯 Stack Tecnológico GRATIS

- **Frontend**: Vercel (React + Vite) - 100GB bandwidth/mes
- **Backend**: Render (Spring Boot) - 750 horas/mes
- **Base de datos**: Supabase (PostgreSQL) - 500MB storage
- **SSL/TLS**: Automático en todos los servicios
- **Custom Domains**: Gratis en Vercel y Render

## ⚡ Quick Start (15 minutos)

```bash
# 1. Preparar proyecto
git add . && git commit -m "Prepare for free deployment" && git push

# 2. Configurar servicios (en paralelo)
# Tab 1: https://supabase.com
# Tab 2: https://render.com
# Tab 3: https://vercel.com

# 3. Deployment automático
# ✅ Push a main despliega automáticamente
```

---

## 📋 PASO 1: Base de Datos - Supabase PostgreSQL

### 🔗 Crear Proyecto

1. **Ir a Supabase**: https://supabase.com
2. **Sign Up** con GitHub
3. **New Project**:
   - Name: `carrental-saas`
   - Database Password: `[CREA UNO SEGURO]`
   - Region: `West US (Oregon)`
   - Plan: **Free** (500MB, 2 conexiones)

### 🗄️ Configurar Base de Datos

1. **SQL Editor**: https://supabase.com/dashboard/project/[tu-proyecto]/sql/new

2. **Ejecutar este SQL**:
```sql
-- Crear tablas de CarRental
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

-- Deshabilitar RLS para demos
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE vehicles DISABLE ROW LEVEL SECURITY;
ALTER TABLE reservations DISABLE ROW LEVEL SECURITY;
ALTER TABLE maintenance_records DISABLE ROW LEVEL SECURITY;
```

### 📝 Guardar Credenciales

Anotar estas credenciales:
```
SUPABASE_DB_HOST=db.[tu-proyecto-id].supabase.co
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=[tu-password]
SUPABASE_DB_PORT=5432

DATABASE_URL=postgresql://postgres:[password]@db.[project-id].supabase.co:5432/postgres
```

---

## 📋 PASO 2: Backend - Render Spring Boot

### 🚀 Crear Web Service

1. **Ir a Render**: https://render.com
2. **Sign Up** con GitHub
3. **New Web Service**:
   - **Repository**: Conectar tu repositorio CarRental
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile.render`

### ⚙️ Configurar Build

**Build Settings**:
- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `./start-render.sh`
- **Instance Type**: **Free** (512MB RAM, shared CPU)

### 🔐 Variables de Entorno

En **Environment** agregar:

```
DATABASE_URL=postgresql://postgres:[TU_PASSWORD]@db.[TU_PROJECT_ID].supabase.co:5432/postgres
JWT_SECRET=tu-jwt-secret-256-bits-muy-seguro-para-produccion
FRONTEND_URL=https://[tu-proyecto].vercel.app
SPRING_PROFILES_ACTIVE=render
```

### 🚨 Configuraciones Importantes

**Advanced Settings**:
- **Auto-Deploy**: Yes
- **Health Check Path**: `/actuator/health`
- **Region**: Oregon (mismo que Supabase)

---

## 📋 PASO 3: Frontend - Vercel React

### 🌐 Crear Proyecto Vercel

1. **Ir a Vercel**: https://vercel.com
2. **Import Project**:
   - **Repository**: Seleccionar `carrental-frontend`
   - **Framework**: `Vite`
   - **Root Directory**: `carrental-frontend`

### ⚙️ Build Settings

Vercel detecta automáticamente:
- **Framework Preset**: Vite
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Install Command**: `npm install`

### 🔐 Variables de Entorno

En **Settings** → **Environment Variables**:

```
VITE_API_BASE_URL=https://[tu-backend].onrender.com/api/v1
VITE_FRONTEND_URL=https://[tu-proyecto].vercel.app
VITE_APP_NAME=CarRental SaaS
VITE_APP_VERSION=1.0.0
```

---

## 📋 PASO 4: Conectar Todo y Testing

### 🔄 Configurar CORS en Backend

Actualizar variables en Render:
```
FRONTEND_URL=https://[tu-proyecto-vercel].vercel.app
```

### 🧪 Testing de Integración

#### **1. Backend Health Check**
```bash
curl https://[tu-backend].onrender.com/actuator/health
# ✅ Debe devolver: {"status":"UP"}
```

#### **2. Database Connection**
```bash
curl https://[tu-backend].onrender.com/api/v1/vehicles
# ✅ Debe devolver lista de vehículos
```

#### **3. Frontend Loading**
```bash
curl -I https://[tu-frontend].vercel.app
# ✅ Debe devolver: HTTP/2 200
```

#### **4. API Integration**
1. Abrir: `https://[tu-frontend].vercel.app`
2. **Login**: `admin` / `admin123`
3. **Verificar**: Dashboard carga datos del backend
4. **Testing**: Crear reserva, ver vehículos

### 🐛 Troubleshooting Común

#### **Error CORS**
```javascript
// Verificar en browser console
// Error: "Access to fetch blocked by CORS policy"
// Solución: Verificar FRONTEND_URL en variables Render
```

#### **API Not Found**
```javascript
// Error: "Network Error" o 404
// Verificar VITE_API_BASE_URL apunta al dominio correcto de Render
```

#### **Backend Sleep (Free Tier)**
```javascript
// Primera carga puede tomar 30-60 segundos
// Solución: Implementar auto-wake desde frontend
setInterval(() => {
  fetch('https://tu-backend.onrender.com/actuator/health')
}, 10 * 60 * 1000); // Ping cada 10 minutos
```

---

## 🎉 RESULTADO FINAL

### 🌍 URLs de Acceso

- **Frontend**: `https://[tu-proyecto].vercel.app`
- **API**: `https://[tu-backend].onrender.com/api/v1`
- **Database**: Supabase Dashboard

### ⚡ Capacidades del Free Tier

**Vercel Frontend**:
- ✅ 100GB bandwidth/mes
- ✅ SSL automático
- ✅ CDN global
- ✅ Auto-deploy en push

**Render Backend**:
- ✅ 750 horas/mes (30+ días)
- ✅ SSL automático
- ✅ Auto-deploy en push
- ⚠️ Sleep después 15min inactividad

**Supabase Database**:
- ✅ 500MB storage
- ✅ 2 conexiones concurrentes
- ✅ Backup automático
- ✅ Dashboard web

### 🔄 Workflow de Desarrollo

```bash
# Desarrollo local
npm run dev              # Frontend: localhost:5173
./mvnw spring-boot:run   # Backend: localhost:8083

# Deploy a producción
git add .
git commit -m "Update feature"
git push origin main

# ✅ Auto-deploy automático en Vercel y Render
```

### 💡 Optimizaciones Post-Deploy

1. **Custom Domain** (opcional):
   - Configurar en Vercel: Settings → Domains
   - Configurar en Render: Settings → Custom Domains

2. **Monitoring**:
   - Render: Métricas automáticas
   - Vercel: Analytics automático
   - Supabase: Database performance

3. **Auto-Wake Script**:
```javascript
// Agregar en frontend para evitar sleep de Render
useEffect(() => {
  const interval = setInterval(() => {
    fetch(`${import.meta.env.VITE_API_BASE_URL}/health`).catch(() => {});
  }, 10 * 60 * 1000); // 10 minutos

  return () => clearInterval(interval);
}, []);
```

---

## 🔒 Consideraciones de Seguridad

### 🛡️ Para Demos
- ✅ JWT secrets fuertes
- ✅ CORS configurado correctamente
- ✅ HTTPS automático
- ✅ Environment variables seguras

### 🚀 Para Producción Real
- [ ] Supabase Pro (más storage y conexiones)
- [ ] Render Paid (sin sleep, más recursos)
- [ ] Backup strategy
- [ ] Monitoring y alertas
- [ ] Rate limiting

---

## 🎯 ¡DEPLOYMENT COMPLETADO!

Tu CarRental SaaS está ahora funcionando 100% gratis en la nube con:

✅ **Backend escalable** en Render con Spring Boot
✅ **Frontend optimizado** en Vercel con React + TypeScript
✅ **Base de datos robusta** en Supabase PostgreSQL
✅ **SSL automático** en todos los servicios
✅ **Auto-deploy** en cada push a main
✅ **Monitoreo integrado** con dashboards

**¡Perfecto para demos y validación de producto!**

### 🤝 Credenciales Demo
- **Admin**: `admin` / `admin123`
- **Demo**: `demo` / `demo123`

### 📞 Soporte
- Vercel: https://vercel.com/docs
- Render: https://render.com/docs
- Supabase: https://supabase.com/docs

---

**🚗💨 ¡Disfruta tu CarRental SaaS en la nube!**
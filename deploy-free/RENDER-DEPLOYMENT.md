# Deployment del Backend en Render (Gratuito)

## 🎯 Paso 1: Preparar Repositorio

1. **Commit los cambios** al repositorio Git:
```bash
git add .
git commit -m "Prepare backend for Render deployment"
git push origin main
```

## 🚀 Paso 2: Crear Servicio en Render

1. **Ir a Render**: https://render.com
2. **Sign Up** con GitHub (recomendado)
3. **Nuevo Web Service**:
   - Connect Repository: [tu-repositorio-carrental]
   - Branch: main
   - Runtime: Docker
   - Dockerfile Path: `Dockerfile.render`

## ⚙️ Paso 3: Configurar Variables de Entorno

En Render Dashboard → Environment:

```
DATABASE_URL=postgresql://postgres:[password]@db.[project-id].supabase.co:5432/postgres
JWT_SECRET=tu-jwt-secret-256-bits-seguro-para-produccion
FRONTEND_URL=https://tu-frontend.vercel.app
SPRING_PROFILES_ACTIVE=render
```

## 🔧 Configuraciones Avanzadas

- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `./start-render.sh`
- **Auto-Deploy**: Yes
- **Instance Type**: Free

## 📊 Limitaciones Free Tier

- ✅ 512MB RAM
- ✅ CPU compartido
- ⚠️ Sleep después de 15min inactividad
- ✅ 750 horas/mes (suficiente para demos)
- ✅ SSL automático
- ✅ Custom domain

## 🧪 Testing

Una vez deployado, tu API estará en:
`https://tu-backend.onrender.com`

Test endpoints:
- GET `/actuator/health` - Health check
- POST `/api/v1/auth/login` - Login
- GET `/api/v1/vehicles` - Listar vehículos

## 🔄 Auto-wake desde Frontend

Para evitar el sleep, el frontend puede hacer un ping cada 10 minutos:

```javascript
// Agregar en frontend
setInterval(() => {
  fetch('https://tu-backend.onrender.com/actuator/health')
}, 10 * 60 * 1000); // 10 minutos
```

¡Listo! Tu backend estará funcionando 24/7 gratis.

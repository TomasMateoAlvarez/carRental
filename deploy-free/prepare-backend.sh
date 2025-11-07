#!/bin/bash

# Preparar Backend de CarRental para Render (Deployment Gratuito)
set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
}

warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

echo "🚀 PREPARANDO BACKEND PARA RENDER (GRATUITO)"
echo "============================================"
echo ""

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
    error "No se encontró pom.xml. Ejecuta este script desde el directorio del proyecto CarRental."
fi

# 1. Crear perfil de producción optimizado para Render
log "Creando application-render.properties..."

cat > src/main/resources/application-render.properties << 'EOF'
# CarRental - Configuración para Render (Free Tier)
spring.profiles.active=render

# Server Configuration
server.port=${PORT:8080}

# Database Configuration (Supabase PostgreSQL)
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# JPA Configuration (optimizado para free tier)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Connection Pool (optimizado para Supabase free tier - max 2 conexiones)
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
spring.datasource.hikari.connection-timeout=30000

# JWT Configuration
jwt.secret=${JWT_SECRET:defaultSecretForDemo123456789012345678901234567890}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS Configuration (permitir frontend de Vercel)
cors.allowed.origins=${FRONTEND_URL:http://localhost:5173}
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed.headers=*
cors.allow.credentials=true

# Actuator (health checks para Render)
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized
management.health.db.enabled=true

# Logging (mínimo para free tier)
logging.level.com.example.carrental=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate=ERROR
logging.pattern.console=%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Disable unnecessary features for demo
spring.jpa.open-in-view=false
spring.mvc.log-request-details=false
EOF

success "application-render.properties creado"

# 2. Modificar application.properties principal
log "Actualizando application.properties principal..."

# Backup del original
cp src/main/resources/application.properties src/main/resources/application.properties.backup

cat > src/main/resources/application.properties << 'EOF'
# CarRental SaaS - Configuración Principal
spring.application.name=CarRental

# Profiles
spring.profiles.active=${SPRING_PROFILES_ACTIVE:h2}

# Server Configuration
server.port=${PORT:8083}

# Default Database Configuration (H2 for local development)
spring.datasource.url=jdbc:h2:mem:carrental_db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (only for development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# JWT Configuration
jwt.secret=defaultSecretForDemo123456789012345678901234567890
jwt.expiration=86400000

# CORS Configuration
cors.allowed.origins=http://localhost:5173,http://localhost:5174
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS
cors.allowed.headers=*
cors.allow.credentials=true

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Logging
logging.level.com.example.carrental=INFO
EOF

success "application.properties actualizado"

# 3. Crear script de inicio para Render
log "Creando script de inicio para Render..."

cat > start-render.sh << 'EOF'
#!/bin/bash

# Script de inicio para Render
echo "🚀 Iniciando CarRental Backend en Render..."

# Verificar variables de entorno requeridas
if [ -z "$DATABASE_URL" ]; then
    echo "❌ ERROR: DATABASE_URL no está configurada"
    exit 1
fi

# Mostrar información del entorno
echo "✅ PORT: ${PORT:-8080}"
echo "✅ DATABASE_URL: [CONFIGURADA]"
echo "✅ JWT_SECRET: [CONFIGURADO]"

# Configurar perfil de Spring
export SPRING_PROFILES_ACTIVE=render

# Iniciar la aplicación
echo "🏃 Iniciando aplicación..."
java -Xmx400m -Xms200m -XX:+UseContainerSupport -Dserver.port=${PORT:-8080} -jar target/CarRental-*.jar
EOF

chmod +x start-render.sh
success "Script de inicio creado"

# 4. Actualizar Dockerfile para Render
log "Creando Dockerfile optimizado para Render..."

cat > Dockerfile.render << 'EOF'
# Dockerfile optimizado para Render Free Tier
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copiar archivos de Maven para cache de dependencias
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Hacer mvnw ejecutable
RUN chmod +x ./mvnw

# Descargar dependencias (esta capa se cachea)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN ./mvnw clean package -DskipTests -B

# Imagen final optimizada
FROM openjdk:17-jre-slim

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copiar JAR desde la etapa de build
COPY --from=build /app/target/CarRental-*.jar app.jar

# Crear usuario no-root
RUN groupadd -r carrental && useradd -r -g carrental carrental
RUN chown -R carrental:carrental /app
USER carrental

# Variables de entorno para Render
ENV SPRING_PROFILES_ACTIVE=render
ENV JAVA_OPTS="-Xmx400m -Xms200m -XX:+UseContainerSupport"

# Puerto
EXPOSE ${PORT:-8080}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Comando de inicio
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
EOF

success "Dockerfile para Render creado"

# 5. Compilar y verificar que todo funciona
log "Compilando proyecto para verificar que todo está correcto..."

if ./mvnw clean package -DskipTests; then
    success "✅ Compilación exitosa"
else
    error "❌ Error en compilación. Revisa los errores arriba."
fi

# 6. Crear archivo de instrucciones para Render
cat > deploy-free/RENDER-DEPLOYMENT.md << 'EOF'
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
EOF

echo ""
echo "🎉 BACKEND PREPARADO PARA RENDER"
echo "================================"
echo ""
echo "📁 Archivos creados:"
echo "   ✅ application-render.properties"
echo "   ✅ start-render.sh"
echo "   ✅ Dockerfile.render"
echo "   ✅ RENDER-DEPLOYMENT.md"
echo ""
echo "📋 Próximos pasos:"
echo "   1. Hacer commit y push al repositorio"
echo "   2. Configurar Render según RENDER-DEPLOYMENT.md"
echo "   3. Configurar variables de entorno con datos de Supabase"
echo ""
echo "💡 El backend estará optimizado para:"
echo "   ✅ Free tier de Render (512MB RAM)"
echo "   ✅ Supabase PostgreSQL (2 conexiones max)"
echo "   ✅ Auto-sleep/wake"
echo "   ✅ SSL automático"
echo ""
success "¡Backend listo para deployment gratuito!"
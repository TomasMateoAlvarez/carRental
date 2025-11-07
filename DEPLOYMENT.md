# CarRental SaaS - Production Deployment Guide

Este documento detalla cómo desplegar CarRental SaaS en producción usando Docker Compose.

## 📋 Requisitos Previos

### Recursos del Servidor
- **CPU**: Mínimo 2 cores, recomendado 4+ cores
- **RAM**: Mínimo 4GB, recomendado 8+ GB
- **Almacenamiento**: Mínimo 20GB, recomendado 50+ GB SSD
- **Red**: Conexión estable a internet

### Software Requerido
- Docker 24.0+
- Docker Compose 2.0+
- Git
- OpenSSL (para certificados SSL)

```bash
# Verificar instalaciones
docker --version
docker-compose --version
git --version
openssl version
```

## 🚀 Deployment Rápido

### 1. Clonar el Repositorio
```bash
git clone https://github.com/your-org/CarRental.git
cd CarRental
```

### 2. Configurar Variables de Entorno
```bash
# Copiar archivo de configuración
cp .env.production .env

# Editar configuración (IMPORTANTE: Cambiar todas las contraseñas)
nano .env
```

### 3. Ejecutar Deployment
```bash
# Hacer el script ejecutable
chmod +x deploy-production.sh

# Ejecutar deployment
./deploy-production.sh
```

## ⚙️ Configuración Detallada

### Variables de Entorno Críticas

#### Base de Datos
```bash
DB_PASSWORD=SecurePostgresPassword2024!
DB_HOST=postgres
DB_PORT=5432
DB_NAME=carrental_db
```

#### Seguridad
```bash
JWT_SECRET=very-secure-jwt-secret-key-for-production-change-this-2024-min-256-bits
ADMIN_PASSWORD=SecureAdminPassword2024!
```

#### Servicios Externos
```bash
# Email (SMTP)
SMTP_HOST=smtp.gmail.com
SMTP_USERNAME=your-email@company.com
SMTP_PASSWORD=your-app-password

# Twilio (SMS)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token

# Stripe (Pagos)
STRIPE_SECRET_KEY=sk_live_your-stripe-secret-key
STRIPE_PUBLIC_KEY=pk_live_your-stripe-public-key
```

### Certificados SSL

#### Opción 1: Certificados Existentes
```bash
# Copiar certificados a nginx/ssl/
cp your-cert.pem nginx/ssl/cert.pem
cp your-key.pem nginx/ssl/key.pem
```

#### Opción 2: Let's Encrypt (Recomendado)
```bash
# Instalar certbot
sudo apt-get update
sudo apt-get install certbot

# Generar certificados
sudo certbot certonly --standalone -d yourdomain.com

# Copiar certificados
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem nginx/ssl/key.pem
```

## 🏗️ Arquitectura del Sistema

### Servicios Docker

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| nginx | 80, 443 | Reverse proxy y SSL termination |
| backend | 8083 | Spring Boot API (interno) |
| frontend | 3080 | React SPA (interno) |
| postgres | 5432 | Base de datos PostgreSQL |
| redis | 6379 | Cache y sesiones |
| prometheus | 9090 | Métricas y monitoreo |
| grafana | 3000 | Dashboards y visualización |

### Recursos Asignados

| Servicio | CPU Limit | Memory Limit | CPU Reserved | Memory Reserved |
|----------|-----------|--------------|--------------|-----------------|
| postgres | 0.5 | 512M | 0.2 | 256M |
| redis | 0.3 | 256M | 0.1 | 128M |
| backend | 1.0 | 768M | 0.5 | 384M |
| frontend | 0.2 | 128M | 0.1 | 64M |
| nginx | 0.1 | 64M | 0.05 | 32M |

## 🔧 Comandos de Gestión

### Iniciar Servicios
```bash
docker-compose -f docker-compose.production.yml up -d
```

### Parar Servicios
```bash
docker-compose -f docker-compose.production.yml down
```

### Ver Logs
```bash
# Todos los servicios
docker-compose -f docker-compose.production.yml logs -f

# Servicio específico
docker-compose -f docker-compose.production.yml logs -f backend
```

### Estado de Servicios
```bash
docker-compose -f docker-compose.production.yml ps
```

### Actualizar Aplicación
```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.production.yml build --no-cache
docker-compose -f docker-compose.production.yml up -d
```

## 📊 Monitoreo y Salud

### Health Checks

Todos los servicios incluyen health checks automáticos:

```bash
# Verificar estado de health checks
docker-compose -f docker-compose.production.yml ps

# Ver detalles de health check
docker inspect carrental-backend | grep -A5 -B5 Health
```

### URLs de Monitoreo

- **Application**: https://yourdomain.com
- **API Health**: https://yourdomain.com/api/health
- **Prometheus**: http://yourdomain.com:9090
- **Grafana**: http://yourdomain.com:3000

### Métricas Clave

#### Application Metrics
- Response time < 500ms
- Error rate < 1%
- Uptime > 99.9%

#### Infrastructure Metrics
- CPU usage < 70%
- Memory usage < 80%
- Disk usage < 85%

## 🔒 Seguridad

### Configuraciones de Seguridad

#### Nginx
- Rate limiting configurado
- Security headers habilitados
- SSL/TLS con certificados válidos
- Proxy headers seguros

#### Base de Datos
- Acceso solo desde red interna
- Contraseñas seguras
- Conexiones cifradas

#### Aplicación
- JWT tokens seguros
- CORS configurado correctamente
- Variables de entorno protegidas

### Mejores Prácticas

1. **Cambiar todas las contraseñas por defecto**
2. **Usar certificados SSL válidos**
3. **Configurar firewall del servidor**
4. **Habilitar logs de auditoría**
5. **Actualizar dependencias regularmente**

## 🗄️ Backup y Restauración

### Backup Automático

El sistema incluye scripts de backup automático:

```bash
# Backup manual de base de datos
docker exec carrental-postgres pg_dump -U carrental carrental_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup de volúmenes
docker run --rm -v carrental_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup_$(date +%Y%m%d_%H%M%S).tar.gz /data
```

### Restauración

```bash
# Restaurar base de datos
docker exec -i carrental-postgres psql -U carrental -d carrental_db < backup_file.sql

# Restaurar volúmenes
docker run --rm -v carrental_postgres_data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres_backup_file.tar.gz -C /
```

## 📈 Escalabilidad

### Escalado Horizontal

```bash
# Escalar backend
docker-compose -f docker-compose.production.yml up -d --scale backend=3

# Escalar con load balancer
docker-compose -f docker-compose.production.yml -f docker-compose.scale.yml up -d
```

### Optimizaciones de Performance

1. **Database tuning**: Configurar PostgreSQL para carga
2. **Redis clustering**: Para alta disponibilidad
3. **CDN**: Para assets estáticos
4. **Database read replicas**: Para queries de lectura

## 🚨 Troubleshooting

### Problemas Comunes

#### Servicios no inician
```bash
# Verificar logs
docker-compose -f docker-compose.production.yml logs

# Verificar recursos
docker system df
free -h
```

#### Base de datos no conecta
```bash
# Verificar PostgreSQL
docker exec carrental-postgres pg_isready -U carrental

# Verificar logs de conexión
docker logs carrental-postgres
```

#### Certificados SSL
```bash
# Verificar certificados
openssl x509 -in nginx/ssl/cert.pem -text -noout

# Renovar Let's Encrypt
sudo certbot renew
```

### Logs Importantes

```bash
# Application logs
docker logs carrental-backend

# Nginx access logs
docker exec carrental-nginx tail -f /var/log/nginx/access.log

# Database logs
docker logs carrental-postgres
```

## 📞 Soporte

Para soporte técnico:

1. **Verificar logs**: Siempre incluir logs relevantes
2. **Estado del sistema**: Ejecutar health checks
3. **Versiones**: Documentar versiones de Docker y servicios
4. **Configuración**: Verificar variables de entorno (sin credenciales)

### Comandos de Diagnóstico

```bash
# Sistema general
docker system info
docker-compose -f docker-compose.production.yml config

# Performance
docker stats

# Espacio en disco
docker system df
du -sh /var/lib/docker/
```

---

## ✅ Checklist de Deployment

- [ ] Servidor con recursos adecuados
- [ ] Docker y Docker Compose instalados
- [ ] Variables de entorno configuradas
- [ ] Certificados SSL válidos
- [ ] Firewall configurado
- [ ] Backup automatizado configurado
- [ ] Monitoreo configurado
- [ ] DNS apuntando al servidor
- [ ] Testing en ambiente de staging
- [ ] Plan de rollback definido

**¡Deployment completado exitosamente!** 🎉
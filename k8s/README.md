# CarRental Kubernetes Deployment

Este directorio contiene toda la configuración necesaria para desplegar la plataforma CarRental SaaS en Kubernetes con características de nivel empresarial.

## 📁 Estructura del Proyecto

```
k8s/
├── base/                      # Configuraciones base de Kubernetes
│   ├── namespace.yaml         # Namespaces para diferentes entornos
│   ├── configmap.yaml         # ConfigMaps para aplicaciones y servicios
│   ├── secrets.yaml           # Secrets para credenciales sensibles
│   ├── persistent-volumes.yaml # PVCs y Storage Classes
│   ├── postgres-deployment.yaml # PostgreSQL con alta disponibilidad
│   ├── redis-deployment.yaml  # Redis con persistencia
│   ├── backend-deployment.yaml # Spring Boot backend
│   ├── frontend-deployment.yaml # React frontend + Nginx proxy
│   ├── ingress.yaml           # Ingress con SSL y rate limiting
│   ├── hpa.yaml              # Horizontal Pod Autoscaler
│   └── monitoring.yaml       # Prometheus + Grafana
├── scripts/                  # Scripts de automatización
│   ├── deploy.sh            # Script principal de despliegue
│   ├── undeploy.sh          # Script de eliminación completa
│   └── manage.sh            # Script de gestión y operaciones
└── README.md               # Esta documentación
```

## 🚀 Despliegue Rápido

### Prerrequisitos

- **Kubernetes cluster** operativo (local o cloud)
- **kubectl** configurado y conectado al cluster
- **Docker** instalado (para construcción de imágenes)
- **Acceso a registro de contenedores** (GitHub Container Registry)

### Despliegue Completo

```bash
# 1. Navegar al directorio de scripts
cd k8s/scripts

# 2. Hacer ejecutables los scripts
chmod +x *.sh

# 3. Despliegue completo (staging)
./deploy.sh

# 4. Despliegue en producción
./deploy.sh -e production -n carrental-prod

# 5. Verificar estado
./manage.sh status
```

## 📋 Configuraciones Incluidas

### 🗃️ Almacenamiento y Datos

**Persistent Volumes**:
- PostgreSQL: 20GB (fast-ssd)
- Redis: 5GB (fast-ssd)
- Logs: 10GB (standard, ReadWriteMany)
- Backups: 50GB (standard)
- Uploads: 100GB (standard, ReadWriteMany)

**Storage Classes**:
- `fast-ssd`: Para bases de datos (GP3 en AWS)
- `standard`: Para logs y backups (GP2 en AWS)

### 🛢️ Base de Datos

**PostgreSQL 15**:
- Deployment con 1 replica (modo Recreate)
- Configuración optimizada para producción
- Health checks automáticos
- Backup automático incluido
- Recursos: 256Mi-1Gi memoria, 200m-1000m CPU

**Redis 7**:
- Configuración con persistencia
- Comandos peligrosos deshabilitados
- Configuración de memoria optimizada (256MB)
- Health checks con autenticación

### 🔧 Aplicaciones

**Backend (Spring Boot)**:
- 3 replicas mínimas con HPA hasta 10
- Health checks en `/actuator/health`
- Métricas Prometheus en `/actuator/prometheus`
- Configuración completa via ConfigMaps y Secrets
- Recursos: 512Mi-1Gi memoria, 200m-1000m CPU

**Frontend (React + Nginx)**:
- 2 replicas con HPA hasta 6
- Nginx optimizado con compresión
- Rate limiting configurado
- Recursos: 64Mi-256Mi memoria, 50m-200m CPU

**Nginx Proxy**:
- Reverse proxy para enrutamiento
- Rate limiting y seguridad
- Headers de seguridad configurados
- Load balancing entre replicas

### 🌐 Networking y Seguridad

**Ingress Controller**:
- SSL/TLS automático con Let's Encrypt
- Rate limiting: 100 req/min, 20 conexiones concurrentes
- CORS configurado para dominios específicos
- Headers de seguridad (CSP, XSS Protection, etc.)
- Session affinity para operaciones stateful

**Network Policies**:
- Aislamiento de tráfico por namespace
- Comunicación controlada entre servicios
- Acceso externo restringido a puertos específicos

**Domains configurados**:
- `carrental.com` → Frontend
- `www.carrental.com` → Frontend (redirect)
- `api.carrental.com` → Backend APIs

### 📊 Monitoreo y Alertas

**Prometheus**:
- Recolección de métricas de aplicaciones y cluster
- Retención de 30 días, 10GB máximo
- Reglas de alertas predefinidas
- ServiceMonitor para autodescubrimiento

**Grafana**:
- Dashboards preconfigurados
- Datasource Prometheus automático
- Almacenamiento persistente
- Login: admin/admin123

**Alertas incluidas**:
- Alto uso de CPU (>80%)
- Alto uso de memoria (>85%)
- Aplicación caída
- Alta tasa de errores (>10%)
- Conexiones de base de datos altas

### 🔄 Autoscaling

**Horizontal Pod Autoscaler**:
- Backend: 3-10 replicas (CPU 70%, memoria 80%)
- Frontend: 2-6 replicas (CPU 60%, memoria 70%)
- Nginx: 2-5 replicas (CPU 70%, memoria 80%)

**Vertical Pod Autoscaler** (opcional):
- PostgreSQL: 256Mi-4Gi memoria, 200m-2000m CPU
- Redis: 64Mi-512Mi memoria, 100m-1000m CPU

**Pod Disruption Budgets**:
- Garantiza disponibilidad mínima durante actualizaciones
- Backend: mínimo 2 pods disponibles
- Frontend/Proxy: mínimo 1 pod disponible

### 💾 Gestión de Recursos

**Resource Quotas**:
- CPU total: 4 cores (request), 8 cores (limit)
- Memoria total: 8GB (request), 16GB (limit)
- Almacenamiento: 200GB total
- Límites de objetos: 30 pods, 15 services, etc.

**Limit Ranges**:
- Contenedores: 50m-2000m CPU, 64Mi-4Gi memoria
- Pods: máximo 4000m CPU, 8Gi memoria
- PVCs: 1Gi-100Gi por volumen

## 🛠️ Scripts de Gestión

### deploy.sh - Despliegue Completo

```bash
# Uso básico
./deploy.sh

# Opciones avanzadas
./deploy.sh -e production -n carrental-prod --skip-build --timeout 600

# Dry run (ver qué se desplegará)
./deploy.sh --dry-run
```

**Características**:
- Construcción y push automático de imágenes Docker
- Verificación de prerrequisitos
- Despliegue ordenado de componentes
- Health checks automáticos
- Información de URLs finales

### undeploy.sh - Eliminación Completa

```bash
# Eliminación con preservación de datos
./undeploy.sh

# Eliminación completa (incluye datos)
./undeploy.sh --no-preserve-data --force

# Dry run
./undeploy.sh --dry-run
```

**Características**:
- Backup automático de datos antes de eliminar
- Preservación opcional de PVCs y datos
- Confirmaciones de seguridad
- Cleanup de recursos cluster-wide

### manage.sh - Operaciones Diarias

```bash
# Estado general
./manage.sh status
./manage.sh detailed-status

# Logs de componentes
./manage.sh logs backend 100
./manage.sh logs all

# Escalado manual
./manage.sh scale backend 5
./manage.sh scale frontend 3

# Reinicio de servicios
./manage.sh restart backend
./manage.sh restart all

# Port forwarding
./manage.sh port-forward grafana 3001
./manage.sh port-forward prometheus 9090

# Acceso a bases de datos
./manage.sh db-connect
./manage.sh redis-connect

# Backup manual
./manage.sh backup
```

## 🔐 Seguridad

### Configuraciones de Seguridad Implementadas

**Pod Security**:
- `runAsNonRoot: true` en todos los contenedores
- Capabilities mínimas necesarias
- `readOnlyRootFilesystem` donde es posible
- `allowPrivilegeEscalation: false`

**Network Security**:
- Network Policies restrictivas
- Ingress con rate limiting
- Headers de seguridad configurados
- TLS/SSL obligatorio en producción

**Secrets Management**:
- Secrets base64 encoded (placeholder en repo)
- Variables sensibles via Secrets
- RBAC configurado para Prometheus
- Registry secrets para imágenes privadas

**⚠️ IMPORTANTE**: Los Secrets incluidos son solo para demostración. En producción usar:
```bash
kubectl create secret generic carrental-backend-secrets \
  --from-literal=SPRING_DATASOURCE_PASSWORD="tu-password-real" \
  --from-literal=JWT_SECRET="tu-jwt-secret-real" \
  -n carrental
```

## 📈 Monitoreo y Observabilidad

### URLs de Monitoreo

Una vez desplegado, usar port-forwarding para acceder:

```bash
# Grafana (admin/admin123)
kubectl port-forward service/grafana-service 3000:3000 -n carrental
# → http://localhost:3000

# Prometheus
kubectl port-forward service/prometheus-service 9090:9090 -n carrental
# → http://localhost:9090

# Backend Metrics
kubectl port-forward service/carrental-backend-service 8081:8081 -n carrental
# → http://localhost:8081/actuator/prometheus
```

### Métricas Disponibles

**Aplicación**:
- HTTP requests, latency, error rate
- JVM metrics (memoria, GC, threads)
- Database connection pool
- Business metrics customizados

**Infraestructura**:
- CPU, memoria, network, disk
- Pod restarts, scaling events
- Ingress traffic y errors

## 🔄 Operaciones Comunes

### Actualización de Aplicaciones

```bash
# 1. Construir nuevas imágenes
docker build -t ghcr.io/your-org/carrental-backend:v2.1.0 .
docker push ghcr.io/your-org/carrental-backend:v2.1.0

# 2. Actualizar deployment
kubectl set image deployment/carrental-backend-deployment \
  carrental-backend=ghcr.io/your-org/carrental-backend:v2.1.0 -n carrental

# 3. Verificar rollout
kubectl rollout status deployment/carrental-backend-deployment -n carrental
```

### Backup y Restore

```bash
# Backup automático
./manage.sh backup

# Backup manual con kubectl
kubectl exec deployment/postgres-deployment -n carrental -- \
  pg_dump -U carrental carrental_db > backup-$(date +%Y%m%d).sql

# Restore
kubectl exec -i deployment/postgres-deployment -n carrental -- \
  psql -U carrental -d carrental_db < backup-20241103.sql
```

### Escalado Reactivo

```bash
# Escalado manual temporal
./manage.sh scale backend 8

# Ver estado de HPA
kubectl get hpa -n carrental -w

# Ajustar thresholds de HPA
kubectl patch hpa carrental-backend-hpa -n carrental -p \
  '{"spec":{"metrics":[{"type":"Resource","resource":{"name":"cpu","target":{"type":"Utilization","averageUtilization":60}}}]}}'
```

### Troubleshooting

```bash
# Ver events del namespace
kubectl get events -n carrental --sort-by='.lastTimestamp'

# Logs de todos los pods con problemas
kubectl logs -l app.kubernetes.io/part-of=carrental-saas -n carrental --previous

# Describir pod con problemas
kubectl describe pod <pod-name> -n carrental

# Ejecutar shell en contenedor
kubectl exec -it deployment/carrental-backend-deployment -n carrental -- /bin/bash

# Verificar conectividad entre servicios
kubectl run debug --image=nicolaka/netshoot -n carrental --rm -it -- /bin/bash
```

## 🌍 Multi-Environment

### Configuración por Entorno

**Staging**:
```bash
./deploy.sh -e staging -n carrental-staging
```

**Production**:
```bash
./deploy.sh -e production -n carrental-prod
```

### Diferencias por Entorno

| Componente | Staging | Production |
|------------|---------|------------|
| Replicas Backend | 2 | 3-10 |
| Replicas Frontend | 1 | 2-6 |
| Storage Class | standard | fast-ssd |
| TLS | Let's Encrypt Staging | Let's Encrypt Prod |
| Monitoring | Básico | Completo + Alertas |
| Backup | Manual | Automático + S3 |

## 📚 Referencias Adicionales

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Prometheus Operator](https://prometheus-operator.dev/)
- [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/)
- [Cert-Manager](https://cert-manager.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🆘 Soporte

Para problemas o preguntas:
1. Revisar logs: `./manage.sh logs <component>`
2. Verificar estado: `./manage.sh detailed-status`
3. Consultar events: `kubectl get events -n carrental`
4. Documentación en `/docs/` del proyecto principal

---

**✅ Plataforma CarRental SaaS lista para producción con Kubernetes**
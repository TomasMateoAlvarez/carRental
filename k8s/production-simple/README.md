# CarRental SaaS - Production Deployment

## 🚀 Quick Start

Este directorio contiene todo lo necesario para deployar CarRental SaaS en producción con Kubernetes.

### ⚡ Deployment Automático (Recomendado)

```bash
# Deployment completo en un solo comando
./deploy-complete.sh
```

### 📋 Deployment Manual (Paso a Paso)

```bash
# 1. Instalar prerrequisitos
./install-prerequisites.sh

# 2. Configurar secretos
./setup-secrets.sh

# 3. Construir imágenes
./build-images.sh

# 4. Deployment principal
./deploy.sh
```

## 📁 Archivos Incluidos

### 🔧 Scripts de Deployment
- `deploy-complete.sh` - **Deployment automático completo**
- `install-prerequisites.sh` - Instala cert-manager y nginx-ingress
- `setup-secrets.sh` - Configuración interactiva de secretos
- `build-images.sh` - Construcción de imágenes Docker
- `deploy.sh` - Deployment principal de la aplicación

### ⚙️ Configuración de Kubernetes
- `namespace.yaml` - Namespace de producción
- `configmap.yaml` - Configuración de la aplicación
- `postgres.yaml` - Base de datos PostgreSQL con persistencia
- `backend.yaml` - Backend Spring Boot (2-6 replicas)
- `frontend.yaml` - Frontend React + Nginx (2-4 replicas)
- `ingress.yaml` - SSL/TLS automático y load balancing
- `backup.yaml` - Sistema de backup automatizado
- `monitoring.yaml` - Prometheus + Grafana

### 🐳 Docker
- `../Dockerfile.backend` - Imagen optimizada del backend
- `../../carrental-frontend/Dockerfile` - Imagen optimizada del frontend

## 📋 Prerrequisitos

### 1. Cluster Kubernetes
- **Versión**: 1.24+
- **Nodos**: Mínimo 3 nodos
- **Recursos**: 2 CPU, 4GB RAM por nodo
- **Opciones**: GKE, EKS, AKS, o self-managed

### 2. Herramientas Locales
```bash
# kubectl (ya instalado)
which kubectl

# Docker
docker --version

# Acceso al cluster configurado
kubectl cluster-info
```

### 3. Configuración del Cluster
```bash
# Google GKE
gcloud container clusters get-credentials CLUSTER_NAME --zone ZONE --project PROJECT

# AWS EKS
aws eks update-kubeconfig --region REGION --name CLUSTER_NAME

# Azure AKS
az aks get-credentials --resource-group RG --name CLUSTER_NAME
```

## 🌐 Configuración del Dominio

### 1. Obtener IP del Load Balancer
```bash
kubectl get service ingress-nginx-controller -n ingress-nginx
```

### 2. Configurar DNS
```
A record: carrental.tudominio.com → IP_DEL_LOAD_BALANCER
A record: api.carrental.tudominio.com → IP_DEL_LOAD_BALANCER
A record: monitoring.carrental.tudominio.com → IP_DEL_LOAD_BALANCER
```

### 3. Actualizar Configuración
Editar `ingress.yaml` y reemplazar `yourdomain.com` con tu dominio real.

## 🔐 Configuración de Secretos

El script `setup-secrets.sh` configurará automáticamente:

- **Database credentials** (auto-generados o personalizados)
- **JWT secrets** (256-bit auto-generado)
- **PostgreSQL passwords** (auto-generados)
- **Email para Let's Encrypt** (para certificados SSL)

Las credenciales se guardan en `production-credentials.txt` (chmod 600).

## 🏗️ Arquitectura Deployada

```
Internet
    ↓
Load Balancer (Nginx Ingress + SSL)
    ↓
┌─────────────────────────────────────┐
│         Kubernetes Cluster         │
├─────────────┬─────────────┬─────────┤
│  Frontend   │   Backend   │PostgreSQL│
│ (2-4 pods)  │ (2-6 pods)  │(1 pod)  │
├─────────────┼─────────────┼─────────┤
│ Prometheus  │   Grafana   │ Backup  │
│ (metrics)   │(dashboards) │(daily)  │
└─────────────┴─────────────┴─────────┘
```

## 📊 Recursos Deployados

### Aplicaciones
- **Backend**: Spring Boot con auto-scaling (2-6 replicas)
- **Frontend**: React + Nginx con auto-scaling (2-4 replicas)
- **Database**: PostgreSQL 15 con 20GB storage persistente

### Monitoreo
- **Prometheus**: Recolección de métricas y alertas
- **Grafana**: Dashboards y visualización (admin/admin123)

### Backup
- **Backups diarios**: 2 AM UTC con retención de 30 días
- **Backup verification**: Health checks automáticos

### SSL/TLS
- **Certificados automáticos**: Let's Encrypt
- **Renovación automática**: cert-manager

## 🔍 Verificación Post-Deployment

### Verificar Estado
```bash
# Estado general
kubectl get all -n carrental-prod

# Certificados SSL
kubectl get certificates -n carrental-prod

# Logs
kubectl logs deployment/carrental-backend-deployment -n carrental-prod -f
```

### Testing de Endpoints
```bash
# Frontend (después de configurar DNS)
curl -I https://carrental.tudominio.com

# API
curl https://api.carrental.tudominio.com/api/v1/auth/health

# Monitoreo
curl -I https://monitoring.carrental.tudominio.com
```

## 🛠️ Operaciones Comunes

### Escalado
```bash
# Escalar backend
kubectl scale deployment carrental-backend-deployment --replicas=6 -n carrental-prod

# Escalar frontend
kubectl scale deployment carrental-frontend-deployment --replicas=4 -n carrental-prod
```

### Backup Manual
```bash
# Crear backup inmediato
kubectl create job manual-backup-$(date +%Y%m%d) --from=cronjob/carrental-backup-daily -n carrental-prod

# Ver backups
kubectl exec deployment/backup-health-monitor -n carrental-prod -- ls -la /backup/daily/
```

### Actualizaciones
```bash
# Rebuild imágenes
./build-images.sh v1.1.0

# Rolling update
kubectl set image deployment/carrental-backend-deployment carrental-backend=carrental-backend:v1.1.0 -n carrental-prod
```

## 🚨 Troubleshooting

### SSL Certificates
```bash
# Verificar estado de certificados
kubectl describe certificate carrental-tls-secret -n carrental-prod

# Forzar renovación
kubectl annotate certificate carrental-tls-secret -n carrental-prod cert-manager.io/force-renew="$(date +%s)"
```

### Database Issues
```bash
# Conectar a PostgreSQL
kubectl exec -it deployment/postgres-deployment -n carrental-prod -- psql -U carrental_user -d carrental_db

# Verificar conectividad
kubectl exec deployment/carrental-backend-deployment -n carrental-prod -- nc -zv postgres-service 5432
```

### Performance Issues
```bash
# Ver métricas de recursos
kubectl top pods -n carrental-prod
kubectl top nodes

# Ver HPA status
kubectl get hpa -n carrental-prod
```

## 📈 Monitoreo

### Acceso
- **Grafana**: https://monitoring.carrental.tudominio.com
- **Credentials**: admin/admin123 (⚠️ **CAMBIAR INMEDIATAMENTE**)

### Dashboards Incluidos
- **Application Overview**: Request rate, response time, error rate
- **Infrastructure Metrics**: CPU, memory, disk, network
- **Database Performance**: Connections, queries, locks

### Alertas Configuradas
- Application downtime (>1 minuto)
- High response time (>2 segundos)
- High error rate (>5%)
- Database connection issues
- Storage space low (<10%)

## 🔒 Seguridad

### Implementado
- ✅ SSL/TLS automático
- ✅ Network policies restrictivas
- ✅ Containers non-root
- ✅ Resource limits
- ✅ Security headers
- ✅ Read-only root filesystems

### Post-Deployment
- [ ] Cambiar password de Grafana
- [ ] Configurar backup a cloud storage
- [ ] Revisar y ajustar resource limits
- [ ] Configurar log aggregation
- [ ] Setup monitoring alerts

## 📞 Soporte

### Comandos de Diagnóstico
```bash
# Estado completo
kubectl get all,pv,pvc,certificates,ingress -n carrental-prod

# Eventos recientes
kubectl get events -n carrental-prod --sort-by='.metadata.creationTimestamp'

# Logs de todos los pods
kubectl logs -l app.kubernetes.io/part-of=carrental-saas -n carrental-prod --tail=100
```

### Archivos de Log
- Production credentials: `production-credentials.txt`
- Deployment logs: Output de los scripts de deployment

---

## 🎉 ¡Deployment Completado!

Tu plataforma CarRental SaaS está ahora ejecutándose en producción con:

✅ **Alta disponibilidad** con auto-scaling
✅ **Seguridad** con SSL/TLS automático
✅ **Monitoreo** completo con alertas
✅ **Backup** automatizado diario
✅ **Escalabilidad** horizontal y vertical

¡Disfruta tu nueva plataforma SaaS! 🚗💨
# CarRental SaaS - Production Deployment Guide

## 🎯 Overview

This guide covers the **minimal viable production deployment** for CarRental SaaS - a robust, scalable architecture that's simple to deploy and maintain.

### Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Load Balancer │────│   Kubernetes    │────│   Monitoring    │
│  (Nginx Ingress)│    │    Cluster      │    │ (Prometheus +   │
│   + SSL/TLS     │    │                 │    │   Grafana)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
┌───────▼──────┐    ┌─────────▼─────────┐    ┌──────▼──────┐
│   Frontend   │    │     Backend       │    │ PostgreSQL  │
│ (React/Nginx)│    │ (Spring Boot)     │    │ Database    │
│   2 replicas │    │   2-6 replicas    │    │ Persistent  │
└──────────────┘    └───────────────────┘    └─────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   Backup System   │
                    │  (Daily backups)  │
                    └───────────────────┘
```

## 🚀 Quick Start

### Prerequisites

1. **Kubernetes Cluster** (1.24+)
   - Minimum 3 nodes, 2 CPU, 4GB RAM each
   - Can be: Google GKE, AWS EKS, Azure AKS, or self-managed

2. **Required Tools**
   ```bash
   # Install kubectl
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"

   # Install cert-manager (for SSL)
   kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

   # Install nginx-ingress (for load balancing)
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml
   ```

3. **Domain Configuration**
   - Domain pointing to your cluster (e.g., `carrental.yourdomain.com`)
   - Subdomains: `api.carrental.yourdomain.com`, `monitoring.carrental.yourdomain.com`

### Deployment Steps

1. **Clone and Navigate**
   ```bash
   cd /Users/mateoalvarez/IdeaProjects/CarRental/k8s/production-simple
   ```

2. **Configure Secrets**
   ```bash
   # Create production secrets
   kubectl create secret generic carrental-secrets -n carrental-prod \
     --from-literal=SPRING_DATASOURCE_USERNAME=carrental_user \
     --from-literal=SPRING_DATASOURCE_PASSWORD=your-secure-password-here \
     --from-literal=JWT_SECRET=your-jwt-secret-256-bits-minimum \
     --from-literal=JWT_EXPIRATION=86400000 \
     --from-literal=POSTGRES_PASSWORD=your-postgres-root-password
   ```

3. **Update Configuration**
   - Edit `ingress.yaml`: Replace `yourdomain.com` with your actual domain
   - Edit `configmap.yaml`: Update URLs and CORS settings
   - Edit `ingress.yaml`: Replace email for Let's Encrypt

4. **Deploy**
   ```bash
   # Automated deployment
   ./deploy.sh

   # Or manual deployment
   kubectl apply -f namespace.yaml
   kubectl apply -f configmap.yaml
   kubectl apply -f postgres.yaml
   kubectl apply -f backend.yaml
   kubectl apply -f frontend.yaml
   kubectl apply -f ingress.yaml
   kubectl apply -f backup.yaml
   kubectl apply -f monitoring.yaml
   ```

5. **Verify Deployment**
   ```bash
   kubectl get all -n carrental-prod
   kubectl get ingress -n carrental-prod
   kubectl get certificates -n carrental-prod
   ```

## 📁 File Structure

```
k8s/production-simple/
├── namespace.yaml          # Kubernetes namespace
├── configmap.yaml          # Application configuration + secrets template
├── postgres.yaml           # PostgreSQL database with persistence
├── backend.yaml            # Spring Boot backend (2-6 replicas)
├── frontend.yaml           # React frontend with Nginx (2-4 replicas)
├── ingress.yaml            # SSL/TLS + load balancing
├── backup.yaml             # Automated database backups
├── monitoring.yaml         # Prometheus + Grafana monitoring
└── deploy.sh              # Automated deployment script
```

## 🔧 Configuration Details

### Database Configuration
- **PostgreSQL 15** with persistent storage (20GB)
- **Connection pooling**: 200 max connections
- **Daily backups** with 30-day retention
- **Performance tuning** for production workloads

### Backend Configuration
- **2-6 replicas** with horizontal pod autoscaling
- **Resource limits**: 512Mi-1Gi memory, 250m-1000m CPU
- **Health checks**: Startup, liveness, and readiness probes
- **JVM optimization**: G1GC, container-aware settings

### Frontend Configuration
- **2-4 replicas** with auto-scaling
- **Nginx optimization**: Gzip, caching, security headers
- **Rate limiting**: 100 req/min per IP
- **Static asset caching**: 1 year for assets, 5 min for HTML

### SSL/TLS Configuration
- **Automatic SSL** with Let's Encrypt
- **Security headers**: XSS protection, content type options
- **HTTPS redirect** and CORS configuration

### Monitoring Configuration
- **Prometheus**: Metrics collection and alerting
- **Grafana**: Dashboards and visualization
- **Basic alerts**: Application down, high response time, errors
- **Resource monitoring**: CPU, memory, disk usage

### Backup Configuration
- **Daily backups** at 2 AM UTC
- **Compression** and local storage
- **30-day retention** policy
- **Backup verification** and health monitoring

## 🔐 Security Features

### Network Security
- **Network policies** for pod-to-pod communication
- **Default deny** ingress/egress rules
- **Internal service isolation**

### Pod Security
- **Non-root containers** with security contexts
- **Read-only root filesystems** where possible
- **Dropped capabilities** (ALL) with minimal additions
- **Resource limits** to prevent resource exhaustion

### Data Security
- **Kubernetes secrets** for sensitive data
- **TLS encryption** for all external traffic
- **Database connection encryption** internally

## 📊 Monitoring & Alerting

### Available Dashboards
- **Application Overview**: Request rate, response time, error rate
- **Infrastructure**: CPU, memory, disk, network usage
- **Database**: Connections, query performance, locks

### Default Alerts
- Application downtime (>1 minute)
- High response time (>2 seconds)
- High error rate (>5%)
- Database connection issues
- Pod crash loops
- Storage space low (<10%)

### Access Information
- **Grafana**: `https://monitoring.yourdomain.com`
- **Default credentials**: admin/admin123 (⚠️ CHANGE THIS!)

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow
Located at: `.github/workflows/production-deploy.yml`

**Triggers:**
- Push to `main` branch
- Git tags matching `v*`
- Manual workflow dispatch (for rollbacks)

**Pipeline Steps:**
1. **Build & Test**: Backend (Maven) + Frontend (npm)
2. **Docker Build**: Multi-stage optimized images
3. **Deploy**: Automated Kubernetes deployment
4. **Verify**: Health checks and rollback on failure

### Image Registry
- **Container Registry**: GitHub Container Registry (ghcr.io)
- **Image Tagging**: Branch name + commit SHA
- **Security**: Automated vulnerability scanning

## 🚨 Operations Guide

### Common Commands

```bash
# Check deployment status
kubectl get all -n carrental-prod

# View application logs
kubectl logs deployment/carrental-backend-deployment -n carrental-prod -f

# Scale application
kubectl scale deployment carrental-backend-deployment --replicas=4 -n carrental-prod

# Check SSL certificate status
kubectl get certificates -n carrental-prod

# Access database
kubectl exec -it deployment/postgres-deployment -n carrental-prod -- psql -U carrental_user -d carrental_db

# View recent backups
kubectl exec -it deployment/backup-health-monitor -n carrental-prod -- ls -la /backup/daily/
```

### Troubleshooting

#### SSL Certificate Issues
```bash
# Check certificate status
kubectl describe certificate carrental-tls-secret -n carrental-prod

# Check cert-manager logs
kubectl logs -n cert-manager deployment/cert-manager

# Force certificate renewal
kubectl annotate certificate carrental-tls-secret -n carrental-prod cert-manager.io/force-renew="$(date +%s)"
```

#### Application Issues
```bash
# Check pod status
kubectl describe pod -l app.kubernetes.io/name=carrental-backend -n carrental-prod

# Check events
kubectl get events -n carrental-prod --sort-by='.metadata.creationTimestamp'

# Restart deployment
kubectl rollout restart deployment/carrental-backend-deployment -n carrental-prod
```

#### Database Issues
```bash
# Check database connectivity
kubectl exec deployment/carrental-backend-deployment -n carrental-prod -- nc -zv postgres-service 5432

# Check database logs
kubectl logs deployment/postgres-deployment -n carrental-prod

# Database backup status
kubectl exec deployment/backup-health-monitor -n carrental-prod -- /scripts/health-check.sh
```

### Backup & Recovery

#### Manual Backup
```bash
kubectl create job manual-backup-$(date +%Y%m%d-%H%M) --from=cronjob/carrental-backup-daily -n carrental-prod
```

#### Restore from Backup
```bash
# List available backups
kubectl exec deployment/backup-health-monitor -n carrental-prod -- ls -la /backup/daily/

# Restore (example)
kubectl exec -it deployment/postgres-deployment -n carrental-prod -- /scripts/restore.sh carrental_backup_20231201_020000.sql.gz
```

## 📈 Scaling Guide

### Horizontal Scaling
The deployment includes **Horizontal Pod Autoscalers (HPA)** that automatically scale based on:

- **Backend**: 2-6 replicas (CPU 75%, Memory 80%)
- **Frontend**: 2-4 replicas (CPU 70%, Memory 80%)

### Manual Scaling
```bash
# Scale backend
kubectl scale deployment carrental-backend-deployment --replicas=8 -n carrental-prod

# Scale frontend
kubectl scale deployment carrental-frontend-deployment --replicas=6 -n carrental-prod
```

### Database Scaling
For database scaling (when needed):
1. **Vertical scaling**: Increase PostgreSQL pod resources
2. **Read replicas**: Add read-only database replicas
3. **Connection pooling**: Implement PgBouncer

### Storage Scaling
```bash
# Check storage usage
kubectl exec deployment/postgres-deployment -n carrental-prod -- df -h /var/lib/postgresql/data

# Resize PVC (if storage class supports it)
kubectl patch pvc postgres-pvc -n carrental-prod -p '{"spec":{"resources":{"requests":{"storage":"50Gi"}}}}'
```

## 🔒 Security Hardening

### Production Security Checklist

- [ ] Change default Grafana password
- [ ] Update Let's Encrypt email in ingress.yaml
- [ ] Use strong database passwords
- [ ] Configure proper JWT secrets (256-bit minimum)
- [ ] Enable pod security policies
- [ ] Review network policies
- [ ] Set up log aggregation
- [ ] Configure backup encryption
- [ ] Enable audit logging
- [ ] Regular security updates

### Additional Security Measures
```bash
# Enable pod security standards
kubectl label namespace carrental-prod pod-security.kubernetes.io/enforce=restricted

# Create network policy for stricter isolation
kubectl apply -f - <<EOF
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: default-deny-all
  namespace: carrental-prod
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
EOF
```

## 💰 Cost Optimization

### Resource Optimization
- **Right-sizing**: Monitor actual resource usage and adjust requests/limits
- **Spot instances**: Use spot/preemptible instances for non-critical workloads
- **Cluster autoscaling**: Enable cluster autoscaler to scale nodes

### Monitoring Costs
```bash
# Check resource usage
kubectl top nodes
kubectl top pods -n carrental-prod

# Resource requests vs actual usage
kubectl describe nodes | grep -A 3 "Allocated resources"
```

## 📞 Support

### Getting Help
1. **Check logs**: Application and infrastructure logs
2. **Monitor metrics**: Grafana dashboards and alerts
3. **Review documentation**: This guide and Kubernetes docs
4. **Community support**: Kubernetes and Spring Boot communities

### Maintenance Schedule
- **Daily**: Automated backups and monitoring
- **Weekly**: Review logs and metrics
- **Monthly**: Security updates and dependency updates
- **Quarterly**: Performance review and capacity planning

---

## 🎉 Congratulations!

You now have a **production-ready CarRental SaaS platform** with:

✅ **High Availability**: Multi-replica deployments with auto-scaling
✅ **Security**: SSL/TLS, network policies, non-root containers
✅ **Monitoring**: Prometheus + Grafana with alerts
✅ **Backup**: Automated daily backups with retention
✅ **CI/CD**: GitHub Actions deployment pipeline
✅ **Scalability**: Horizontal and vertical scaling capabilities

**Next Steps:**
1. Configure your domain DNS
2. Wait for SSL certificates to be issued
3. Change default passwords
4. Monitor application health
5. Plan for growth and scaling

Your CarRental SaaS is now ready to serve customers! 🚗💨
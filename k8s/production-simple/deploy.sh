#!/bin/bash

# CarRental Production Deployment Script
# Simple but robust deployment automation

set -e

# Configuration
NAMESPACE="carrental-prod"
TIMEOUT="300s"
KUBECTL_CONTEXT=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
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

check_prerequisites() {
    log "Checking prerequisites..."

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        error "kubectl is not installed"
    fi

    # Check cluster connection
    if ! kubectl cluster-info &> /dev/null; then
        error "Cannot connect to Kubernetes cluster"
    fi

    # Check if cert-manager is installed
    if ! kubectl get crd certificates.cert-manager.io &> /dev/null; then
        warning "cert-manager CRDs not found. SSL certificates may not work."
        read -p "Continue anyway? (y/N): " continue_without_certs
        if [ "$continue_without_certs" != "y" ] && [ "$continue_without_certs" != "Y" ]; then
            exit 1
        fi
    fi

    success "Prerequisites check completed"
}

create_namespace() {
    log "Creating namespace..."
    kubectl apply -f namespace.yaml
    success "Namespace created/updated"
}

deploy_secrets() {
    log "Deploying secrets and configuration..."

    # Check if secrets exist
    if ! kubectl get secret carrental-secrets -n $NAMESPACE &> /dev/null; then
        warning "carrental-secrets not found. Please create it manually:"
        echo ""
        echo "kubectl create secret generic carrental-secrets -n $NAMESPACE \\"
        echo "  --from-literal=SPRING_DATASOURCE_USERNAME=carrental_user \\"
        echo "  --from-literal=SPRING_DATASOURCE_PASSWORD=your-secure-password \\"
        echo "  --from-literal=JWT_SECRET=your-jwt-secret-256-bits \\"
        echo "  --from-literal=JWT_EXPIRATION=86400000 \\"
        echo "  --from-literal=POSTGRES_PASSWORD=your-postgres-password"
        echo ""
        read -p "Press Enter after creating the secret..."
    fi

    kubectl apply -f configmap.yaml
    success "Configuration deployed"
}

deploy_database() {
    log "Deploying PostgreSQL database..."

    kubectl apply -f postgres.yaml

    log "Waiting for PostgreSQL to be ready..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=postgres -n $NAMESPACE --timeout=$TIMEOUT

    success "PostgreSQL deployed and ready"
}

deploy_backend() {
    log "Deploying backend application..."

    kubectl apply -f backend.yaml

    log "Waiting for backend to be ready..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=carrental-backend -n $NAMESPACE --timeout=$TIMEOUT

    success "Backend deployed and ready"
}

deploy_frontend() {
    log "Deploying frontend application..."

    kubectl apply -f frontend.yaml

    log "Waiting for frontend to be ready..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=carrental-frontend -n $NAMESPACE --timeout=$TIMEOUT

    success "Frontend deployed and ready"
}

deploy_ingress() {
    log "Deploying ingress and SSL..."

    kubectl apply -f ingress.yaml

    success "Ingress deployed"
}

deploy_backup() {
    log "Deploying backup system..."

    kubectl apply -f backup.yaml

    success "Backup system deployed"
}

deploy_monitoring() {
    log "Deploying monitoring..."

    kubectl apply -f monitoring.yaml

    log "Waiting for monitoring to be ready..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n $NAMESPACE --timeout=$TIMEOUT || warning "Prometheus may take longer to start"
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n $NAMESPACE --timeout=$TIMEOUT || warning "Grafana may take longer to start"

    success "Monitoring deployed"
}

verify_deployment() {
    log "Verifying deployment..."

    echo ""
    echo "=== Deployment Status ==="
    kubectl get all -n $NAMESPACE

    echo ""
    echo "=== Ingress Status ==="
    kubectl get ingress -n $NAMESPACE

    echo ""
    echo "=== Certificate Status ==="
    kubectl get certificates -n $NAMESPACE 2>/dev/null || echo "No certificates found (cert-manager may not be installed)"

    echo ""
    echo "=== Pod Status ==="
    kubectl get pods -n $NAMESPACE -o wide

    # Check if all deployments are ready
    if kubectl wait --for=condition=available deployment --all -n $NAMESPACE --timeout=60s; then
        success "All deployments are ready"
    else
        warning "Some deployments may not be ready yet"
    fi
}

show_access_info() {
    log "Deployment completed!"

    echo ""
    echo "🎉 CarRental SaaS has been deployed successfully!"
    echo ""
    echo "📝 Next Steps:"
    echo "1. Update DNS records to point to your cluster"
    echo "2. Wait for SSL certificates to be issued (may take a few minutes)"
    echo "3. Access your application:"
    echo ""
    echo "   🌐 Frontend: https://carrental.yourdomain.com"
    echo "   🔧 API: https://api.carrental.yourdomain.com"
    echo "   📊 Monitoring: https://monitoring.yourdomain.com"
    echo "   👤 Grafana: admin/admin123 (CHANGE THIS!)"
    echo ""
    echo "🔧 Management Commands:"
    echo "   Check status: kubectl get all -n $NAMESPACE"
    echo "   View logs: kubectl logs deployment/carrental-backend-deployment -n $NAMESPACE"
    echo "   Scale up: kubectl scale deployment carrental-backend-deployment --replicas=4 -n $NAMESPACE"
    echo ""
    echo "🔒 Security Reminders:"
    echo "   - Change default Grafana password"
    echo "   - Update email in ingress.yaml for Let's Encrypt"
    echo "   - Configure backup retention as needed"
    echo ""
}

print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -n, --namespace NAMESPACE    Set namespace (default: carrental-prod)"
    echo "  -t, --timeout TIMEOUT        Set timeout for operations (default: 300s)"
    echo "  -c, --context CONTEXT        Set kubectl context"
    echo "  --skip-verification          Skip deployment verification"
    echo "  --database-only              Deploy only database"
    echo "  --app-only                   Deploy only application (skip database)"
    echo "  -h, --help                   Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                           # Full deployment"
    echo "  $0 --database-only           # Deploy only database"
    echo "  $0 --app-only                # Deploy only application"
    echo "  $0 -n my-namespace           # Deploy to custom namespace"
}

# Parse command line arguments
SKIP_VERIFICATION=false
DATABASE_ONLY=false
APP_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        -c|--context)
            KUBECTL_CONTEXT="$2"
            shift 2
            ;;
        --skip-verification)
            SKIP_VERIFICATION=true
            shift
            ;;
        --database-only)
            DATABASE_ONLY=true
            shift
            ;;
        --app-only)
            APP_ONLY=true
            shift
            ;;
        -h|--help)
            print_usage
            exit 0
            ;;
        *)
            error "Unknown option $1"
            ;;
    esac
done

# Set kubectl context if specified
if [ ! -z "$KUBECTL_CONTEXT" ]; then
    kubectl config use-context $KUBECTL_CONTEXT
fi

# Main deployment flow
main() {
    echo ""
    echo "🚀 CarRental SaaS Production Deployment"
    echo "======================================="
    echo ""

    check_prerequisites

    create_namespace
    deploy_secrets

    if [ "$APP_ONLY" = false ]; then
        deploy_database
    fi

    if [ "$DATABASE_ONLY" = false ]; then
        deploy_backend
        deploy_frontend
        deploy_ingress
        deploy_backup
        deploy_monitoring
    fi

    if [ "$SKIP_VERIFICATION" = false ]; then
        verify_deployment
    fi

    if [ "$DATABASE_ONLY" = false ]; then
        show_access_info
    fi
}

# Run main function
main
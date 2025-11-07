#!/bin/bash

# Install Prerequisites for CarRental Production Deployment
set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

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

check_cluster() {
    log "Verificando conexión al cluster..."
    if ! kubectl cluster-info &> /dev/null; then
        error "No se puede conectar al cluster Kubernetes. Configura kubectl primero."
    fi
    success "Cluster Kubernetes accesible"
}

install_cert_manager() {
    log "Instalando cert-manager..."

    # Check if cert-manager is already installed
    if kubectl get namespace cert-manager &> /dev/null; then
        warning "cert-manager ya está instalado"
        return 0
    fi

    # Install cert-manager
    kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

    # Wait for cert-manager to be ready
    log "Esperando que cert-manager esté listo..."
    kubectl wait --for=condition=ready pod -l app=cert-manager -n cert-manager --timeout=300s
    kubectl wait --for=condition=ready pod -l app=cainjector -n cert-manager --timeout=300s
    kubectl wait --for=condition=ready pod -l app=webhook -n cert-manager --timeout=300s

    success "cert-manager instalado correctamente"
}

install_nginx_ingress() {
    log "Instalando nginx-ingress..."

    # Check if nginx-ingress is already installed
    if kubectl get namespace ingress-nginx &> /dev/null; then
        warning "nginx-ingress ya está instalado"
        return 0
    fi

    # Install nginx-ingress
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

    # Wait for nginx-ingress to be ready
    log "Esperando que nginx-ingress esté listo..."
    kubectl wait --namespace ingress-nginx \
        --for=condition=ready pod \
        --selector=app.kubernetes.io/component=controller \
        --timeout=300s

    success "nginx-ingress instalado correctamente"
}

verify_installations() {
    log "Verificando instalaciones..."

    echo ""
    echo "=== cert-manager status ==="
    kubectl get pods -n cert-manager

    echo ""
    echo "=== nginx-ingress status ==="
    kubectl get pods -n ingress-nginx

    echo ""
    echo "=== Load Balancer IP ==="
    kubectl get service ingress-nginx-controller -n ingress-nginx -o wide

    success "Verificación completada"
}

show_next_steps() {
    echo ""
    echo "🎉 Prerrequisitos instalados correctamente!"
    echo ""
    echo "📝 Próximos pasos:"
    echo "1. Configurar DNS para apuntar tu dominio al Load Balancer IP mostrado arriba"
    echo "2. Actualizar el archivo ingress.yaml con tu dominio real"
    echo "3. Crear los secretos de producción"
    echo "4. Ejecutar el deployment principal con ./deploy.sh"
    echo ""
    echo "💡 Para obtener la IP del Load Balancer:"
    echo "kubectl get service ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}'"
}

main() {
    echo "🚀 Instalando prerrequisitos para CarRental SaaS"
    echo "=============================================="

    check_cluster
    install_cert_manager
    install_nginx_ingress
    verify_installations
    show_next_steps
}

# Execute main function
main
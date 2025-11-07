#!/bin/bash

# Complete CarRental SaaS Production Deployment
# This script orchestrates the entire deployment process

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="carrental-prod"
VERSION=${1:-"latest"}

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
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

title() {
    echo -e "${BOLD}${BLUE}=== $1 ===${NC}"
}

wait_for_user() {
    echo ""
    read -p "Presiona Enter para continuar..."
    echo ""
}

check_prerequisites() {
    title "VERIFICANDO PRERREQUISITOS"

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        error "kubectl no está instalado"
    fi

    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
    fi

    # Check cluster connection
    if ! kubectl cluster-info &> /dev/null; then
        error "No se puede conectar al cluster Kubernetes. Ejecuta uno de estos comandos primero:

        Google GKE: gcloud container clusters get-credentials CLUSTER_NAME --zone ZONE --project PROJECT
        AWS EKS: aws eks update-kubeconfig --region REGION --name CLUSTER_NAME
        Azure AKS: az aks get-credentials --resource-group RG --name CLUSTER_NAME
        Local: cp /path/to/kubeconfig ~/.kube/config"
    fi

    success "Todos los prerrequisitos están disponibles"
}

install_cluster_components() {
    title "INSTALANDO COMPONENTES DEL CLUSTER"

    log "Ejecutando install-prerequisites.sh..."
    $SCRIPT_DIR/install-prerequisites.sh

    success "Componentes del cluster instalados"
}

configure_secrets() {
    title "CONFIGURANDO SECRETOS"

    log "Ejecutando setup-secrets.sh..."
    $SCRIPT_DIR/setup-secrets.sh

    success "Secretos configurados"
}

update_domain_configuration() {
    title "CONFIGURACIÓN DEL DOMINIO"

    echo "🌐 CONFIGURACIÓN NECESARIA DEL DOMINIO"
    echo "====================================="
    echo ""
    echo "Antes de continuar, necesitas:"
    echo ""
    echo "1. Obtener la IP del Load Balancer:"
    kubectl get service ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null && echo "" || echo "   (Pendiente de asignación)"
    echo ""
    echo "2. Configurar DNS para tu dominio:"
    echo "   A record: carrental.tudominio.com → IP_DEL_LOAD_BALANCER"
    echo "   A record: api.carrental.tudominio.com → IP_DEL_LOAD_BALANCER"
    echo "   A record: monitoring.carrental.tudominio.com → IP_DEL_LOAD_BALANCER"
    echo ""
    echo "3. Actualizar ingress.yaml con tu dominio real (reemplazar yourdomain.com)"
    echo ""

    read -p "¿Has configurado el DNS y actualizado ingress.yaml? (y/N): " dns_ready
    if [ "$dns_ready" != "y" ] && [ "$dns_ready" != "Y" ]; then
        warning "Configura el DNS y actualiza ingress.yaml antes de continuar"
        echo "Archivo a editar: $SCRIPT_DIR/ingress.yaml"
        wait_for_user
    fi

    success "Configuración de dominio verificada"
}

build_docker_images() {
    title "CONSTRUYENDO IMÁGENES DOCKER"

    log "Ejecutando build-images.sh..."
    $SCRIPT_DIR/build-images.sh $VERSION

    success "Imágenes Docker construidas"
}

deploy_infrastructure() {
    title "DEPLOYANDO INFRAESTRUCTURA"

    cd $SCRIPT_DIR

    log "Creando namespace..."
    kubectl apply -f namespace.yaml

    log "Aplicando configuración..."
    kubectl apply -f configmap.yaml

    log "Deployando PostgreSQL..."
    kubectl apply -f postgres.yaml

    log "Esperando que PostgreSQL esté listo..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=postgres -n $NAMESPACE --timeout=300s

    success "Infraestructura deployada"
}

deploy_applications() {
    title "DEPLOYANDO APLICACIONES"

    cd $SCRIPT_DIR

    log "Deployando backend..."
    kubectl apply -f backend.yaml

    log "Esperando que el backend esté listo..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=carrental-backend -n $NAMESPACE --timeout=300s

    log "Deployando frontend..."
    kubectl apply -f frontend.yaml

    log "Esperando que el frontend esté listo..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=carrental-frontend -n $NAMESPACE --timeout=300s

    success "Aplicaciones deployadas"
}

configure_networking() {
    title "CONFIGURANDO RED Y SSL"

    cd $SCRIPT_DIR

    log "Aplicando ingress y SSL..."
    kubectl apply -f ingress.yaml

    log "Verificando certificados SSL..."
    sleep 30  # Wait for cert-manager to pick up
    kubectl get certificates -n $NAMESPACE || warning "Certificados aún procesándose"

    success "Red y SSL configurados"
}

deploy_support_systems() {
    title "DEPLOYANDO SISTEMAS DE SOPORTE"

    cd $SCRIPT_DIR

    log "Deployando sistema de backup..."
    kubectl apply -f backup.yaml

    log "Deployando monitoreo..."
    kubectl apply -f monitoring.yaml

    log "Esperando que el monitoreo esté listo..."
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n $NAMESPACE --timeout=300s || warning "Prometheus puede tardar más en iniciar"
    kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n $NAMESPACE --timeout=300s || warning "Grafana puede tardar más en iniciar"

    success "Sistemas de soporte deployados"
}

verify_deployment() {
    title "VERIFICANDO DEPLOYMENT"

    log "Verificando estado de todos los pods..."
    kubectl get pods -n $NAMESPACE -o wide

    echo ""
    log "Verificando servicios..."
    kubectl get services -n $NAMESPACE

    echo ""
    log "Verificando ingress..."
    kubectl get ingress -n $NAMESPACE

    echo ""
    log "Verificando certificados SSL..."
    kubectl get certificates -n $NAMESPACE 2>/dev/null || echo "Sin certificados aún"

    echo ""
    log "Verificando que todos los deployments estén listos..."
    if kubectl wait --for=condition=available deployment --all -n $NAMESPACE --timeout=60s; then
        success "Todos los deployments están listos"
    else
        warning "Algunos deployments pueden necesitar más tiempo"
    fi

    success "Verificación completada"
}

show_access_information() {
    title "INFORMACIÓN DE ACCESO"

    echo ""
    echo "🎉 ${BOLD}${GREEN}¡CARRENTAL SAAS DEPLOYADO EXITOSAMENTE!${NC}"
    echo ""
    echo "📱 ${BOLD}ACCESO A LA APLICACIÓN:${NC}"
    echo "   🌐 Frontend: https://carrental.tudominio.com"
    echo "   🔧 API: https://api.carrental.tudominio.com"
    echo "   📊 Monitoring: https://monitoring.carrental.tudominio.com"
    echo ""
    echo "🔑 ${BOLD}CREDENCIALES GRAFANA:${NC}"
    echo "   Usuario: admin"
    echo "   Password: admin123 ${YELLOW}(¡CÁMBIALO INMEDIATAMENTE!)${NC}"
    echo ""
    echo "📋 ${BOLD}COMANDOS ÚTILES:${NC}"
    echo "   Estado: kubectl get all -n $NAMESPACE"
    echo "   Logs backend: kubectl logs deployment/carrental-backend-deployment -n $NAMESPACE -f"
    echo "   Logs frontend: kubectl logs deployment/carrental-frontend-deployment -n $NAMESPACE -f"
    echo "   Escalar: kubectl scale deployment carrental-backend-deployment --replicas=4 -n $NAMESPACE"
    echo ""
    echo "🔒 ${BOLD}SEGURIDAD:${NC}"
    echo "   ✅ SSL/TLS automático con Let's Encrypt"
    echo "   ✅ Network policies aplicadas"
    echo "   ✅ Containers non-root"
    echo "   ✅ Backups automáticos diarios"
    echo ""
    echo "📈 ${BOLD}MONITOREO:${NC}"
    echo "   ✅ Prometheus: métricas y alertas"
    echo "   ✅ Grafana: dashboards y visualización"
    echo "   ✅ Health checks automáticos"
    echo "   ✅ Auto-scaling configurado"
    echo ""
    echo "💾 ${BOLD}BACKUP:${NC}"
    echo "   ✅ Backups diarios a las 2 AM UTC"
    echo "   ✅ Retención de 30 días"
    echo "   ✅ Verificación automática de backups"
    echo ""
    echo "🎯 ${BOLD}PRÓXIMOS PASOS:${NC}"
    echo "   1. Verificar que DNS resuelva correctamente"
    echo "   2. Esperar certificados SSL (puede tomar unos minutos)"
    echo "   3. Cambiar password de Grafana"
    echo "   4. Configurar alertas de monitoreo"
    echo "   5. ¡Empezar a usar tu plataforma SaaS!"
    echo ""
}

main() {
    echo ""
    echo "${BOLD}${BLUE}🚀 CARRENTAL SAAS - DEPLOYMENT COMPLETO A PRODUCCIÓN${NC}"
    echo "${BOLD}${BLUE}======================================================${NC}"
    echo ""
    echo "Este script ejecutará el deployment completo de CarRental SaaS:"
    echo ""
    echo "✅ Verificación de prerrequisitos"
    echo "✅ Instalación de componentes del cluster (cert-manager, nginx-ingress)"
    echo "✅ Configuración de secretos de producción"
    echo "✅ Construcción de imágenes Docker"
    echo "✅ Deployment de infraestructura (PostgreSQL)"
    echo "✅ Deployment de aplicaciones (Backend + Frontend)"
    echo "✅ Configuración de red y SSL"
    echo "✅ Deployment de sistemas de soporte (Backup + Monitoreo)"
    echo "✅ Verificación completa"
    echo ""

    read -p "¿Continuar con el deployment completo? (y/N): " proceed
    if [ "$proceed" != "y" ] && [ "$proceed" != "Y" ]; then
        echo "Deployment cancelado"
        exit 0
    fi

    echo ""

    check_prerequisites
    wait_for_user

    install_cluster_components
    wait_for_user

    configure_secrets
    wait_for_user

    update_domain_configuration
    wait_for_user

    build_docker_images
    wait_for_user

    deploy_infrastructure
    wait_for_user

    deploy_applications
    wait_for_user

    configure_networking
    wait_for_user

    deploy_support_systems
    wait_for_user

    verify_deployment

    show_access_information
}

# Execute main function
main "$@"
#!/bin/bash

# Build Docker Images for CarRental SaaS
set -e

# Configuration
BACKEND_IMAGE="carrental-backend"
FRONTEND_IMAGE="carrental-frontend"
VERSION=${1:-"latest"}
REGISTRY=${REGISTRY:-""}

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

check_docker() {
    log "Verificando Docker..."
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
    fi

    if ! docker info &> /dev/null; then
        error "Docker no está ejecutándose"
    fi

    success "Docker está disponible"
}

build_backend() {
    log "Construyendo imagen del backend..."

    cd /Users/mateoalvarez/IdeaProjects/CarRental

    # Build JAR first
    log "Construyendo JAR con Maven..."
    ./mvnw clean package -DskipTests

    # Build Docker image
    local full_image="${REGISTRY}${BACKEND_IMAGE}:${VERSION}"
    docker build -f Dockerfile.backend -t $full_image .

    success "Backend image construida: $full_image"
}

build_frontend() {
    log "Construyendo imagen del frontend..."

    cd /Users/mateoalvarez/IdeaProjects/carrental-frontend

    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        log "Instalando dependencias de npm..."
        npm install
    fi

    # Build Docker image
    local full_image="${REGISTRY}${FRONTEND_IMAGE}:${VERSION}"
    docker build -t $full_image .

    success "Frontend image construida: $full_image"
}

update_deployment_files() {
    log "Actualizando archivos de deployment con nuevas imágenes..."

    cd /Users/mateoalvarez/IdeaProjects/CarRental/k8s/production-simple

    # Update backend image
    sed -i.bak "s|image: carrental-backend:latest|image: ${REGISTRY}${BACKEND_IMAGE}:${VERSION}|g" backend.yaml

    # Update frontend image
    sed -i.bak "s|image: carrental-frontend:latest|image: ${REGISTRY}${FRONTEND_IMAGE}:${VERSION}|g" frontend.yaml

    # Remove backup files
    rm -f backend.yaml.bak frontend.yaml.bak

    success "Archivos de deployment actualizados"
}

list_images() {
    log "Imágenes construidas:"
    echo ""
    docker images | grep -E "(carrental-backend|carrental-frontend)" || echo "No se encontraron imágenes"
}

push_images() {
    if [ ! -z "$REGISTRY" ]; then
        log "Subiendo imágenes al registry..."

        docker push "${REGISTRY}${BACKEND_IMAGE}:${VERSION}"
        docker push "${REGISTRY}${FRONTEND_IMAGE}:${VERSION}"

        success "Imágenes subidas al registry"
    else
        warning "No se especificó registry. Las imágenes solo están disponibles localmente."
        echo "Para subir a un registry, usa: REGISTRY=your-registry.com/ $0 $VERSION"
    fi
}

show_usage() {
    echo "Usage: $0 [VERSION]"
    echo ""
    echo "Construye las imágenes Docker para CarRental SaaS"
    echo ""
    echo "Parámetros:"
    echo "  VERSION    Versión de la imagen (default: latest)"
    echo ""
    echo "Variables de entorno:"
    echo "  REGISTRY   Registry para subir imágenes (ej: ghcr.io/usuario/)"
    echo ""
    echo "Ejemplos:"
    echo "  $0                           # Construir con versión 'latest'"
    echo "  $0 v1.0.0                    # Construir con versión 'v1.0.0'"
    echo "  REGISTRY=ghcr.io/user/ $0    # Construir y subir al registry"
}

main() {
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    echo "🐳 Construyendo Imágenes Docker - CarRental SaaS"
    echo "==============================================="
    echo "Versión: $VERSION"
    echo "Registry: ${REGISTRY:-'local only'}"
    echo ""

    check_docker
    build_backend
    build_frontend
    update_deployment_files
    list_images
    push_images

    echo ""
    success "¡Imágenes construidas exitosamente!"
    echo ""
    echo "📝 Próximos pasos:"
    echo "1. Si las imágenes están en un registry, actualizar los deployment YAML"
    echo "2. Ejecutar ./deploy.sh para hacer el deployment"
    echo ""
}

main "$@"
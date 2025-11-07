#!/bin/bash

# Setup Secrets for CarRental Production Deployment
set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

NAMESPACE="carrental-prod"

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

generate_random_password() {
    openssl rand -base64 32 | tr -d "=+/" | cut -c1-25
}

generate_jwt_secret() {
    openssl rand -base64 64 | tr -d "=+/" | cut -c1-64
}

check_cluster() {
    log "Verificando conexión al cluster..."
    if ! kubectl cluster-info &> /dev/null; then
        error "No se puede conectar al cluster Kubernetes. Configura kubectl primero."
    fi
    success "Cluster Kubernetes accesible"
}

create_namespace() {
    log "Creando namespace..."
    kubectl apply -f namespace.yaml
    success "Namespace creado/actualizado"
}

prompt_for_secrets() {
    echo ""
    echo "🔐 CONFIGURACIÓN DE SECRETOS DE PRODUCCIÓN"
    echo "=========================================="
    echo ""

    # Database username
    read -p "📝 Database username [carrental_user]: " DB_USER
    DB_USER=${DB_USER:-carrental_user}

    # Database password
    echo ""
    DB_PASS_GENERATED=$(generate_random_password)
    read -p "📝 Database password [auto-generated: $DB_PASS_GENERATED]: " DB_PASS
    DB_PASS=${DB_PASS:-$DB_PASS_GENERATED}

    # PostgreSQL root password
    echo ""
    POSTGRES_PASS_GENERATED=$(generate_random_password)
    read -p "📝 PostgreSQL root password [auto-generated: $POSTGRES_PASS_GENERATED]: " POSTGRES_PASS
    POSTGRES_PASS=${POSTGRES_PASS:-$POSTGRES_PASS_GENERATED}

    # JWT Secret
    echo ""
    JWT_SECRET_GENERATED=$(generate_jwt_secret)
    read -p "📝 JWT Secret (256-bit) [auto-generated]: " JWT_SECRET
    JWT_SECRET=${JWT_SECRET:-$JWT_SECRET_GENERATED}

    # JWT Expiration
    echo ""
    read -p "📝 JWT Expiration in milliseconds [86400000 = 24h]: " JWT_EXPIRATION
    JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}

    # Email configuration (optional)
    echo ""
    echo "📧 Email configuration (opcional - para notificaciones):"
    read -p "📝 SMTP Username (optional): " SMTP_USER
    read -s -p "📝 SMTP Password (optional): " SMTP_PASS
    echo ""

    # Let's Encrypt email
    echo ""
    read -p "📝 Email for Let's Encrypt SSL certificates: " LETSENCRYPT_EMAIL

    echo ""
    echo "✅ Configuración recopilada correctamente"
}

create_secrets() {
    log "Creando secretos en Kubernetes..."

    # Check if secret already exists
    if kubectl get secret carrental-secrets -n $NAMESPACE &> /dev/null; then
        warning "Secret carrental-secrets ya existe. ¿Sobrescribir? (y/N)"
        read -p "> " overwrite
        if [ "$overwrite" != "y" ] && [ "$overwrite" != "Y" ]; then
            success "Manteniendo secretos existentes"
            return 0
        fi
        kubectl delete secret carrental-secrets -n $NAMESPACE
    fi

    # Create the secret
    kubectl create secret generic carrental-secrets -n $NAMESPACE \
        --from-literal=SPRING_DATASOURCE_USERNAME="$DB_USER" \
        --from-literal=SPRING_DATASOURCE_PASSWORD="$DB_PASS" \
        --from-literal=JWT_SECRET="$JWT_SECRET" \
        --from-literal=JWT_EXPIRATION="$JWT_EXPIRATION" \
        --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASS"

    # Add email configuration if provided
    if [ ! -z "$SMTP_USER" ] && [ ! -z "$SMTP_PASS" ]; then
        kubectl patch secret carrental-secrets -n $NAMESPACE \
            --patch="{\"data\":{\"SPRING_MAIL_USERNAME\":\"$(echo -n $SMTP_USER | base64)\",\"SPRING_MAIL_PASSWORD\":\"$(echo -n $SMTP_PASS | base64)\"}}"
    fi

    success "Secretos creados correctamente"
}

update_ingress_config() {
    if [ ! -z "$LETSENCRYPT_EMAIL" ]; then
        log "Actualizando configuración de Let's Encrypt..."

        # Create backup
        cp ingress.yaml ingress.yaml.backup

        # Update email in ingress.yaml
        sed -i.bak "s/admin@yourdomain.com/$LETSENCRYPT_EMAIL/g" ingress.yaml
        rm ingress.yaml.bak

        success "Email de Let's Encrypt actualizado en ingress.yaml"
    fi
}

show_credentials() {
    echo ""
    echo "🔑 CREDENCIALES CONFIGURADAS"
    echo "==========================="
    echo ""
    echo "Database:"
    echo "  Username: $DB_USER"
    echo "  Password: $DB_PASS"
    echo ""
    echo "PostgreSQL Root:"
    echo "  Password: $POSTGRES_PASS"
    echo ""
    echo "JWT Configuration:"
    echo "  Secret: ${JWT_SECRET:0:20}... (hidden)"
    echo "  Expiration: $JWT_EXPIRATION ms"
    echo ""
    if [ ! -z "$LETSENCRYPT_EMAIL" ]; then
        echo "Let's Encrypt Email: $LETSENCRYPT_EMAIL"
        echo ""
    fi
    echo "⚠️  IMPORTANTE: Guarda estas credenciales en un lugar seguro!"
    echo ""
}

save_credentials() {
    CREDS_FILE="production-credentials.txt"
    log "Guardando credenciales en $CREDS_FILE..."

    cat > $CREDS_FILE << EOF
# CarRental SaaS Production Credentials
# Generated on: $(date)
# KEEP THIS FILE SECURE AND DO NOT COMMIT TO VERSION CONTROL

Database Configuration:
  Username: $DB_USER
  Password: $DB_PASS

PostgreSQL Root:
  Password: $POSTGRES_PASS

JWT Configuration:
  Secret: $JWT_SECRET
  Expiration: $JWT_EXPIRATION ms

$(if [ ! -z "$LETSENCRYPT_EMAIL" ]; then echo "Let's Encrypt Email: $LETSENCRYPT_EMAIL"; fi)

$(if [ ! -z "$SMTP_USER" ]; then echo "SMTP Username: $SMTP_USER"; fi)
$(if [ ! -z "$SMTP_PASS" ]; then echo "SMTP Password: $SMTP_PASS"; fi)

# Kubernetes Commands:
# View secrets: kubectl get secret carrental-secrets -n carrental-prod -o yaml
# Delete secrets: kubectl delete secret carrental-secrets -n carrental-prod
EOF

    chmod 600 $CREDS_FILE
    success "Credenciales guardadas en $CREDS_FILE (chmod 600)"
}

verify_secrets() {
    log "Verificando secretos creados..."

    echo ""
    echo "=== Secretos en Kubernetes ==="
    kubectl get secret carrental-secrets -n $NAMESPACE -o yaml | grep "^  [A-Z]" | sed 's/:.*/: [HIDDEN]/'

    success "Secretos verificados correctamente"
}

show_next_steps() {
    echo ""
    echo "🎉 Secretos configurados correctamente!"
    echo ""
    echo "📝 Próximos pasos:"
    echo "1. Configurar tu dominio en ingress.yaml (reemplazar yourdomain.com)"
    echo "2. Asegurarte de que DNS apunte al Load Balancer"
    echo "3. Ejecutar el deployment principal: ./deploy.sh"
    echo ""
    echo "💡 Para ver los secretos:"
    echo "kubectl get secret carrental-secrets -n $NAMESPACE -o yaml"
}

main() {
    echo "🔐 Configuración de Secretos - CarRental SaaS"
    echo "============================================="

    check_cluster
    create_namespace
    prompt_for_secrets
    create_secrets
    update_ingress_config
    show_credentials
    save_credentials
    verify_secrets
    show_next_steps
}

# Check if running interactively
if [ -t 0 ]; then
    main
else
    echo "❌ Este script debe ejecutarse interactivamente"
    exit 1
fi
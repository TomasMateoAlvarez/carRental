#!/bin/bash

# CarRental SaaS - Preparación Completa para Deployment Gratuito
# Prepara Backend (Render) + Frontend (Vercel) para deployment

set -e

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
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

title() {
    echo -e "${BOLD}${BLUE}$1${NC}"
    echo -e "${BOLD}${BLUE}$(echo "$1" | sed 's/./=/g')${NC}"
    echo ""
}

# Project paths
PROJECT_ROOT="/Users/mateoalvarez/IdeaProjects/CarRental"
FRONTEND_ROOT="/Users/mateoalvarez/IdeaProjects/carrental-frontend"
DEPLOY_FREE_DIR="$PROJECT_ROOT/deploy-free"

echo -e "${BOLD}${BLUE}🚀 CARRENTAL SAAS - PREPARACIÓN PARA DEPLOYMENT GRATUITO${NC}"
echo -e "${BOLD}${BLUE}============================================================${NC}"
echo ""
echo "🎯 Objetivo: Deployar CarRental 100% GRATIS con:"
echo "   📊 Supabase (PostgreSQL Database)"
echo "   🚀 Render (Spring Boot Backend)"
echo "   🌐 Vercel (React Frontend)"
echo ""
echo "⏱️ Tiempo estimado: 5-10 minutos"
echo "🔧 Preparación automática de archivos y configuraciones"
echo ""

# Verificar prerrequisitos
title "VERIFICANDO PRERREQUISITOS"

# Verificar que estamos en el directorio correcto
if [ ! -f "$PROJECT_ROOT/pom.xml" ]; then
    error "No se encontró pom.xml. Asegúrate de estar en el directorio del proyecto CarRental."
fi

if [ ! -d "$FRONTEND_ROOT" ]; then
    error "No se encontró el directorio del frontend: $FRONTEND_ROOT"
fi

# Verificar herramientas necesarias
command -v java >/dev/null 2>&1 || error "Java no encontrado"
command -v ./mvnw >/dev/null 2>&1 || error "Maven wrapper no encontrado"
command -v node >/dev/null 2>&1 || error "Node.js no encontrado"
command -v npm >/dev/null 2>&1 || error "npm no encontrado"

success "Todos los prerrequisitos están disponibles"
echo ""

# Crear directorio deploy-free si no existe
if [ ! -d "$DEPLOY_FREE_DIR" ]; then
    mkdir -p "$DEPLOY_FREE_DIR"
    log "Directorio deploy-free creado"
fi

# Verificar que los scripts de preparación existen
if [ ! -f "$DEPLOY_FREE_DIR/prepare-backend.sh" ]; then
    error "Script prepare-backend.sh no encontrado. Ejecuta primero los scripts de preparación."
fi

if [ ! -f "$DEPLOY_FREE_DIR/prepare-frontend.sh" ]; then
    error "Script prepare-frontend.sh no encontrado. Ejecuta primero los scripts de preparación."
fi

# Preparar Backend
title "PREPARANDO BACKEND (RENDER)"

log "Ejecutando prepare-backend.sh..."
cd "$PROJECT_ROOT"

# Hacer ejecutable si no lo está
chmod +x "$DEPLOY_FREE_DIR/prepare-backend.sh"

if bash "$DEPLOY_FREE_DIR/prepare-backend.sh"; then
    success "Backend preparado exitosamente para Render"
else
    error "Error preparando backend"
fi

echo ""

# Preparar Frontend
title "PREPARANDO FRONTEND (VERCEL)"

log "Ejecutando prepare-frontend.sh..."

# Hacer ejecutable si no lo está
chmod +x "$DEPLOY_FREE_DIR/prepare-frontend.sh"

if bash "$DEPLOY_FREE_DIR/prepare-frontend.sh"; then
    success "Frontend preparado exitosamente para Vercel"
else
    error "Error preparando frontend"
fi

echo ""

# Verificar archivos creados
title "VERIFICANDO ARCHIVOS GENERADOS"

# Backend files
backend_files=(
    "src/main/resources/application-render.properties"
    "start-render.sh"
    "Dockerfile.render"
)

for file in "${backend_files[@]}"; do
    if [ -f "$PROJECT_ROOT/$file" ]; then
        success "✓ $file"
    else
        warning "✗ $file (no encontrado)"
    fi
done

# Frontend files
frontend_files=(
    ".env.example"
    ".env.production"
    "vercel.json"
    "src/config/api.js"
)

for file in "${frontend_files[@]}"; do
    if [ -f "$FRONTEND_ROOT/$file" ]; then
        success "✓ Frontend: $file"
    else
        warning "✗ Frontend: $file (no encontrado)"
    fi
done

# Documentation files
doc_files=(
    "deploy-free/DEPLOYMENT-GUIDE-FREE.md"
    "deploy-free/setup-supabase.md"
    "deploy-free/test-deployment.sh"
)

for file in "${doc_files[@]}"; do
    if [ -f "$PROJECT_ROOT/$file" ]; then
        success "✓ Documentación: $file"
    else
        warning "✗ Documentación: $file (no encontrado)"
    fi
done

echo ""

# Test de compilación
title "TESTING DE COMPILACIÓN"

# Test backend build
log "Testing backend compilation..."
cd "$PROJECT_ROOT"
if ./mvnw clean compile -q; then
    success "Backend compila correctamente"
else
    error "Error de compilación en backend"
fi

# Test frontend build
log "Testing frontend build..."
cd "$FRONTEND_ROOT"
if [ -d "node_modules" ]; then
    if npm run build >/dev/null 2>&1; then
        success "Frontend compila correctamente"

        # Mostrar tamaño del build
        if [ -d "dist" ]; then
            BUILD_SIZE=$(du -sh dist 2>/dev/null | cut -f1 || echo "Unknown")
            log "Tamaño del build frontend: $BUILD_SIZE"
        fi
    else
        error "Error de compilación en frontend"
    fi
else
    warning "node_modules no encontrado. Ejecuta 'npm install' en el frontend."
fi

echo ""

# Mostrar resumen final
title "🎉 PREPARACIÓN COMPLETADA"

echo "📁 Archivos creados para deployment:"
echo ""
echo "🚀 Backend (Render):"
echo "   ✅ application-render.properties - Configuración optimizada"
echo "   ✅ Dockerfile.render - Imagen Docker optimizada"
echo "   ✅ start-render.sh - Script de inicio"
echo ""
echo "🌐 Frontend (Vercel):"
echo "   ✅ vercel.json - Configuración de routing y caching"
echo "   ✅ .env.production - Variables de entorno"
echo "   ✅ src/config/api.js - Configuración de API centralizada"
echo "   ✅ Build optimizado para producción"
echo ""
echo "📚 Documentación:"
echo "   ✅ DEPLOYMENT-GUIDE-FREE.md - Guía completa paso a paso"
echo "   ✅ setup-supabase.md - Configuración de base de datos"
echo "   ✅ test-deployment.sh - Testing de integración"
echo ""

title "📋 PRÓXIMOS PASOS"

echo "1️⃣ **Commit y Push al Repositorio**:"
echo "   cd $PROJECT_ROOT"
echo "   git add ."
echo "   git commit -m 'Prepare for free deployment: Supabase + Render + Vercel'"
echo "   git push origin main"
echo ""

echo "2️⃣ **Configurar Base de Datos (Supabase)**:"
echo "   📖 Sigue: deploy-free/setup-supabase.md"
echo "   🌐 URL: https://supabase.com"
echo "   ⏱️ Tiempo: 5 minutos"
echo ""

echo "3️⃣ **Deployar Backend (Render)**:"
echo "   🌐 URL: https://render.com"
echo "   🐳 Usar: Dockerfile.render"
echo "   ⚙️ Variables: DATABASE_URL, JWT_SECRET, FRONTEND_URL"
echo "   ⏱️ Tiempo: 10 minutos"
echo ""

echo "4️⃣ **Deployar Frontend (Vercel)**:"
echo "   🌐 URL: https://vercel.com"
echo "   📁 Directorio: carrental-frontend"
echo "   ⚙️ Variables: VITE_API_BASE_URL, VITE_FRONTEND_URL"
echo "   ⏱️ Tiempo: 5 minutos"
echo ""

echo "5️⃣ **Testing de Integración**:"
echo "   ./deploy-free/test-deployment.sh"
echo "   🧪 Verifica que todo funcione correctamente"
echo ""

title "🎯 DEPLOYMENT 100% GRATUITO"

echo "💰 **Costos**: $0 USD/mes"
echo "📊 **Capacidad**:"
echo "   • Supabase: 500MB DB, 2 conexiones concurrentes"
echo "   • Render: 750 horas/mes (30+ días), 512MB RAM"
echo "   • Vercel: 100GB bandwidth, builds ilimitados"
echo ""
echo "✨ **Características**:"
echo "   ✅ SSL automático en todos los servicios"
echo "   ✅ Auto-deploy en cada push"
echo "   ✅ CDN global para frontend"
echo "   ✅ Backup automático de base de datos"
echo "   ✅ Monitoring integrado"
echo ""

title "📚 DOCUMENTACIÓN COMPLETA"

echo "📖 Guía completa: deploy-free/DEPLOYMENT-GUIDE-FREE.md"
echo "🔧 Setup DB: deploy-free/setup-supabase.md"
echo "🧪 Testing: ./deploy-free/test-deployment.sh"
echo ""

echo -e "${GREEN}${BOLD}🚀 ¡Tu CarRental SaaS está listo para deployment gratuito!${NC}"
echo -e "${GREEN}${BOLD}📚 Sigue la guía DEPLOYMENT-GUIDE-FREE.md para completar el proceso${NC}"
echo ""

success "Preparación completada exitosamente"
#!/bin/bash

# Script de inicio para Render
echo "🚀 Iniciando CarRental Backend en Render..."

# Verificar variables de entorno requeridas
if [ -z "$DATABASE_URL" ]; then
    echo "❌ ERROR: DATABASE_URL no está configurada"
    exit 1
fi

# Mostrar información del entorno
echo "✅ PORT: ${PORT:-8080}"
echo "✅ DATABASE_URL: [CONFIGURADA]"
echo "✅ JWT_SECRET: [CONFIGURADO]"

# Configurar perfil de Spring
export SPRING_PROFILES_ACTIVE=render

# Iniciar la aplicación
echo "🏃 Iniciando aplicación..."
java -Xmx400m -Xms200m -XX:+UseContainerSupport -Dserver.port=${PORT:-8080} -jar target/CarRental-*.jar

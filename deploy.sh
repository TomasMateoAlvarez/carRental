#!/bin/bash

# CarRental Production Deployment Script
# Usage: ./deploy.sh [environment]
# Environments: staging, production

set -e

ENVIRONMENT=${1:-staging}
DOCKER_COMPOSE_FILE="docker-compose.production.yml"
ENV_FILE=".env.${ENVIRONMENT}"

echo "🚀 Starting CarRental deployment for ${ENVIRONMENT}..."

# Check if environment file exists
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ Environment file $ENV_FILE not found!"
    echo "Please create it with the required environment variables."
    exit 1
fi

# Load environment variables
source "$ENV_FILE"

# Pre-deployment checks
echo "🔍 Running pre-deployment checks..."

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running!"
    exit 1
fi

# Check if required environment variables are set
required_vars=("DB_PASSWORD" "JWT_SECRET" "REDIS_PASSWORD")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo "❌ Required environment variable $var is not set!"
        exit 1
    fi
done

# Backup database (if production)
if [ "$ENVIRONMENT" = "production" ]; then
    echo "💾 Creating database backup..."
    timestamp=$(date +"%Y%m%d_%H%M%S")
    docker exec carrental-postgres pg_dump -U carrental carrental_db > "backup_${timestamp}.sql"
    echo "✅ Database backup created: backup_${timestamp}.sql"
fi

# Pull latest images
echo "📥 Pulling latest Docker images..."
docker-compose -f "$DOCKER_COMPOSE_FILE" pull

# Stop services
echo "🛑 Stopping existing services..."
docker-compose -f "$DOCKER_COMPOSE_FILE" down

# Start services
echo "🚀 Starting services..."
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
timeout=300
elapsed=0
while [ $elapsed -lt $timeout ]; do
    if docker-compose -f "$DOCKER_COMPOSE_FILE" ps | grep -q "healthy"; then
        echo "✅ Services are healthy!"
        break
    fi
    sleep 10
    elapsed=$((elapsed + 10))
    echo "Waiting... ${elapsed}s"
done

if [ $elapsed -ge $timeout ]; then
    echo "❌ Services failed to become healthy within ${timeout}s"
    echo "Checking logs..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" logs --tail=50
    exit 1
fi

# Run smoke tests
echo "🧪 Running smoke tests..."
backend_url="http://localhost:8083"
frontend_url="http://localhost:80"

# Test backend health
if curl -f "$backend_url/actuator/health" >/dev/null 2>&1; then
    echo "✅ Backend health check passed"
else
    echo "❌ Backend health check failed"
    exit 1
fi

# Test frontend
if curl -f "$frontend_url/health" >/dev/null 2>&1; then
    echo "✅ Frontend health check passed"
else
    echo "❌ Frontend health check failed"
    exit 1
fi

# Display service status
echo "📊 Service Status:"
docker-compose -f "$DOCKER_COMPOSE_FILE" ps

echo "🎉 Deployment completed successfully!"
echo ""
echo "🌐 Services are available at:"
echo "   Frontend: http://localhost"
echo "   Backend API: http://localhost:8083/api/v1"
echo "   Grafana: http://localhost:3000 (admin/admin123)"
echo "   Prometheus: http://localhost:9090"
echo ""
echo "📝 To view logs: docker-compose -f $DOCKER_COMPOSE_FILE logs -f"
echo "🛑 To stop: docker-compose -f $DOCKER_COMPOSE_FILE down"
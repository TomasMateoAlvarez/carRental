#!/bin/bash

# CarRental SaaS - Production Deployment Script
# This script builds and deploys the CarRental application in production mode

set -e  # Exit on any error

echo "🚀 Starting CarRental SaaS Production Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "docker-compose is not installed. Please install docker-compose and try again."
    exit 1
fi

# Check if .env.production exists
if [ ! -f ".env.production" ]; then
    print_warning ".env.production file not found. Copying from .env.production template..."
    if [ -f ".env.production" ]; then
        cp .env.production .env
        print_warning "Please update .env file with your production values before continuing."
        read -p "Press Enter to continue once you've updated the .env file..."
    else
        print_error "No environment template found. Please create .env file manually."
        exit 1
    fi
fi

# Create necessary directories
print_status "Creating necessary directories..."
mkdir -p nginx/ssl
mkdir -p nginx/conf.d
mkdir -p logs
mkdir -p backups

# Check if SSL certificates exist
if [ ! -f "nginx/ssl/cert.pem" ] || [ ! -f "nginx/ssl/key.pem" ]; then
    print_warning "SSL certificates not found. Generating self-signed certificates for development..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/key.pem \
        -out nginx/ssl/cert.pem \
        -subj "/C=US/ST=State/L=City/O=Organization/CN=localhost"
    print_warning "⚠️  Using self-signed certificates. Replace with valid certificates for production!"
fi

# Pull latest images
print_status "Pulling latest base images..."
docker-compose -f docker-compose.production.yml pull postgres redis nginx prometheus grafana

# Build the application
print_status "Building application images..."
docker-compose -f docker-compose.production.yml build --no-cache

# Stop existing containers
print_status "Stopping existing containers..."
docker-compose -f docker-compose.production.yml down --remove-orphans

# Start the application
print_status "Starting CarRental SaaS in production mode..."
docker-compose -f docker-compose.production.yml up -d

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
max_attempts=60
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker-compose -f docker-compose.production.yml ps | grep -q "Up (healthy)"; then
        healthy_services=$(docker-compose -f docker-compose.production.yml ps | grep "Up (healthy)" | wc -l)
        total_services=$(docker-compose -f docker-compose.production.yml ps | grep "Up" | wc -l)

        print_status "Healthy services: $healthy_services/$total_services"

        if [ "$healthy_services" -eq "$total_services" ]; then
            break
        fi
    fi

    attempt=$((attempt + 1))
    sleep 5
done

# Check if all services are healthy
if [ $attempt -eq $max_attempts ]; then
    print_error "Timeout waiting for services to be healthy. Checking logs..."
    docker-compose -f docker-compose.production.yml logs --tail=20
    exit 1
fi

print_success "All services are healthy!"

# Display service URLs
print_success "🎉 CarRental SaaS deployed successfully!"
echo ""
echo "📋 Service Information:"
echo "======================================"
echo "🌐 Application:     http://localhost"
echo "🌐 HTTPS:           https://localhost (self-signed)"
echo "🔧 API Health:      http://localhost/api/health"
echo "📊 Prometheus:      http://localhost:9090"
echo "📈 Grafana:         http://localhost:3000 (admin/admin123)"
echo "🗄️  Database:       localhost:5432 (carrental/SecurePostgresPassword2024!)"
echo "🔄 Redis:           localhost:6379 (SecureRedisPassword2024!)"
echo ""

# Show running containers
print_status "Running containers:"
docker-compose -f docker-compose.production.yml ps

echo ""
print_success "✅ Deployment completed successfully!"
print_warning "📝 Next steps:"
echo "   1. Update .env file with your production secrets"
echo "   2. Replace self-signed SSL certificates with valid ones"
echo "   3. Configure your domain and DNS settings"
echo "   4. Set up automated backups"
echo "   5. Configure monitoring alerts"
echo ""
print_status "To view logs: docker-compose -f docker-compose.production.yml logs -f"
print_status "To stop:      docker-compose -f docker-compose.production.yml down"
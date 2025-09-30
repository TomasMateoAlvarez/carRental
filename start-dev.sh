#!/bin/bash

# Car Rental Development Environment Startup Script

echo "🚗 Starting Car Rental SaaS Development Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Check if .env file exists, create from example if not
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  Creating .env file from .env.example${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${YELLOW}⚠️  Please edit .env file with your credentials before running again${NC}"
        exit 1
    else
        echo -e "${RED}❌ .env.example file not found${NC}"
        exit 1
    fi
fi

# Stop any existing containers
echo -e "${BLUE}🛑 Stopping existing containers...${NC}"
docker-compose down

# Build and start services
echo -e "${BLUE}🏗️  Building and starting services...${NC}"
docker-compose up --build -d

# Wait for database to be ready
echo -e "${BLUE}⏳ Waiting for database to be ready...${NC}"
until docker-compose exec -T db pg_isready -U carrental -d carrental_db > /dev/null 2>&1; do
    echo -e "${YELLOW}   Waiting for PostgreSQL...${NC}"
    sleep 2
done

echo -e "${GREEN}✅ Database is ready${NC}"

# Wait for application to start
echo -e "${BLUE}⏳ Waiting for application to start...${NC}"
until curl -f http://localhost:8083/actuator/health > /dev/null 2>&1; do
    echo -e "${YELLOW}   Waiting for application...${NC}"
    sleep 3
done

echo -e "${GREEN}🎉 Car Rental SaaS is ready!${NC}"
echo ""
echo -e "${GREEN}📋 Services Available:${NC}"
echo -e "   🚗 Application:    http://localhost:8083"
echo -e "   💾 Database:       localhost:5432"
echo -e "   📊 Actuator:       http://localhost:8083/actuator"
echo -e "   🔍 Health Check:   http://localhost:8083/actuator/health"
echo -e "   📈 Metrics:        http://localhost:8083/actuator/metrics"
echo -e "   🎯 Prometheus:     http://localhost:9090"
echo -e "   💨 Redis:          localhost:6379"
echo ""
echo -e "${GREEN}🔐 Default Admin Credentials:${NC}"
echo -e "   Username: admin"
echo -e "   Password: admin123"
echo ""
echo -e "${BLUE}📖 API Documentation:${NC}"
echo -e "   Base URL: http://localhost:8083/api/v1"
echo -e "   Clients:  http://localhost:8083/clients"
echo -e "   Vehicles: http://localhost:8083/api/v1/vehicles"
echo ""
echo -e "${YELLOW}💡 Tip: Use 'docker-compose logs -f' to follow logs${NC}"
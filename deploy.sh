#!/bin/bash

# Mom Care Platform Deployment Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="mom-care-platform"
DOCKER_IMAGE="$APP_NAME"
VERSION=${1:-latest}
ENVIRONMENT=${2:-staging}

echo -e "${BLUE}🚀 Starting deployment of Mom Care Platform${NC}"
echo -e "${BLUE}📦 Version: $VERSION${NC}"
echo -e "${BLUE}🌍 Environment: $ENVIRONMENT${NC}"

# Function to print status
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

print_status "Docker is running"

# Build the application
echo -e "${BLUE}🏗️  Building application...${NC}"
./gradlew clean build -x test
print_status "Application built successfully"

# Build Docker image
echo -e "${BLUE}🐳 Building Docker image...${NC}"
docker build -t $DOCKER_IMAGE:$VERSION .
print_status "Docker image built successfully"

# Tag image for environment
docker tag $DOCKER_IMAGE:$VERSION $DOCKER_IMAGE:$ENVIRONMENT
print_status "Docker image tagged for $ENVIRONMENT"

# Stop existing containers
echo -e "${BLUE}🛑 Stopping existing containers...${NC}"
docker-compose -f docker-compose.yml down || true
print_status "Existing containers stopped"

# Start new containers
echo -e "${BLUE}🚀 Starting new containers...${NC}"
docker-compose -f docker-compose.yml up -d
print_status "New containers started"

# Wait for services to be ready
echo -e "${BLUE}⏳ Waiting for services to be ready...${NC}"
sleep 30

# Health check
echo -e "${BLUE}🏥 Performing health check...${NC}"
if curl -f http://localhost:8080/health > /dev/null 2>&1; then
    print_status "Health check passed"
else
    print_error "Health check failed"
    echo -e "${BLUE}📋 Container logs:${NC}"
    docker-compose logs mom-care-backend
    exit 1
fi

# Show running containers
echo -e "${BLUE}📋 Running containers:${NC}"
docker-compose ps

# Show logs
echo -e "${BLUE}📋 Recent logs:${NC}"
docker-compose logs --tail=20 mom-care-backend

print_status "Deployment completed successfully!"
echo -e "${BLUE}🌐 Application is available at: http://localhost:8080${NC}"
echo -e "${BLUE}📊 Monitoring dashboard: http://localhost:3000${NC}"
echo -e "${BLUE}📈 Prometheus metrics: http://localhost:9090${NC}"

# Cleanup old images
echo -e "${BLUE}🧹 Cleaning up old Docker images...${NC}"
docker image prune -f
print_status "Cleanup completed"

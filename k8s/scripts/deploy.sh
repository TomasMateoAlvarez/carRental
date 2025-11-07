#!/bin/bash

# CarRental Kubernetes Deployment Script
# This script deploys the complete CarRental SaaS platform to Kubernetes

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="carrental"
ENVIRONMENT="${ENVIRONMENT:-staging}"
KUBECTL_CONTEXT="${KUBECTL_CONTEXT:-}"
DRY_RUN="${DRY_RUN:-false}"
SKIP_BUILD="${SKIP_BUILD:-false}"
TIMEOUT="${TIMEOUT:-300}"

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_ROOT="$(dirname "$K8S_DIR")"

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."

    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi

    # Check if docker is installed (for building images)
    if [[ "$SKIP_BUILD" == "false" ]] && ! command -v docker &> /dev/null; then
        print_error "docker is not installed or not in PATH"
        exit 1
    fi

    # Check kubectl connectivity
    if ! kubectl cluster-info &> /dev/null; then
        print_error "Cannot connect to Kubernetes cluster"
        exit 1
    fi

    # Set kubectl context if provided
    if [[ -n "$KUBECTL_CONTEXT" ]]; then
        kubectl config use-context "$KUBECTL_CONTEXT"
        print_status "Using kubectl context: $KUBECTL_CONTEXT"
    fi

    print_status "Prerequisites check passed"
}

# Function to build and push Docker images
build_and_push_images() {
    if [[ "$SKIP_BUILD" == "true" ]]; then
        print_warning "Skipping image build (SKIP_BUILD=true)"
        return 0
    fi

    print_step "Building and pushing Docker images..."

    # Build backend image
    print_status "Building backend image..."
    cd "$PROJECT_ROOT"
    docker build -t ghcr.io/your-org/carrental-backend:latest \
        -t ghcr.io/your-org/carrental-backend:$(git rev-parse --short HEAD) \
        -f Dockerfile .

    # Build frontend image
    print_status "Building frontend image..."
    cd "$PROJECT_ROOT/../carrental-frontend"
    docker build -t ghcr.io/your-org/carrental-frontend:latest \
        -t ghcr.io/your-org/carrental-frontend:$(git rev-parse --short HEAD) \
        -f Dockerfile .

    # Push images
    print_status "Pushing images to registry..."
    docker push ghcr.io/your-org/carrental-backend:latest
    docker push ghcr.io/your-org/carrental-backend:$(git rev-parse --short HEAD)
    docker push ghcr.io/your-org/carrental-frontend:latest
    docker push ghcr.io/your-org/carrental-frontend:$(git rev-parse --short HEAD)

    print_status "Images built and pushed successfully"
}

# Function to create namespace
create_namespace() {
    print_step "Creating namespace: $NAMESPACE"

    if kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_warning "Namespace $NAMESPACE already exists"
    else
        kubectl apply -f "$K8S_DIR/base/namespace.yaml"
        print_status "Namespace $NAMESPACE created"
    fi
}

# Function to deploy infrastructure components
deploy_infrastructure() {
    print_step "Deploying infrastructure components..."

    # Deploy storage classes and persistent volumes
    print_status "Deploying persistent volumes..."
    kubectl apply -f "$K8S_DIR/base/persistent-volumes.yaml"

    # Deploy ConfigMaps
    print_status "Deploying ConfigMaps..."
    kubectl apply -f "$K8S_DIR/base/configmap.yaml"

    # Deploy Secrets (warning about security)
    print_warning "Deploying Secrets (ensure these are properly secured in production)"
    kubectl apply -f "$K8S_DIR/base/secrets.yaml"

    print_status "Infrastructure components deployed"
}

# Function to deploy databases
deploy_databases() {
    print_step "Deploying database services..."

    # Deploy PostgreSQL
    print_status "Deploying PostgreSQL..."
    kubectl apply -f "$K8S_DIR/base/postgres-deployment.yaml"

    # Deploy Redis
    print_status "Deploying Redis..."
    kubectl apply -f "$K8S_DIR/base/redis-deployment.yaml"

    # Wait for databases to be ready
    print_status "Waiting for databases to be ready..."
    kubectl wait --for=condition=Available deployment/postgres-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"
    kubectl wait --for=condition=Available deployment/redis-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"

    print_status "Database services deployed and ready"
}

# Function to deploy applications
deploy_applications() {
    print_step "Deploying application services..."

    # Deploy backend
    print_status "Deploying backend application..."
    kubectl apply -f "$K8S_DIR/base/backend-deployment.yaml"

    # Deploy frontend
    print_status "Deploying frontend application..."
    kubectl apply -f "$K8S_DIR/base/frontend-deployment.yaml"

    # Wait for applications to be ready
    print_status "Waiting for applications to be ready..."
    kubectl wait --for=condition=Available deployment/carrental-backend-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"
    kubectl wait --for=condition=Available deployment/carrental-frontend-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"
    kubectl wait --for=condition=Available deployment/nginx-proxy-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"

    print_status "Application services deployed and ready"
}

# Function to deploy networking
deploy_networking() {
    print_step "Deploying networking components..."

    # Deploy Ingress
    print_status "Deploying Ingress controller..."
    kubectl apply -f "$K8S_DIR/base/ingress.yaml"

    print_status "Networking components deployed"
}

# Function to deploy autoscaling
deploy_autoscaling() {
    print_step "Deploying autoscaling components..."

    # Deploy HPA
    print_status "Deploying Horizontal Pod Autoscalers..."
    kubectl apply -f "$K8S_DIR/base/hpa.yaml"

    print_status "Autoscaling components deployed"
}

# Function to deploy monitoring
deploy_monitoring() {
    print_step "Deploying monitoring stack..."

    # Deploy Prometheus and Grafana
    print_status "Deploying Prometheus and Grafana..."
    kubectl apply -f "$K8S_DIR/base/monitoring.yaml"

    # Wait for monitoring to be ready
    print_status "Waiting for monitoring services to be ready..."
    kubectl wait --for=condition=Available deployment/prometheus-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"
    kubectl wait --for=condition=Available deployment/grafana-deployment -n "$NAMESPACE" --timeout="${TIMEOUT}s"

    print_status "Monitoring stack deployed and ready"
}

# Function to verify deployment
verify_deployment() {
    print_step "Verifying deployment..."

    # Check all pods are running
    print_status "Checking pod status..."
    kubectl get pods -n "$NAMESPACE"

    # Check services
    print_status "Checking services..."
    kubectl get services -n "$NAMESPACE"

    # Check ingress
    print_status "Checking ingress..."
    kubectl get ingress -n "$NAMESPACE"

    # Check HPA
    print_status "Checking autoscaling..."
    kubectl get hpa -n "$NAMESPACE"

    # Health check endpoints
    print_status "Performing health checks..."

    # Get service IPs
    BACKEND_IP=$(kubectl get service carrental-backend-service -n "$NAMESPACE" -o jsonpath='{.spec.clusterIP}')

    # Check backend health
    if kubectl run health-check --image=curlimages/curl:latest --rm -i --restart=Never -- \
        curl -s "http://${BACKEND_IP}:8081/actuator/health" | grep -q "UP"; then
        print_status "Backend health check passed"
    else
        print_warning "Backend health check failed"
    fi

    print_status "Deployment verification completed"
}

# Function to show deployment info
show_deployment_info() {
    print_step "Deployment Information"

    echo "=============================================="
    echo "  CarRental SaaS Platform Deployed"
    echo "=============================================="
    echo "Namespace: $NAMESPACE"
    echo "Environment: $ENVIRONMENT"
    echo ""

    # Get ingress info
    INGRESS_IP=$(kubectl get ingress carrental-ingress -n "$NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending...")

    echo "Application URLs:"
    echo "  Frontend: http://${INGRESS_IP}/ (or http://carrental.com if DNS is configured)"
    echo "  Backend API: http://${INGRESS_IP}/api/v1"
    echo ""

    # Monitoring URLs
    GRAFANA_PORT=$(kubectl get service grafana-service -n "$NAMESPACE" -o jsonpath='{.spec.ports[0].port}')
    PROMETHEUS_PORT=$(kubectl get service prometheus-service -n "$NAMESPACE" -o jsonpath='{.spec.ports[0].port}')

    echo "Monitoring (port-forward required):"
    echo "  Grafana: kubectl port-forward service/grafana-service ${GRAFANA_PORT}:${GRAFANA_PORT} -n ${NAMESPACE}"
    echo "  Prometheus: kubectl port-forward service/prometheus-service ${PROMETHEUS_PORT}:${PROMETHEUS_PORT} -n ${NAMESPACE}"
    echo ""

    echo "Useful commands:"
    echo "  Watch pods: kubectl get pods -n $NAMESPACE -w"
    echo "  View logs: kubectl logs -f deployment/carrental-backend-deployment -n $NAMESPACE"
    echo "  Scale backend: kubectl scale deployment carrental-backend-deployment --replicas=5 -n $NAMESPACE"
    echo "=============================================="
}

# Function to show help
show_help() {
    cat << EOF
CarRental Kubernetes Deployment Script

Usage: $0 [OPTIONS]

Options:
  -h, --help              Show this help message
  -n, --namespace NAME    Kubernetes namespace (default: carrental)
  -e, --environment ENV   Environment (staging/production, default: staging)
  -c, --context CONTEXT   Kubectl context to use
  --skip-build           Skip building Docker images
  --dry-run              Show what would be deployed without applying
  --timeout SECONDS      Timeout for waiting operations (default: 300)

Environment Variables:
  ENVIRONMENT            Environment name (staging/production)
  KUBECTL_CONTEXT        Kubectl context to use
  SKIP_BUILD            Skip building Docker images (true/false)
  DRY_RUN               Dry run mode (true/false)
  TIMEOUT               Timeout in seconds

Examples:
  $0                                    # Deploy to staging
  $0 -e production -n carrental-prod   # Deploy to production
  $0 --skip-build                      # Deploy without building images
  $0 --dry-run                         # Show what would be deployed

EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -c|--context)
            KUBECTL_CONTEXT="$2"
            shift 2
            ;;
        --skip-build)
            SKIP_BUILD="true"
            shift
            ;;
        --dry-run)
            DRY_RUN="true"
            shift
            ;;
        --timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main deployment function
main() {
    print_status "Starting CarRental Kubernetes deployment..."
    print_status "Environment: $ENVIRONMENT"
    print_status "Namespace: $NAMESPACE"
    print_status "Dry run: $DRY_RUN"

    if [[ "$DRY_RUN" == "true" ]]; then
        print_warning "DRY RUN MODE - No changes will be applied"
        export KUBECTL_ARGS="--dry-run=client"
    fi

    check_prerequisites

    if [[ "$DRY_RUN" == "false" ]]; then
        build_and_push_images
        create_namespace
        deploy_infrastructure
        deploy_databases
        deploy_applications
        deploy_networking
        deploy_autoscaling
        deploy_monitoring
        verify_deployment
        show_deployment_info
    else
        print_status "DRY RUN: Would deploy all components"
    fi

    print_status "Deployment completed successfully!"
}

# Run main function
main "$@"
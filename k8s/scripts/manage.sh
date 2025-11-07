#!/bin/bash

# CarRental Kubernetes Management Script
# This script provides various management operations for the CarRental platform

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
NAMESPACE="${NAMESPACE:-carrental}"
KUBECTL_CONTEXT="${KUBECTL_CONTEXT:-}"

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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
    # Check if kubectl is installed
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
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
    fi

    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_error "Namespace $NAMESPACE does not exist"
        exit 1
    fi
}

# Function to show status
show_status() {
    print_step "CarRental Platform Status"
    echo "Namespace: $NAMESPACE"
    echo "=========================================="

    # Pods status
    echo ""
    print_status "Pods Status:"
    kubectl get pods -n "$NAMESPACE" -o wide

    # Services
    echo ""
    print_status "Services:"
    kubectl get services -n "$NAMESPACE"

    # Deployments
    echo ""
    print_status "Deployments:"
    kubectl get deployments -n "$NAMESPACE"

    # PVCs
    echo ""
    print_status "Persistent Volume Claims:"
    kubectl get pvc -n "$NAMESPACE"

    # Ingress
    echo ""
    print_status "Ingress:"
    kubectl get ingress -n "$NAMESPACE"

    # HPA
    echo ""
    print_status "Horizontal Pod Autoscaler:"
    kubectl get hpa -n "$NAMESPACE"
}

# Function to show detailed status
show_detailed_status() {
    print_step "Detailed Platform Status"

    # Check each component
    components=(
        "deployment/postgres-deployment:Database"
        "deployment/redis-deployment:Cache"
        "deployment/carrental-backend-deployment:Backend"
        "deployment/carrental-frontend-deployment:Frontend"
        "deployment/nginx-proxy-deployment:Proxy"
        "deployment/prometheus-deployment:Monitoring"
        "deployment/grafana-deployment:Grafana"
    )

    for component in "${components[@]}"; do
        IFS=':' read -r resource name <<< "$component"

        if kubectl get "$resource" -n "$NAMESPACE" &> /dev/null; then
            ready=$(kubectl get "$resource" -n "$NAMESPACE" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
            desired=$(kubectl get "$resource" -n "$NAMESPACE" -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")

            if [[ "$ready" == "$desired" && "$ready" != "0" ]]; then
                echo -e "${GREEN}✓${NC} $name: $ready/$desired pods ready"
            else
                echo -e "${RED}✗${NC} $name: $ready/$desired pods ready"
            fi
        else
            echo -e "${YELLOW}?${NC} $name: Not found"
        fi
    done

    # Health checks
    echo ""
    print_status "Health Checks:"

    # Backend health
    if kubectl get deployment carrental-backend-deployment -n "$NAMESPACE" &> /dev/null; then
        backend_ip=$(kubectl get service carrental-backend-service -n "$NAMESPACE" -o jsonpath='{.spec.clusterIP}' 2>/dev/null || echo "")
        if [[ -n "$backend_ip" ]]; then
            if kubectl run health-check-backend --image=curlimages/curl:latest --rm -i --restart=Never -- \
                curl -s -f "http://${backend_ip}:8081/actuator/health" >/dev/null 2>&1; then
                echo -e "${GREEN}✓${NC} Backend API: Healthy"
            else
                echo -e "${RED}✗${NC} Backend API: Unhealthy"
            fi
        fi
    fi

    # Database health
    if kubectl get deployment postgres-deployment -n "$NAMESPACE" &> /dev/null; then
        if kubectl exec -n "$NAMESPACE" deployment/postgres-deployment -- \
            pg_isready -U carrental -d carrental_db >/dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} PostgreSQL: Healthy"
        else
            echo -e "${RED}✗${NC} PostgreSQL: Unhealthy"
        fi
    fi

    # Redis health
    if kubectl get deployment redis-deployment -n "$NAMESPACE" &> /dev/null; then
        if kubectl exec -n "$NAMESPACE" deployment/redis-deployment -- \
            redis-cli ping >/dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} Redis: Healthy"
        else
            echo -e "${RED}✗${NC} Redis: Unhealthy"
        fi
    fi
}

# Function to show logs
show_logs() {
    local component="$1"
    local lines="${2:-100}"

    print_step "Showing logs for $component (last $lines lines)"

    case "$component" in
        backend|be)
            kubectl logs -n "$NAMESPACE" deployment/carrental-backend-deployment --tail="$lines" -f
            ;;
        frontend|fe)
            kubectl logs -n "$NAMESPACE" deployment/carrental-frontend-deployment --tail="$lines" -f
            ;;
        proxy|nginx)
            kubectl logs -n "$NAMESPACE" deployment/nginx-proxy-deployment --tail="$lines" -f
            ;;
        database|db|postgres)
            kubectl logs -n "$NAMESPACE" deployment/postgres-deployment --tail="$lines" -f
            ;;
        cache|redis)
            kubectl logs -n "$NAMESPACE" deployment/redis-deployment --tail="$lines" -f
            ;;
        prometheus|prom)
            kubectl logs -n "$NAMESPACE" deployment/prometheus-deployment --tail="$lines" -f
            ;;
        grafana)
            kubectl logs -n "$NAMESPACE" deployment/grafana-deployment --tail="$lines" -f
            ;;
        all)
            kubectl logs -n "$NAMESPACE" --all-containers=true --tail="$lines" -f
            ;;
        *)
            print_error "Unknown component: $component"
            echo "Available components: backend, frontend, proxy, database, cache, prometheus, grafana, all"
            exit 1
            ;;
    esac
}

# Function to scale components
scale_component() {
    local component="$1"
    local replicas="$2"

    print_step "Scaling $component to $replicas replicas"

    case "$component" in
        backend|be)
            kubectl scale deployment carrental-backend-deployment --replicas="$replicas" -n "$NAMESPACE"
            ;;
        frontend|fe)
            kubectl scale deployment carrental-frontend-deployment --replicas="$replicas" -n "$NAMESPACE"
            ;;
        proxy|nginx)
            kubectl scale deployment nginx-proxy-deployment --replicas="$replicas" -n "$NAMESPACE"
            ;;
        *)
            print_error "Cannot scale component: $component"
            echo "Scalable components: backend, frontend, proxy"
            exit 1
            ;;
    esac

    print_status "Scaled $component to $replicas replicas"
}

# Function to restart components
restart_component() {
    local component="$1"

    print_step "Restarting $component"

    case "$component" in
        backend|be)
            kubectl rollout restart deployment/carrental-backend-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/carrental-backend-deployment -n "$NAMESPACE"
            ;;
        frontend|fe)
            kubectl rollout restart deployment/carrental-frontend-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/carrental-frontend-deployment -n "$NAMESPACE"
            ;;
        proxy|nginx)
            kubectl rollout restart deployment/nginx-proxy-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/nginx-proxy-deployment -n "$NAMESPACE"
            ;;
        database|db|postgres)
            kubectl rollout restart deployment/postgres-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/postgres-deployment -n "$NAMESPACE"
            ;;
        cache|redis)
            kubectl rollout restart deployment/redis-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/redis-deployment -n "$NAMESPACE"
            ;;
        prometheus|prom)
            kubectl rollout restart deployment/prometheus-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/prometheus-deployment -n "$NAMESPACE"
            ;;
        grafana)
            kubectl rollout restart deployment/grafana-deployment -n "$NAMESPACE"
            kubectl rollout status deployment/grafana-deployment -n "$NAMESPACE"
            ;;
        all)
            kubectl rollout restart deployment --all -n "$NAMESPACE"
            ;;
        *)
            print_error "Unknown component: $component"
            echo "Available components: backend, frontend, proxy, database, cache, prometheus, grafana, all"
            exit 1
            ;;
    esac

    print_status "Restarted $component successfully"
}

# Function to connect to database
connect_database() {
    print_step "Connecting to PostgreSQL database"
    kubectl exec -it deployment/postgres-deployment -n "$NAMESPACE" -- \
        psql -U carrental -d carrental_db
}

# Function to connect to Redis
connect_redis() {
    print_step "Connecting to Redis"
    kubectl exec -it deployment/redis-deployment -n "$NAMESPACE" -- redis-cli
}

# Function to port forward services
port_forward() {
    local service="$1"
    local local_port="${2:-}"

    case "$service" in
        backend|be)
            local_port="${local_port:-8083}"
            print_status "Port forwarding backend service to localhost:$local_port"
            kubectl port-forward service/carrental-backend-service "$local_port":8083 -n "$NAMESPACE"
            ;;
        frontend|fe)
            local_port="${local_port:-3000}"
            print_status "Port forwarding frontend service to localhost:$local_port"
            kubectl port-forward service/carrental-frontend-service "$local_port":80 -n "$NAMESPACE"
            ;;
        database|db|postgres)
            local_port="${local_port:-5432}"
            print_status "Port forwarding PostgreSQL to localhost:$local_port"
            kubectl port-forward service/postgres-service "$local_port":5432 -n "$NAMESPACE"
            ;;
        redis)
            local_port="${local_port:-6379}"
            print_status "Port forwarding Redis to localhost:$local_port"
            kubectl port-forward service/redis-service "$local_port":6379 -n "$NAMESPACE"
            ;;
        prometheus|prom)
            local_port="${local_port:-9090}"
            print_status "Port forwarding Prometheus to localhost:$local_port"
            kubectl port-forward service/prometheus-service "$local_port":9090 -n "$NAMESPACE"
            ;;
        grafana)
            local_port="${local_port:-3000}"
            print_status "Port forwarding Grafana to localhost:$local_port"
            kubectl port-forward service/grafana-service "$local_port":3000 -n "$NAMESPACE"
            ;;
        *)
            print_error "Unknown service: $service"
            echo "Available services: backend, frontend, database, redis, prometheus, grafana"
            exit 1
            ;;
    esac
}

# Function to backup database
backup_database() {
    local backup_file="carrental-backup-$(date +%Y%m%d-%H%M%S).sql"

    print_step "Creating database backup: $backup_file"
    kubectl exec deployment/postgres-deployment -n "$NAMESPACE" -- \
        pg_dump -U carrental carrental_db > "$backup_file"

    print_status "Database backup created: $backup_file"
}

# Function to show help
show_help() {
    cat << EOF
CarRental Kubernetes Management Script

Usage: $0 <command> [options]

Commands:
  status                     Show platform status
  detailed-status           Show detailed status with health checks
  logs <component> [lines]  Show logs for component (default: 100 lines)
  scale <component> <num>   Scale component to specified replicas
  restart <component>       Restart component
  port-forward <service>    Port forward service to localhost
  db-connect               Connect to PostgreSQL database
  redis-connect            Connect to Redis
  backup                   Create database backup

Components:
  backend, frontend, proxy, database, cache, prometheus, grafana, all

Services:
  backend, frontend, database, redis, prometheus, grafana

Options:
  -n, --namespace NAME      Kubernetes namespace (default: carrental)
  -c, --context CONTEXT     Kubectl context to use
  -h, --help               Show this help message

Environment Variables:
  NAMESPACE                Kubernetes namespace
  KUBECTL_CONTEXT          Kubectl context to use

Examples:
  $0 status                           # Show platform status
  $0 logs backend 50                  # Show last 50 lines of backend logs
  $0 scale backend 5                  # Scale backend to 5 replicas
  $0 restart all                      # Restart all components
  $0 port-forward grafana 3001        # Forward Grafana to localhost:3001
  $0 db-connect                       # Connect to database
  $0 backup                           # Create database backup

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
        -c|--context)
            KUBECTL_CONTEXT="$2"
            shift 2
            ;;
        status)
            check_prerequisites
            show_status
            exit 0
            ;;
        detailed-status)
            check_prerequisites
            show_detailed_status
            exit 0
            ;;
        logs)
            check_prerequisites
            if [[ $# -lt 2 ]]; then
                print_error "Usage: $0 logs <component> [lines]"
                exit 1
            fi
            component="$2"
            lines="${3:-100}"
            show_logs "$component" "$lines"
            exit 0
            ;;
        scale)
            check_prerequisites
            if [[ $# -lt 3 ]]; then
                print_error "Usage: $0 scale <component> <replicas>"
                exit 1
            fi
            component="$2"
            replicas="$3"
            scale_component "$component" "$replicas"
            exit 0
            ;;
        restart)
            check_prerequisites
            if [[ $# -lt 2 ]]; then
                print_error "Usage: $0 restart <component>"
                exit 1
            fi
            component="$2"
            restart_component "$component"
            exit 0
            ;;
        port-forward)
            check_prerequisites
            if [[ $# -lt 2 ]]; then
                print_error "Usage: $0 port-forward <service> [local_port]"
                exit 1
            fi
            service="$2"
            local_port="${3:-}"
            port_forward "$service" "$local_port"
            exit 0
            ;;
        db-connect)
            check_prerequisites
            connect_database
            exit 0
            ;;
        redis-connect)
            check_prerequisites
            connect_redis
            exit 0
            ;;
        backup)
            check_prerequisites
            backup_database
            exit 0
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
done

# If no command provided, show help
show_help
#!/bin/bash

# CarRental Kubernetes Undeploy Script
# This script removes the CarRental SaaS platform from Kubernetes

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
FORCE="${FORCE:-false}"
PRESERVE_DATA="${PRESERVE_DATA:-true}"
DRY_RUN="${DRY_RUN:-false}"

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(dirname "$SCRIPT_DIR")"

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

# Function to confirm action
confirm_action() {
    if [[ "$FORCE" == "true" ]]; then
        return 0
    fi

    local message="$1"
    echo -e "${YELLOW}$message${NC}"
    read -p "Are you sure? (yes/no): " -r
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        print_status "Operation cancelled"
        exit 0
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."

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
        print_status "Using kubectl context: $KUBECTL_CONTEXT"
    fi

    # Check if namespace exists
    if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
        print_warning "Namespace $NAMESPACE does not exist"
        exit 0
    fi

    print_status "Prerequisites check passed"
}

# Function to backup data
backup_data() {
    if [[ "$PRESERVE_DATA" == "false" ]]; then
        print_warning "Skipping data backup (PRESERVE_DATA=false)"
        return 0
    fi

    print_step "Creating data backup..."

    local backup_dir="backup-$(date +%Y%m%d-%H%M%S)"
    mkdir -p "$backup_dir"

    # Backup database
    print_status "Backing up PostgreSQL database..."
    kubectl exec -n "$NAMESPACE" deployment/postgres-deployment -- \
        pg_dump -U carrental carrental_db > "$backup_dir/database.sql" || print_warning "Database backup failed"

    # Backup persistent volume data
    print_status "Backing up persistent volume claims..."
    kubectl get pvc -n "$NAMESPACE" -o yaml > "$backup_dir/pvcs.yaml"

    # Backup secrets and configmaps
    print_status "Backing up configuration..."
    kubectl get secrets -n "$NAMESPACE" -o yaml > "$backup_dir/secrets.yaml"
    kubectl get configmaps -n "$NAMESPACE" -o yaml > "$backup_dir/configmaps.yaml"

    print_status "Backup created in: $backup_dir"
}

# Function to scale down deployments
scale_down_deployments() {
    print_step "Scaling down deployments..."

    local deployments=(
        "carrental-backend-deployment"
        "carrental-frontend-deployment"
        "nginx-proxy-deployment"
        "prometheus-deployment"
        "grafana-deployment"
    )

    for deployment in "${deployments[@]}"; do
        if kubectl get deployment "$deployment" -n "$NAMESPACE" &> /dev/null; then
            print_status "Scaling down $deployment..."
            kubectl scale deployment "$deployment" --replicas=0 -n "$NAMESPACE"
        fi
    done

    # Wait for pods to terminate
    print_status "Waiting for pods to terminate..."
    kubectl wait --for=delete pod --all -n "$NAMESPACE" --timeout=120s || print_warning "Some pods may still be terminating"
}

# Function to remove monitoring
remove_monitoring() {
    print_step "Removing monitoring components..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove monitoring.yaml"
        return 0
    fi

    kubectl delete -f "$K8S_DIR/base/monitoring.yaml" --ignore-not-found=true
    print_status "Monitoring components removed"
}

# Function to remove autoscaling
remove_autoscaling() {
    print_step "Removing autoscaling components..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove hpa.yaml"
        return 0
    fi

    kubectl delete -f "$K8S_DIR/base/hpa.yaml" --ignore-not-found=true
    print_status "Autoscaling components removed"
}

# Function to remove networking
remove_networking() {
    print_step "Removing networking components..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove ingress.yaml"
        return 0
    fi

    kubectl delete -f "$K8S_DIR/base/ingress.yaml" --ignore-not-found=true
    print_status "Networking components removed"
}

# Function to remove applications
remove_applications() {
    print_step "Removing application services..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove application deployments"
        return 0
    fi

    # Remove backend
    kubectl delete -f "$K8S_DIR/base/backend-deployment.yaml" --ignore-not-found=true

    # Remove frontend
    kubectl delete -f "$K8S_DIR/base/frontend-deployment.yaml" --ignore-not-found=true

    print_status "Application services removed"
}

# Function to remove databases
remove_databases() {
    print_step "Removing database services..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove database deployments"
        return 0
    fi

    # Remove PostgreSQL
    kubectl delete -f "$K8S_DIR/base/postgres-deployment.yaml" --ignore-not-found=true

    # Remove Redis
    kubectl delete -f "$K8S_DIR/base/redis-deployment.yaml" --ignore-not-found=true

    print_status "Database services removed"
}

# Function to remove infrastructure
remove_infrastructure() {
    print_step "Removing infrastructure components..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove infrastructure components"
        return 0
    fi

    # Remove ConfigMaps (but preserve if PRESERVE_DATA=true)
    if [[ "$PRESERVE_DATA" == "false" ]]; then
        kubectl delete -f "$K8S_DIR/base/configmap.yaml" --ignore-not-found=true
        kubectl delete -f "$K8S_DIR/base/secrets.yaml" --ignore-not-found=true
    else
        print_warning "Preserving ConfigMaps and Secrets (PRESERVE_DATA=true)"
    fi

    print_status "Infrastructure components removed"
}

# Function to remove persistent volumes
remove_persistent_volumes() {
    print_step "Handling persistent volumes..."

    if [[ "$PRESERVE_DATA" == "true" ]]; then
        print_warning "Preserving persistent volumes and data (PRESERVE_DATA=true)"
        print_status "PVCs will be retained. To remove them manually later:"
        kubectl get pvc -n "$NAMESPACE" --no-headers | awk '{print "kubectl delete pvc " $1 " -n " ENVIRON["NAMESPACE"]}'
        return 0
    fi

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove persistent volumes"
        return 0
    fi

    confirm_action "This will permanently delete all persistent data!"

    # Remove PVCs
    kubectl delete pvc --all -n "$NAMESPACE"

    # Remove storage classes if they were created
    kubectl delete -f "$K8S_DIR/base/persistent-volumes.yaml" --ignore-not-found=true

    print_status "Persistent volumes removed"
}

# Function to remove namespace
remove_namespace() {
    print_step "Removing namespace..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would remove namespace $NAMESPACE"
        return 0
    fi

    confirm_action "This will remove the entire namespace '$NAMESPACE' and all its contents!"

    kubectl delete namespace "$NAMESPACE"
    print_status "Namespace $NAMESPACE removed"
}

# Function to cleanup cluster resources
cleanup_cluster_resources() {
    print_step "Cleaning up cluster-wide resources..."

    if [[ "$DRY_RUN" == "true" ]]; then
        print_status "DRY RUN: Would clean up cluster resources"
        return 0
    fi

    # Remove ClusterRoleBindings
    kubectl delete clusterrolebinding prometheus-cluster-role-binding --ignore-not-found=true

    # Remove ClusterRoles
    kubectl delete clusterrole prometheus-cluster-role --ignore-not-found=true

    # Remove ClusterIssuers
    kubectl delete clusterissuer letsencrypt-prod letsencrypt-staging --ignore-not-found=true

    print_status "Cluster resources cleaned up"
}

# Function to show removal summary
show_removal_summary() {
    print_step "Removal Summary"

    echo "=============================================="
    echo "  CarRental SaaS Platform Removal Complete"
    echo "=============================================="
    echo "Namespace: $NAMESPACE"
    echo "Data preservation: $PRESERVE_DATA"
    echo ""

    if [[ "$PRESERVE_DATA" == "true" ]]; then
        echo "Preserved resources:"
        echo "  - Persistent Volume Claims"
        echo "  - ConfigMaps and Secrets"
        echo "  - Backup data (if created)"
        echo ""
        echo "To completely remove all data:"
        echo "  PRESERVE_DATA=false $0"
    fi

    echo "=============================================="
}

# Function to show help
show_help() {
    cat << EOF
CarRental Kubernetes Undeploy Script

Usage: $0 [OPTIONS]

Options:
  -h, --help              Show this help message
  -n, --namespace NAME    Kubernetes namespace (default: carrental)
  -c, --context CONTEXT   Kubectl context to use
  --force                 Skip confirmation prompts
  --preserve-data         Preserve persistent data (default: true)
  --no-preserve-data      Remove all data including PVCs
  --dry-run               Show what would be removed without applying

Environment Variables:
  NAMESPACE              Kubernetes namespace
  KUBECTL_CONTEXT        Kubectl context to use
  FORCE                  Skip confirmations (true/false)
  PRESERVE_DATA          Preserve data (true/false)
  DRY_RUN               Dry run mode (true/false)

Examples:
  $0                                    # Remove with data preservation
  $0 --no-preserve-data --force        # Complete removal without prompts
  $0 --dry-run                         # Show what would be removed
  $0 -n carrental-prod                 # Remove from specific namespace

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
        --force)
            FORCE="true"
            shift
            ;;
        --preserve-data)
            PRESERVE_DATA="true"
            shift
            ;;
        --no-preserve-data)
            PRESERVE_DATA="false"
            shift
            ;;
        --dry-run)
            DRY_RUN="true"
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Main removal function
main() {
    print_status "Starting CarRental Kubernetes removal..."
    print_status "Namespace: $NAMESPACE"
    print_status "Preserve data: $PRESERVE_DATA"
    print_status "Dry run: $DRY_RUN"

    if [[ "$DRY_RUN" == "true" ]]; then
        print_warning "DRY RUN MODE - No changes will be applied"
    fi

    check_prerequisites

    if [[ "$DRY_RUN" == "false" && "$PRESERVE_DATA" == "true" ]]; then
        backup_data
    fi

    if [[ "$DRY_RUN" == "false" ]]; then
        scale_down_deployments
    fi

    remove_monitoring
    remove_autoscaling
    remove_networking
    remove_applications
    remove_databases
    remove_infrastructure
    remove_persistent_volumes
    cleanup_cluster_resources

    if [[ "$DRY_RUN" == "false" ]]; then
        remove_namespace
        show_removal_summary
    else
        print_status "DRY RUN: Would remove all components"
    fi

    print_status "Removal process completed!"
}

# Run main function
main "$@"
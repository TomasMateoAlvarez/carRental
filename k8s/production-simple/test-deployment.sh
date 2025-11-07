#!/bin/bash

# Test CarRental SaaS Production Deployment
# Comprehensive testing and verification script

set -e

# Configuration
NAMESPACE="carrental-prod"
DOMAIN=${1:-"carrental.yourdomain.com"}
API_DOMAIN="api.${DOMAIN#carrental.}"
MONITORING_DOMAIN="monitoring.${DOMAIN#carrental.}"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

# Test results
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0

log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((TESTS_PASSED++))
}

fail() {
    echo -e "${RED}❌ $1${NC}"
    ((TESTS_FAILED++))
}

warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

title() {
    echo -e "${BOLD}${BLUE}=== $1 ===${NC}"
    echo ""
}

run_test() {
    local test_name="$1"
    local test_command="$2"
    local expected_result="$3"

    ((TESTS_TOTAL++))
    log "Testing: $test_name"

    if eval "$test_command" &>/dev/null; then
        if [ -z "$expected_result" ] || eval "$expected_result" &>/dev/null; then
            success "$test_name"
            return 0
        else
            fail "$test_name - Expected condition not met"
            return 1
        fi
    else
        fail "$test_name - Command failed"
        return 1
    fi
}

test_cluster_connectivity() {
    title "CLUSTER CONNECTIVITY TESTS"

    run_test "Kubectl cluster access" "kubectl cluster-info"
    run_test "Namespace exists" "kubectl get namespace $NAMESPACE"
    run_test "All pods running" "[ \$(kubectl get pods -n $NAMESPACE --field-selector=status.phase!=Running --no-headers | wc -l) -eq 0 ]"
}

test_infrastructure() {
    title "INFRASTRUCTURE TESTS"

    # PostgreSQL tests
    run_test "PostgreSQL pod running" "kubectl get pod -l app.kubernetes.io/name=postgres -n $NAMESPACE -o jsonpath='{.items[0].status.phase}' | grep -q Running"
    run_test "PostgreSQL service accessible" "kubectl get service postgres-service -n $NAMESPACE"

    # Test database connectivity from backend
    log "Testing database connectivity from backend..."
    if kubectl exec deployment/carrental-backend-deployment -n $NAMESPACE -- nc -zv postgres-service 5432 &>/dev/null; then
        success "Database connectivity from backend"
        ((TESTS_PASSED++))
    else
        fail "Database connectivity from backend"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Storage tests
    run_test "PostgreSQL PVC bound" "kubectl get pvc postgres-pvc -n $NAMESPACE -o jsonpath='{.status.phase}' | grep -q Bound"
    run_test "Backup PVC bound" "kubectl get pvc backup-storage-pvc -n $NAMESPACE -o jsonpath='{.status.phase}' | grep -q Bound"
}

test_applications() {
    title "APPLICATION TESTS"

    # Backend tests
    run_test "Backend deployment available" "kubectl get deployment carrental-backend-deployment -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"
    run_test "Backend pods ready" "[ \$(kubectl get pods -l app.kubernetes.io/name=carrental-backend -n $NAMESPACE --field-selector=status.phase=Running --no-headers | wc -l) -ge 2 ]"

    # Test backend health endpoint
    log "Testing backend health endpoint..."
    if kubectl exec deployment/carrental-backend-deployment -n $NAMESPACE -- curl -f http://localhost:8081/actuator/health &>/dev/null; then
        success "Backend health endpoint"
        ((TESTS_PASSED++))
    else
        fail "Backend health endpoint"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Frontend tests
    run_test "Frontend deployment available" "kubectl get deployment carrental-frontend-deployment -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"
    run_test "Frontend pods ready" "[ \$(kubectl get pods -l app.kubernetes.io/name=carrental-frontend -n $NAMESPACE --field-selector=status.phase=Running --no-headers | wc -l) -ge 2 ]"

    # Test frontend health endpoint
    log "Testing frontend health endpoint..."
    if kubectl exec deployment/carrental-frontend-deployment -n $NAMESPACE -- curl -f http://localhost:80/health &>/dev/null; then
        success "Frontend health endpoint"
        ((TESTS_PASSED++))
    else
        fail "Frontend health endpoint"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

test_networking() {
    title "NETWORKING TESTS"

    # Ingress tests
    run_test "Ingress controller ready" "kubectl get deployment ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"
    run_test "CarRental ingress exists" "kubectl get ingress carrental-ingress -n $NAMESPACE"

    # Load balancer IP
    log "Checking Load Balancer IP..."
    LB_IP=$(kubectl get service ingress-nginx-controller -n ingress-nginx -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
    if [ ! -z "$LB_IP" ] && [ "$LB_IP" != "null" ]; then
        success "Load Balancer IP assigned: $LB_IP"
        ((TESTS_PASSED++))
    else
        fail "Load Balancer IP not assigned"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Services tests
    run_test "Backend service exists" "kubectl get service carrental-backend-service -n $NAMESPACE"
    run_test "Frontend service exists" "kubectl get service carrental-frontend-service -n $NAMESPACE"
}

test_ssl_certificates() {
    title "SSL CERTIFICATE TESTS"

    # cert-manager tests
    run_test "cert-manager running" "kubectl get deployment cert-manager -n cert-manager -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"
    run_test "Let's Encrypt issuer exists" "kubectl get issuer letsencrypt-prod -n $NAMESPACE"

    # Certificate tests
    if kubectl get certificate carrental-tls-secret -n $NAMESPACE &>/dev/null; then
        CERT_READY=$(kubectl get certificate carrental-tls-secret -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null)
        if [ "$CERT_READY" = "True" ]; then
            success "SSL certificate ready"
            ((TESTS_PASSED++))
        else
            warning "SSL certificate still processing (this is normal for new deployments)"
            ((TESTS_FAILED++))
        fi
    else
        fail "SSL certificate not found"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

test_monitoring() {
    title "MONITORING TESTS"

    # Prometheus tests
    run_test "Prometheus deployment available" "kubectl get deployment prometheus-deployment -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"

    # Test Prometheus metrics endpoint
    log "Testing Prometheus metrics..."
    if kubectl exec deployment/prometheus-deployment -n $NAMESPACE -- curl -f http://localhost:9090/-/healthy &>/dev/null; then
        success "Prometheus healthy"
        ((TESTS_PASSED++))
    else
        fail "Prometheus not healthy"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Grafana tests
    run_test "Grafana deployment available" "kubectl get deployment grafana-deployment -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"

    # Test Grafana health endpoint
    log "Testing Grafana health..."
    if kubectl exec deployment/grafana-deployment -n $NAMESPACE -- curl -f http://localhost:3000/api/health &>/dev/null; then
        success "Grafana healthy"
        ((TESTS_PASSED++))
    else
        fail "Grafana not healthy"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

test_backup_system() {
    title "BACKUP SYSTEM TESTS"

    # Backup cronjob
    run_test "Backup cronjob exists" "kubectl get cronjob carrental-backup-daily -n $NAMESPACE"
    run_test "Backup cleanup cronjob exists" "kubectl get cronjob carrental-backup-cleanup -n $NAMESPACE"

    # Backup health monitor
    run_test "Backup health monitor running" "kubectl get deployment backup-health-monitor -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type==\"Available\")].status}' | grep -q True"
}

test_autoscaling() {
    title "AUTOSCALING TESTS"

    # HPA tests
    run_test "Backend HPA exists" "kubectl get hpa carrental-backend-hpa -n $NAMESPACE"
    run_test "Frontend HPA exists" "kubectl get hpa carrental-frontend-hpa -n $NAMESPACE"

    # Check HPA status
    log "Checking HPA status..."
    BACKEND_HPA_READY=$(kubectl get hpa carrental-backend-hpa -n $NAMESPACE -o jsonpath='{.status.conditions[?(@.type=="AbleToScale")].status}' 2>/dev/null)
    if [ "$BACKEND_HPA_READY" = "True" ]; then
        success "Backend HPA ready to scale"
        ((TESTS_PASSED++))
    else
        warning "Backend HPA not ready (metrics may need time to populate)"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

test_external_connectivity() {
    title "EXTERNAL CONNECTIVITY TESTS"

    if [ "$DOMAIN" != "carrental.yourdomain.com" ]; then
        # Test external domain connectivity
        log "Testing external domain connectivity..."

        # Frontend
        if curl -f -s -I "https://$DOMAIN" &>/dev/null; then
            success "Frontend externally accessible via HTTPS"
            ((TESTS_PASSED++))
        else
            warning "Frontend not externally accessible (DNS may not be configured)"
            ((TESTS_FAILED++))
        fi
        ((TESTS_TOTAL++))

        # API
        if curl -f -s -I "https://$API_DOMAIN" &>/dev/null; then
            success "API externally accessible via HTTPS"
            ((TESTS_PASSED++))
        else
            warning "API not externally accessible (DNS may not be configured)"
            ((TESTS_FAILED++))
        fi
        ((TESTS_TOTAL++))

        # Monitoring
        if curl -f -s -I "https://$MONITORING_DOMAIN" &>/dev/null; then
            success "Monitoring externally accessible via HTTPS"
            ((TESTS_PASSED++))
        else
            warning "Monitoring not externally accessible (DNS may not be configured)"
            ((TESTS_FAILED++))
        fi
        ((TESTS_TOTAL++))
    else
        warning "Skipping external connectivity tests (using default domain)"
        warning "Update domain in ingress.yaml and configure DNS to test external access"
    fi
}

show_resource_usage() {
    title "RESOURCE USAGE"

    echo "Pod Resource Usage:"
    kubectl top pods -n $NAMESPACE 2>/dev/null || echo "Metrics not available yet"

    echo ""
    echo "Node Resource Usage:"
    kubectl top nodes 2>/dev/null || echo "Metrics not available yet"

    echo ""
    echo "Storage Usage:"
    kubectl get pvc -n $NAMESPACE
}

show_test_summary() {
    title "TEST SUMMARY"

    echo "Total Tests: $TESTS_TOTAL"
    echo "Passed: $TESTS_PASSED"
    echo "Failed: $TESTS_FAILED"
    echo ""

    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}🎉 ALL TESTS PASSED! Your CarRental SaaS deployment is healthy!${NC}"
        echo ""
        echo -e "${BOLD}Access URLs:${NC}"
        echo "   🌐 Frontend: https://$DOMAIN"
        echo "   🔧 API: https://$API_DOMAIN"
        echo "   📊 Monitoring: https://$MONITORING_DOMAIN"
        return 0
    else
        echo -e "${RED}⚠️ Some tests failed. Check the output above for details.${NC}"
        echo ""
        echo "Common issues:"
        echo "   - DNS not configured (external connectivity tests)"
        echo "   - SSL certificates still processing (wait a few minutes)"
        echo "   - Metrics not available yet (HPA tests)"
        echo ""
        return 1
    fi
}

show_usage() {
    echo "Usage: $0 [DOMAIN]"
    echo ""
    echo "Test CarRental SaaS production deployment"
    echo ""
    echo "Parameters:"
    echo "  DOMAIN    Your domain (default: carrental.yourdomain.com)"
    echo ""
    echo "Examples:"
    echo "  $0                              # Test with default domain"
    echo "  $0 carrental.example.com        # Test with custom domain"
}

main() {
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi

    echo -e "${BOLD}${BLUE}🧪 CARRENTAL SAAS - PRODUCTION DEPLOYMENT TESTS${NC}"
    echo -e "${BOLD}${BLUE}==================================================${NC}"
    echo ""
    echo "Testing domain: $DOMAIN"
    echo "API domain: $API_DOMAIN"
    echo "Monitoring domain: $MONITORING_DOMAIN"
    echo ""

    test_cluster_connectivity
    test_infrastructure
    test_applications
    test_networking
    test_ssl_certificates
    test_monitoring
    test_backup_system
    test_autoscaling
    test_external_connectivity

    echo ""
    show_resource_usage
    echo ""
    show_test_summary
}

main "$@"
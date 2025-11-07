#!/bin/bash

# Test CarRental Free Deployment Integration
# Verifica que Supabase + Render + Vercel estén funcionando correctamente

set -e

# Configuration
FRONTEND_URL=""
BACKEND_URL=""
SUPABASE_PROJECT_ID=""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')] $1${NC}"
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
    local expected_status="$3"

    ((TESTS_TOTAL++))
    log "Testing: $test_name"

    local status_code=$(eval "$test_command" 2>/dev/null | tail -n1)

    if [ "$status_code" = "$expected_status" ]; then
        success "$test_name"
        return 0
    else
        fail "$test_name (Status: $status_code, Expected: $expected_status)"
        return 1
    fi
}

prompt_urls() {
    title "CONFIGURACIÓN DE URLs"

    echo "Por favor, proporciona las URLs de tu deployment:"
    echo ""

    read -p "🌐 Frontend URL (Vercel): " FRONTEND_URL
    read -p "🚀 Backend URL (Render): " BACKEND_URL
    read -p "🗄️ Supabase Project ID: " SUPABASE_PROJECT_ID

    # Remove trailing slashes
    FRONTEND_URL=${FRONTEND_URL%/}
    BACKEND_URL=${BACKEND_URL%/}

    echo ""
    echo "URLs configuradas:"
    echo "  Frontend: $FRONTEND_URL"
    echo "  Backend: $BACKEND_URL"
    echo "  Database: db.${SUPABASE_PROJECT_ID}.supabase.co"
    echo ""
}

test_supabase() {
    title "TESTING SUPABASE DATABASE"

    # Test Supabase connectivity (public API health)
    run_test "Supabase project accessibility" \
             "curl -s -w '%{http_code}' -o /dev/null https://${SUPABASE_PROJECT_ID}.supabase.co" \
             "200"

    # Test database port (this might timeout but shows reachability)
    log "Testing database port connectivity..."
    if timeout 5 nc -z db.${SUPABASE_PROJECT_ID}.supabase.co 5432 2>/dev/null; then
        success "Database port 5432 reachable"
        ((TESTS_PASSED++))
    else
        warning "Database port test (expected for external access)"
        # Don't count as failed since external DB port access is often blocked
    fi
    ((TESTS_TOTAL++))
}

test_render_backend() {
    title "TESTING RENDER BACKEND"

    # Test backend health endpoint
    run_test "Backend health check" \
             "curl -s -w '%{http_code}' -o /dev/null $BACKEND_URL/actuator/health" \
             "200"

    # Test specific API endpoints
    run_test "Vehicles API endpoint" \
             "curl -s -w '%{http_code}' -o /dev/null $BACKEND_URL/api/v1/vehicles" \
             "200"

    # Test CORS preflight for frontend
    run_test "CORS preflight check" \
             "curl -s -w '%{http_code}' -o /dev/null -X OPTIONS -H 'Origin: $FRONTEND_URL' -H 'Access-Control-Request-Method: GET' $BACKEND_URL/api/v1/vehicles" \
             "200"
}

test_vercel_frontend() {
    title "TESTING VERCEL FRONTEND"

    # Test frontend accessibility
    run_test "Frontend accessibility" \
             "curl -s -w '%{http_code}' -o /dev/null $FRONTEND_URL" \
             "200"

    # Test static assets
    run_test "Frontend assets loading" \
             "curl -s -w '%{http_code}' -o /dev/null $FRONTEND_URL/vite.svg" \
             "200"

    # Test SPA routing (should return 200 for any route)
    run_test "SPA routing (/dashboard)" \
             "curl -s -w '%{http_code}' -o /dev/null $FRONTEND_URL/dashboard" \
             "200"
}

test_integration() {
    title "TESTING FULL INTEGRATION"

    # Test API response format
    log "Testing API response format..."
    local api_response=$(curl -s $BACKEND_URL/api/v1/vehicles)
    if echo "$api_response" | grep -q '\['; then
        success "API returns valid JSON array"
        ((TESTS_PASSED++))
    else
        fail "API response format invalid"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Test authentication endpoint
    run_test "Authentication endpoint" \
             "curl -s -w '%{http_code}' -o /dev/null -X POST $BACKEND_URL/api/v1/auth/login" \
             "400"  # Expected 400 for missing credentials

    # Test environment variables integration
    log "Testing environment integration..."
    local frontend_source=$(curl -s $FRONTEND_URL)
    if echo "$frontend_source" | grep -q "VITE_API_BASE_URL" || echo "$frontend_source" | grep -q "$BACKEND_URL"; then
        warning "Environment variables may be exposed (check build config)"
    else
        success "Environment variables properly handled"
        ((TESTS_PASSED++))
    fi
    ((TESTS_TOTAL++))
}

test_ssl_certificates() {
    title "TESTING SSL/TLS CERTIFICATES"

    # Test frontend SSL
    log "Testing frontend SSL certificate..."
    if openssl s_client -connect ${FRONTEND_URL#https://}:443 -servername ${FRONTEND_URL#https://} < /dev/null 2>/dev/null | grep -q "Verify return code: 0"; then
        success "Frontend SSL certificate valid"
        ((TESTS_PASSED++))
    else
        fail "Frontend SSL certificate issues"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Test backend SSL
    log "Testing backend SSL certificate..."
    if openssl s_client -connect ${BACKEND_URL#https://}:443 -servername ${BACKEND_URL#https://} < /dev/null 2>/dev/null | grep -q "Verify return code: 0"; then
        success "Backend SSL certificate valid"
        ((TESTS_PASSED++))
    else
        fail "Backend SSL certificate issues"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

test_performance() {
    title "TESTING PERFORMANCE"

    # Test frontend load time
    log "Testing frontend load time..."
    local frontend_time=$(curl -s -w '%{time_total}' -o /dev/null $FRONTEND_URL)
    if (( $(echo "$frontend_time < 3.0" | bc -l) )); then
        success "Frontend loads in ${frontend_time}s (< 3s)"
        ((TESTS_PASSED++))
    else
        warning "Frontend slow: ${frontend_time}s (may be normal for first load)"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))

    # Test backend response time
    log "Testing backend response time..."
    local backend_time=$(curl -s -w '%{time_total}' -o /dev/null $BACKEND_URL/actuator/health)
    if (( $(echo "$backend_time < 5.0" | bc -l) )); then
        success "Backend responds in ${backend_time}s (< 5s)"
        ((TESTS_PASSED++))
    else
        warning "Backend slow: ${backend_time}s (may be waking up from sleep)"
        ((TESTS_FAILED++))
    fi
    ((TESTS_TOTAL++))
}

show_deployment_info() {
    title "DEPLOYMENT INFORMATION"

    echo "🌍 Access URLs:"
    echo "   Frontend: $FRONTEND_URL"
    echo "   Backend: $BACKEND_URL"
    echo "   Supabase: https://supabase.com/dashboard/project/$SUPABASE_PROJECT_ID"
    echo ""

    echo "🔐 Demo Credentials:"
    echo "   Admin: admin / admin123"
    echo "   Demo: demo / demo123"
    echo ""

    echo "📊 API Endpoints to Test Manually:"
    echo "   Health: $BACKEND_URL/actuator/health"
    echo "   Vehicles: $BACKEND_URL/api/v1/vehicles"
    echo "   Login: $BACKEND_URL/api/v1/auth/login"
    echo ""

    echo "🧪 Manual Testing Steps:"
    echo "   1. Open $FRONTEND_URL"
    echo "   2. Login with admin/admin123"
    echo "   3. Check dashboard loads data"
    echo "   4. Try creating a reservation"
    echo "   5. Verify data persists after refresh"
}

show_troubleshooting() {
    title "COMMON ISSUES & SOLUTIONS"

    echo "🔧 CORS Errors:"
    echo "   - Verify FRONTEND_URL in Render environment variables"
    echo "   - Check browser console for specific error"
    echo ""

    echo "🔧 Backend Sleep (First Load):"
    echo "   - Render free tier sleeps after 15min inactivity"
    echo "   - First request may take 30-60 seconds"
    echo "   - Implement auto-wake ping in frontend"
    echo ""

    echo "🔧 Database Connection:"
    echo "   - Verify DATABASE_URL in Render environment"
    echo "   - Check Supabase project is active"
    echo "   - Confirm connection pooling (max 2 for free tier)"
    echo ""

    echo "🔧 Environment Variables:"
    echo "   - Redeploy after changing environment variables"
    echo "   - Check build logs in Vercel/Render dashboards"
    echo "   - Verify VITE_ prefix for frontend variables"
}

show_test_summary() {
    title "TEST SUMMARY"

    echo "Total Tests: $TESTS_TOTAL"
    echo "Passed: $TESTS_PASSED"
    echo "Failed: $TESTS_FAILED"
    echo ""

    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}🎉 ALL TESTS PASSED! Your CarRental deployment is working perfectly!${NC}"
        echo ""
        echo "🚀 Your app is ready for demos and production use!"
        return 0
    elif [ $TESTS_FAILED -le 2 ]; then
        echo -e "${YELLOW}⚠️ Minor issues detected. Your deployment is mostly functional.${NC}"
        echo ""
        echo "💡 Check the troubleshooting section above for common fixes."
        return 1
    else
        echo -e "${RED}❌ Multiple issues detected. Please review the configuration.${NC}"
        echo ""
        echo "🛠️ Check environment variables and service status."
        return 1
    fi
}

main() {
    echo -e "${BOLD}${BLUE}🧪 CARRENTAL FREE DEPLOYMENT INTEGRATION TESTS${NC}"
    echo -e "${BOLD}${BLUE}================================================${NC}"
    echo ""

    # Check dependencies
    if ! command -v curl &> /dev/null; then
        echo "❌ curl is required but not installed"
        exit 1
    fi

    if ! command -v nc &> /dev/null; then
        echo "⚠️ netcat (nc) not found - some network tests will be skipped"
    fi

    if ! command -v bc &> /dev/null; then
        echo "⚠️ bc not found - performance timing tests will be skipped"
    fi

    prompt_urls

    test_supabase
    test_render_backend
    test_vercel_frontend
    test_integration
    test_ssl_certificates

    if command -v bc &> /dev/null; then
        test_performance
    fi

    echo ""
    show_deployment_info
    echo ""
    show_troubleshooting
    echo ""
    show_test_summary
}

main "$@"
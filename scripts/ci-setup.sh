#!/bin/bash

# CarRental SaaS - CI/CD Setup Script
# This script sets up the CI/CD environment and validates configuration

set -e

echo "🚀 Setting up CarRental SaaS CI/CD Environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Check required tools
check_dependencies() {
    print_status "Checking required dependencies..."

    local missing_deps=()

    if ! command -v git &> /dev/null; then
        missing_deps+=("git")
    fi

    if ! command -v docker &> /dev/null; then
        missing_deps+=("docker")
    fi

    if ! command -v gh &> /dev/null; then
        missing_deps+=("gh (GitHub CLI)")
    fi

    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing dependencies: ${missing_deps[*]}"
        exit 1
    fi

    print_success "All dependencies are available"
}

# Validate GitHub repository
validate_repository() {
    print_status "Validating GitHub repository..."

    if ! git remote get-url origin &> /dev/null; then
        print_error "Not in a Git repository or no origin remote found"
        exit 1
    fi

    local repo_url=$(git remote get-url origin)
    print_status "Repository: $repo_url"

    if ! gh auth status &> /dev/null; then
        print_warning "GitHub CLI not authenticated. Run: gh auth login"
    fi

    print_success "Repository validation complete"
}

# Setup GitHub Actions secrets
setup_secrets() {
    print_status "Setting up GitHub Actions secrets..."

    # Check if secrets exist
    local secrets_to_check=(
        "GHCR_TOKEN"
        "CODECOV_TOKEN"
        "SLACK_WEBHOOK_URL"
    )

    for secret in "${secrets_to_check[@]}"; do
        if gh secret list | grep -q "$secret"; then
            print_success "Secret $secret already exists"
        else
            print_warning "Secret $secret not found. Please set it manually:"
            echo "  gh secret set $secret"
        fi
    done
}

# Setup environments
setup_environments() {
    print_status "Setting up deployment environments..."

    # Check if environments exist
    if gh api repos/:owner/:repo/environments/staging &> /dev/null; then
        print_success "Staging environment already exists"
    else
        print_warning "Staging environment not found. Creating..."
        gh api repos/:owner/:repo/environments/staging --method PUT
    fi

    if gh api repos/:owner/:repo/environments/production &> /dev/null; then
        print_success "Production environment already exists"
    else
        print_warning "Production environment not found. Creating..."
        gh api repos/:owner/:repo/environments/production --method PUT
    fi
}

# Validate workflow files
validate_workflows() {
    print_status "Validating workflow files..."

    local workflow_dir=".github/workflows"

    if [ ! -d "$workflow_dir" ]; then
        print_error "Workflows directory not found: $workflow_dir"
        exit 1
    fi

    local workflows=(
        "ci-cd-enterprise.yml"
        "dependabot-auto-merge.yml"
    )

    for workflow in "${workflows[@]}"; do
        if [ -f "$workflow_dir/$workflow" ]; then
            print_success "Workflow found: $workflow"
        else
            print_error "Workflow not found: $workflow"
            exit 1
        fi
    done

    # Validate YAML syntax
    for workflow_file in "$workflow_dir"/*.yml; do
        if command -v yamllint &> /dev/null; then
            if yamllint "$workflow_file" &> /dev/null; then
                print_success "YAML syntax valid: $(basename "$workflow_file")"
            else
                print_error "YAML syntax error in: $(basename "$workflow_file")"
                exit 1
            fi
        fi
    done
}

# Setup branch protection
setup_branch_protection() {
    print_status "Setting up branch protection rules..."

    # Main branch protection
    print_status "Configuring main branch protection..."
    gh api repos/:owner/:repo/branches/main/protection \
        --method PUT \
        --field required_status_checks='{"strict":true,"contexts":["Code Quality & Security","Backend Tests","Frontend Tests"]}' \
        --field enforce_admins=true \
        --field required_pull_request_reviews='{"required_approving_review_count":2,"require_code_owner_reviews":true}' \
        --field restrictions=null \
        2>/dev/null && print_success "Main branch protection configured" || print_warning "Could not configure main branch protection"

    # Develop branch protection
    print_status "Configuring develop branch protection..."
    gh api repos/:owner/:repo/branches/develop/protection \
        --method PUT \
        --field required_status_checks='{"strict":true,"contexts":["Code Quality & Security","Backend Tests","Frontend Tests"]}' \
        --field enforce_admins=false \
        --field required_pull_request_reviews='{"required_approving_review_count":1}' \
        --field restrictions=null \
        2>/dev/null && print_success "Develop branch protection configured" || print_warning "Could not configure develop branch protection"
}

# Test CI/CD pipeline
test_pipeline() {
    print_status "Testing CI/CD pipeline..."

    # Check if there are any recent workflow runs
    local recent_runs=$(gh run list --limit 5 --json status,conclusion,name)

    if [ -n "$recent_runs" ]; then
        print_success "Recent workflow runs found"
        echo "$recent_runs" | jq -r '.[] | "\(.name): \(.status) (\(.conclusion))"'
    else
        print_warning "No recent workflow runs found"
    fi

    # Validate Docker setup
    if docker info &> /dev/null; then
        print_success "Docker is running"
    else
        print_warning "Docker is not running - required for local testing"
    fi
}

# Generate CI/CD report
generate_report() {
    print_status "Generating CI/CD setup report..."

    local report_file="ci-cd-setup-report.md"

    cat > "$report_file" << EOF
# CarRental SaaS - CI/CD Setup Report

Generated on: $(date)

## Repository Information
- Repository: $(git remote get-url origin)
- Current Branch: $(git branch --show-current)
- Last Commit: $(git log -1 --pretty=format:"%h - %s (%cr)")

## GitHub Actions Status
$(gh run list --limit 5 --json status,conclusion,name,createdAt | jq -r '.[] | "- \(.name): \(.status) (\(.conclusion)) - \(.createdAt)"')

## Secrets Status
$(gh secret list | sed 's/^/- /')

## Environment Status
- Staging: $(gh api repos/:owner/:repo/environments/staging &> /dev/null && echo "✅ Configured" || echo "❌ Not configured")
- Production: $(gh api repos/:owner/:repo/environments/production &> /dev/null && echo "✅ Configured" || echo "❌ Not configured")

## Branch Protection
- Main: $(gh api repos/:owner/:repo/branches/main/protection &> /dev/null && echo "✅ Protected" || echo "❌ Not protected")
- Develop: $(gh api repos/:owner/:repo/branches/develop/protection &> /dev/null && echo "✅ Protected" || echo "❌ Not protected")

## Next Steps
1. Review and update secrets if needed
2. Test pipeline with a sample commit
3. Configure monitoring and alerts
4. Train team on CI/CD processes

EOF

    print_success "Report generated: $report_file"
}

# Main execution
main() {
    echo "=========================================="
    echo "  CarRental SaaS CI/CD Setup"
    echo "=========================================="
    echo ""

    check_dependencies
    validate_repository
    validate_workflows
    setup_secrets
    setup_environments
    setup_branch_protection
    test_pipeline
    generate_report

    echo ""
    print_success "🎉 CI/CD setup completed successfully!"
    echo ""
    echo "📋 Next steps:"
    echo "1. Review the generated report: ci-cd-setup-report.md"
    echo "2. Set any missing secrets: gh secret set SECRET_NAME"
    echo "3. Test the pipeline: git push origin develop"
    echo "4. Monitor first workflow run in GitHub Actions"
    echo ""
    echo "📚 Documentation:"
    echo "- CI/CD Guide: CI-CD-GUIDE.md"
    echo "- Deployment Guide: DEPLOYMENT.md"
    echo ""
}

# Execute main function
main "$@"
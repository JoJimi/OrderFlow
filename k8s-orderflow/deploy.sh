#!/bin/bash

# =====================================================
# OrderFlow Kubernetes Deployment Script
# =====================================================
# This script deploys OrderFlow microservices to Minikube
#
# Prerequisites:
# - Minikube installed and running
# - kubectl configured
# - ArgoCD installed (optional, for GitOps)
# =====================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "\n${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    # Check kubectl
    if command -v kubectl &> /dev/null; then
        print_success "kubectl is installed"
    else
        print_error "kubectl is not installed"
        exit 1
    fi

    # Check minikube
    if command -v minikube &> /dev/null; then
        print_success "minikube is installed"

        # Check if minikube is running
        if minikube status | grep -q "Running"; then
            print_success "minikube is running"
        else
            print_warning "minikube is not running. Starting..."
            minikube start --cpus=4 --memory=8192
        fi
    else
        print_error "minikube is not installed"
        exit 1
    fi
}

# Enable required addons
enable_addons() {
    print_header "Enabling Minikube Addons"

    echo "Enabling ingress addon..."
    minikube addons enable ingress
    print_success "Ingress addon enabled"

    echo "Enabling metrics-server addon..."
    minikube addons enable metrics-server
    print_success "Metrics-server addon enabled"
}

# Deploy using kubectl (without ArgoCD)
deploy_direct() {
    print_header "Deploying OrderFlow Services"

    # Get script directory
    SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

    echo "Step 1: Creating namespace..."
    kubectl apply -f "$SCRIPT_DIR/base/namespace.yaml"
    print_success "Namespace created"

    echo "Step 2: Creating secrets and configmap..."
    kubectl apply -f "$SCRIPT_DIR/base/secrets.yaml"
    kubectl apply -f "$SCRIPT_DIR/base/configmap.yaml"
    print_success "Secrets and ConfigMap created"

    echo "Step 3: Deploying infrastructure services..."
    kubectl apply -f "$SCRIPT_DIR/infrastructure/postgres.yaml"
    kubectl apply -f "$SCRIPT_DIR/infrastructure/redis.yaml"
    kubectl apply -f "$SCRIPT_DIR/infrastructure/kafka.yaml"
    print_success "Infrastructure services deployed"

    echo "Step 4: Waiting for infrastructure to be ready..."
    echo "Waiting for PostgreSQL..."
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s
    print_success "PostgreSQL is ready"

    echo "Waiting for Redis..."
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s
    print_success "Redis is ready"

    echo "Waiting for Kafka..."
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s
    print_success "Kafka is ready"

    echo "Step 5: Deploying application services..."
    kubectl apply -f "$SCRIPT_DIR/services/user-service.yaml"
    kubectl apply -f "$SCRIPT_DIR/services/product-service.yaml"
    kubectl apply -f "$SCRIPT_DIR/services/inventory-service.yaml"
    print_success "Application services deployed"

    echo "Step 6: Creating Ingress..."
    kubectl apply -f "$SCRIPT_DIR/base/ingress.yaml"
    print_success "Ingress created"

    echo "Step 7: Waiting for application services to be ready..."
    echo "This may take a few minutes..."

    # Wait with timeout
    kubectl wait --for=condition=ready pod -l app=user-service -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=product-service -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=inventory-service -n orderflow --timeout=300s || true
}

# Deploy using Kustomize
deploy_kustomize() {
    print_header "Deploying OrderFlow Services with Kustomize"

    SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

    echo "Applying all resources with kustomize..."
    kubectl apply -k "$SCRIPT_DIR"

    echo "Waiting for all pods to be ready..."
    sleep 30

    # Wait for infrastructure first
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s || true
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s || true

    # Wait for services
    kubectl wait --for=condition=ready pod -l app=user-service -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=product-service -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=inventory-service -n orderflow --timeout=300s || true
}

# Show status
show_status() {
    print_header "Deployment Status"

    echo -e "\n${YELLOW}Pods:${NC}"
    kubectl get pods -n orderflow -o wide

    echo -e "\n${YELLOW}Services:${NC}"
    kubectl get svc -n orderflow

    echo -e "\n${YELLOW}Ingress:${NC}"
    kubectl get ingress -n orderflow

    # Get Minikube IP
    MINIKUBE_IP=$(minikube ip)

    print_header "Access Information"
    echo -e "${GREEN}Minikube IP: ${MINIKUBE_IP}${NC}"
    echo ""
    echo "NodePort Access (direct):"
    echo "  - User Service:      http://${MINIKUBE_IP}:30081"
    echo "  - Product Service:   http://${MINIKUBE_IP}:30082"
    echo "  - Inventory Service: http://${MINIKUBE_IP}:30085"
    echo ""
    echo "Ingress Access (via nginx):"
    echo "  Add this to your /etc/hosts (or C:\\Windows\\System32\\drivers\\etc\\hosts):"
    echo "  ${MINIKUBE_IP} orderflow.local"
    echo ""
    echo "  Then access via:"
    echo "  - User API:      http://orderflow.local/api/users"
    echo "  - Auth API:      http://orderflow.local/api/auth"
    echo "  - Product API:   http://orderflow.local/api/products"
    echo "  - Inventory API: http://orderflow.local/api/inventory"
    echo ""
    echo "Health Check Endpoints:"
    echo "  - User Service:      http://${MINIKUBE_IP}:30081/actuator/health"
    echo "  - Product Service:   http://${MINIKUBE_IP}:30082/actuator/health"
    echo "  - Inventory Service: http://${MINIKUBE_IP}:30085/actuator/health"
}

# Clean up
cleanup() {
    print_header "Cleaning Up"

    echo "Deleting orderflow namespace and all resources..."
    kubectl delete namespace orderflow --ignore-not-found=true

    print_success "Cleanup completed"
}

# View logs
view_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo "Usage: $0 logs <service-name>"
        echo "Available services: user-service, product-service, inventory-service, postgres, redis, kafka"
        exit 1
    fi

    kubectl logs -f -l app=$service -n orderflow --all-containers=true
}

# Main
main() {
    case "${1:-deploy}" in
        deploy)
            check_prerequisites
            enable_addons
            deploy_direct
            show_status
            ;;
        kustomize)
            check_prerequisites
            enable_addons
            deploy_kustomize
            show_status
            ;;
        status)
            show_status
            ;;
        cleanup)
            cleanup
            ;;
        logs)
            view_logs "$2"
            ;;
        *)
            echo "Usage: $0 {deploy|kustomize|status|cleanup|logs <service>}"
            echo ""
            echo "Commands:"
            echo "  deploy    - Deploy services step by step"
            echo "  kustomize - Deploy using kustomize"
            echo "  status    - Show deployment status"
            echo "  cleanup   - Remove all resources"
            echo "  logs      - View logs for a service"
            exit 1
            ;;
    esac
}

main "$@"
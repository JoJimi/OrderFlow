#!/bin/bash

# =====================================================
# OrderFlow ALG Stack Deployment Script (Bash)
# =====================================================
# Deploy Alloy, Loki, and Grafana to Kubernetes
# =====================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_header() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

show_alg_status() {
    print_header "ALG Stack Deployment Status"

    echo -e "\n${YELLOW}📦 ALG Pods:${NC}"
    kubectl get pods -n orderflow -l "part-of=alg-monitoring" -o wide || true

    echo -e "\n${YELLOW}🌐 ALG Services:${NC}"
    kubectl get svc -n orderflow -l "part-of=alg-monitoring" || true

    echo -e "\n${YELLOW}💾 PVC Status:${NC}"
    kubectl get pvc -n orderflow -l "part-of=alg-monitoring" || true

    MINIKUBE_IP=$(minikube ip 2>/dev/null || echo "localhost")

    print_header "📊 ALG Stack Access"
    echo -e "${GREEN}Grafana:  http://${MINIKUBE_IP}:30301${NC}"
    echo -e "${GREEN}Loki:     http://${MINIKUBE_IP}:30300${NC}"
    echo -e "${GREEN}Alloy:    http://${MINIKUBE_IP}:30345/metrics${NC}"
    echo ""
    echo -e "${BLUE}Grafana Login:${NC}"
    echo "  Username: admin"
    echo "  Password: admin123"
}

deploy_alg_kustomize() {
    print_header "Deploying ALG Stack with Kustomize"

    if [ ! -f "monitoring/alg/kustomization.yaml" ]; then
        print_error "monitoring/alg/kustomization.yaml not found!"
        exit 1
    fi

    kubectl apply -k monitoring/alg
    print_success "ALG Stack resources applied"

    echo "⏳ Waiting for services to be ready..."
    sleep 30

    # Wait for Loki
    echo "⏳ Waiting for Loki to be ready..."
    kubectl wait --for=condition=ready pod -l app=loki -n orderflow --timeout=300s 2>/dev/null || print_warning "Loki timeout"

    # Wait for Alloy
    echo "⏳ Waiting for Alloy to be ready..."
    kubectl wait --for=condition=ready pod -l app=alloy -n orderflow --timeout=300s 2>/dev/null || print_warning "Alloy timeout"

    # Wait for Grafana
    echo "⏳ Waiting for Grafana to be ready..."
    kubectl wait --for=condition=ready pod -l app=grafana -n orderflow --timeout=300s 2>/dev/null || print_warning "Grafana timeout"

    print_success "ALG Stack deployed!"
}

deploy_alg_manual() {
    print_header "Deploying ALG Stack (Manual)"

    # Loki
    print_header "Deploying Loki"
    kubectl apply -f monitoring/alg/loki-pvc.yaml
    kubectl apply -f monitoring/alg/loki-configmap.yaml
    kubectl apply -f monitoring/alg/loki-deployment.yaml
    print_success "Loki deployed"

    sleep 10

    # Alloy
    print_header "Deploying Alloy"
    kubectl apply -f monitoring/alg/alloy-configmap.yaml
    kubectl apply -f monitoring/alg/alloy-deployment.yaml
    print_success "Alloy deployed"

    sleep 10

    # Grafana
    print_header "Deploying Grafana"
    kubectl apply -f monitoring/alg/grafana-alg.yaml
    print_success "Grafana deployed"

    echo "⏳ Waiting for all services to be ready..."
    sleep 30
    kubectl wait --for=condition=ready pod -l app=loki -n orderflow --timeout=300s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=alloy -n orderflow --timeout=300s 2>/dev/null || true
    kubectl wait --for=condition=ready pod -l app=grafana -n orderflow --timeout=300s 2>/dev/null || true

    print_success "ALG Stack deployed!"
}

view_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo "Usage: $0 logs <service>"
        echo ""
        echo "Available services:"
        echo "  - loki"
        echo "  - alloy"
        echo "  - grafana"
        exit 1
    fi

    kubectl logs -f -l app=$service -n orderflow --all-containers=true
}

test_connectivity() {
    print_header "Testing ALG Stack Connectivity"

    echo "Testing Loki..."
    kubectl run -it --rm debug --image=curlimages/curl:latest --restart=Never -- \
        curl -s http://loki:3100/ready || print_warning "Loki health check failed"

    echo "Testing Alloy..."
    kubectl run -it --rm debug --image=curlimages/curl:latest --restart=Never -- \
        curl -s http://alloy:12345/-/ready || print_warning "Alloy health check failed"

    echo "Testing Grafana..."
    kubectl run -it --rm debug --image=curlimages/curl:latest --restart=Never -- \
        curl -s http://grafana:3000/api/health || print_warning "Grafana health check failed"

    print_success "Connectivity tests completed"
}

cleanup_alg() {
    print_header "Cleaning Up ALG Stack"
    kubectl delete namespace orderflow-alg --ignore-not-found=true 2>/dev/null || true
    kubectl delete -l "part-of=alg-monitoring" -n orderflow --all 2>/dev/null || true
    print_success "ALG Stack cleanup completed"
}

# Main
print_header "OrderFlow ALG Stack Deployment"

case "${1:-deploy}" in
    deploy)
        deploy_alg_kustomize
        show_alg_status
        ;;
    manual)
        deploy_alg_manual
        show_alg_status
        ;;
    kustomize)
        deploy_alg_kustomize
        show_alg_status
        ;;
    status)
        show_alg_status
        ;;
    logs)
        view_logs "$2"
        ;;
    test)
        test_connectivity
        ;;
    cleanup)
        cleanup_alg
        ;;
    *)
        echo "Usage: $0 {deploy|manual|kustomize|status|logs|test|cleanup} [service]"
        echo ""
        echo "Commands:"
        echo "  deploy    - Deploy ALG Stack with Kustomize (recommended)"
        echo "  manual    - Deploy ALG Stack step by step"
        echo "  kustomize - Deploy ALG Stack with Kustomize"
        echo "  status    - Show ALG Stack status"
        echo "  logs      - View logs for a service (loki|alloy|grafana)"
        echo "  test      - Test connectivity of ALG services"
        echo "  cleanup   - Remove ALG Stack"
        exit 1
        ;;
esac
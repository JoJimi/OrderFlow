#!/bin/bash

# =====================================================
# OrderFlow with Monitoring Stack Deployment
# =====================================================
# Prometheus + Grafana를 포함한 배포 스크립트
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

print_header() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

show_status() {
    print_header "OrderFlow + Monitoring Deployment Status"

    echo -e "\n${YELLOW}📦 Pods:${NC}"
    kubectl get pods -n orderflow -o wide

    echo -e "\n${YELLOW}🌐 Services:${NC}"
    kubectl get svc -n orderflow

    echo -e "\n${YELLOW}🌍 Ingress:${NC}"
    kubectl get ingress -n orderflow

    MINIKUBE_IP=$(minikube ip)

    print_header "📊 Monitoring Stack Access"
    echo -e "${GREEN}Prometheus:  http://${MINIKUBE_IP}:30090${NC}"
    echo -e "${GREEN}Grafana:     http://${MINIKUBE_IP}:30300${NC}"
    echo ""
    echo -e "${BLUE}Grafana Login:${NC}"
    echo "  Username: admin"
    echo "  Password: admin123"
}

deploy_kustomize() {
    print_header "Deploying with Kustomize (Application + Monitoring)"
    kubectl apply -k .
    print_success "Applied kustomization"

    echo "Waiting for all pods..."
    sleep 30

    # Wait for infrastructure
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s || true
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s || true
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s || true

    # Wait for monitoring
    kubectl wait --for=condition=ready pod -l app=prometheus -n orderflow --timeout=120s || true
    kubectl wait --for=condition=ready pod -l app=grafana -n orderflow --timeout=120s || true

    print_success "All services deployed!"
}

deploy_monitoring_only() {
    print_header "Deploying Monitoring Stack Only"
    kubectl apply -k ./monitoring
    print_success "Monitoring stack deployed!"

    echo "Waiting for monitoring pods..."
    kubectl wait --for=condition=ready pod -l part-of=orderflow-monitoring -n orderflow --timeout=120s || true
    print_success "Monitoring ready!"
}

view_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo "Usage: $0 logs <service-name>"
        echo ""
        echo "Application Services:"
        echo "  - user-service"
        echo "  - product-service"
        echo "  - inventory-service"
        echo ""
        echo "Infrastructure:"
        echo "  - postgres, redis, kafka"
        echo ""
        echo "Monitoring:"
        echo "  - prometheus, grafana"
        exit 1
    fi

    kubectl logs -f -l app=$service -n orderflow --all-containers=true
}

# Main
print_header "OrderFlow Kubernetes Deployment with Monitoring"

case "${1:-deploy}" in
    deploy)
        kubectl apply -f base/namespace.yaml
        kubectl apply -f base/configmap.yaml
        kubectl apply -f infrastructure/postgres.yaml
        kubectl apply -f infrastructure/redis.yaml
        kubectl apply -f infrastructure/kafka.yaml
        sleep 30
        kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s || true
        kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s || true
        kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s || true
        kubectl apply -f services/user-service.yaml
        kubectl apply -f services/product-service.yaml
        kubectl apply -f services/inventory-service.yaml
        kubectl apply -f monitoring/prometheus-configmap.yaml
        kubectl apply -f monitoring/prometheus.yaml
        kubectl apply -f monitoring/grafana.yaml
        kubectl apply -f base/ingress.yaml
        print_success "Deployment completed!"
        show_status
        ;;
    kustomize)
        deploy_kustomize
        show_status
        ;;
    monitoring-only)
        deploy_monitoring_only
        show_status
        ;;
    status)
        show_status
        ;;
    logs)
        view_logs "$2"
        ;;
    cleanup)
        print_header "Cleaning Up"
        kubectl delete namespace orderflow --ignore-not-found=true
        print_success "Cleanup completed"
        ;;
    *)
        echo "Usage: $0 {deploy|kustomize|monitoring-only|status|logs|cleanup} [service-name]"
        echo ""
        echo "Commands:"
        echo "  deploy          - Deploy all services step by step"
        echo "  kustomize       - Deploy all using kustomize (recommended)"
        echo "  monitoring-only - Deploy only Prometheus + Grafana"
        echo "  status          - Show deployment status"
        echo "  logs            - View logs for a service"
        echo "  cleanup         - Remove all resources"
        exit 1
        ;;
esac
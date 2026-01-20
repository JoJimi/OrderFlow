# =====================================================
# OrderFlow with Monitoring Stack Deployment
# =====================================================
# Prometheus + Grafana를 포함한 배포 스크립트
# =====================================================

param(
    [Parameter(Position=0)]
    [ValidateSet("deploy", "kustomize", "status", "logs", "cleanup", "monitoring-only")]
    [string]$Command = "deploy",

    [Parameter(Position=1)]
    [string]$ServiceName = ""
)

# Colors
function Write-Success($message) {
    Write-Host "✓ $message" -ForegroundColor Green
}

function Write-Warning($message) {
    Write-Host "⚠ $message" -ForegroundColor Yellow
}

function Write-Header($message) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Blue
    Write-Host $message -ForegroundColor Blue
    Write-Host "============================================" -ForegroundColor Blue
    Write-Host ""
}

function Show-Status {
    Write-Header "OrderFlow + Monitoring Deployment Status"

    Write-Host "`n📦 Pods:" -ForegroundColor Yellow
    kubectl get pods -n orderflow -o wide

    Write-Host "`n🌐 Services:" -ForegroundColor Yellow
    kubectl get svc -n orderflow

    Write-Host "`n🌍 Ingress:" -ForegroundColor Yellow
    kubectl get ingress -n orderflow

    $MinikubeIP = minikube ip

    Write-Header "📊 Monitoring Stack Access"
    Write-Host "Prometheus:  http://${MinikubeIP}:30090" -ForegroundColor Green
    Write-Host "Grafana:     http://${MinikubeIP}:30300" -ForegroundColor Green
    Write-Host ""
    Write-Host "Grafana Login:" -ForegroundColor Cyan
    Write-Host "  Username: admin"
    Write-Host "  Password: admin123"
}

function Deploy-Kustomize {
    Write-Header "Deploying with Kustomize (Application + Monitoring)"
    kubectl apply -k .
    Write-Success "Applied kustomization"

    Write-Host "Waiting for all pods..."
    Start-Sleep -Seconds 30

    # Wait for infrastructure
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s -ErrorAction SilentlyContinue
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s -ErrorAction SilentlyContinue
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s -ErrorAction SilentlyContinue

    # Wait for monitoring
    kubectl wait --for=condition=ready pod -l app=prometheus -n orderflow --timeout=120s -ErrorAction SilentlyContinue
    kubectl wait --for=condition=ready pod -l app=grafana -n orderflow --timeout=120s -ErrorAction SilentlyContinue

    Write-Success "All services deployed!"
}

function Deploy-Monitoring-Only {
    Write-Header "Deploying Monitoring Stack Only"
    kubectl apply -k ./monitoring
    Write-Success "Monitoring stack deployed!"

    Write-Host "Waiting for monitoring pods..."
    kubectl wait --for=condition=ready pod -l part-of=orderflow-monitoring -n orderflow --timeout=120s -ErrorAction SilentlyContinue
    Write-Success "Monitoring ready!"
}

function View-Logs {
    param([string]$Service)

    if ([string]::IsNullOrEmpty($Service)) {
        Write-Host "Usage: .\deploy.ps1 logs <service-name>"
        Write-Host ""
        Write-Host "Application Services:"
        Write-Host "  - user-service"
        Write-Host "  - product-service"
        Write-Host "  - inventory-service"
        Write-Host ""
        Write-Host "Infrastructure:"
        Write-Host "  - postgres, redis, kafka"
        Write-Host ""
        Write-Host "Monitoring:"
        Write-Host "  - prometheus, grafana"
        exit 1
    }

    kubectl logs -f -l app=$Service -n orderflow --all-containers=true
}

# Main
Write-Header "OrderFlow Kubernetes Deployment"

switch ($Command) {
    "deploy" {
        kubectl apply -f base/namespace.yaml
        kubectl apply -f base/configmap.yaml
        kubectl apply -f infrastructure/postgres.yaml
        kubectl apply -f infrastructure/redis.yaml
        kubectl apply -f infrastructure/kafka.yaml
        Start-Sleep -Seconds 30
        kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s -ErrorAction SilentlyContinue
        kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s -ErrorAction SilentlyContinue
        kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s -ErrorAction SilentlyContinue
        kubectl apply -f services/user-service.yaml
        kubectl apply -f services/product-service.yaml
        kubectl apply -f services/inventory-service.yaml
        kubectl apply -f monitoring/prometheus-configmap.yaml
        kubectl apply -f monitoring/prometheus.yaml
        kubectl apply -f monitoring/grafana.yaml
        kubectl apply -f base/ingress.yaml
        Write-Success "Deployment completed!"
        Show-Status
    }
    "kustomize" {
        Deploy-Kustomize
        Show-Status
    }
    "monitoring-only" {
        Deploy-Monitoring-Only
        Show-Status
    }
    "status" {
        Show-Status
    }
    "logs" {
        View-Logs -Service $ServiceName
    }
    "cleanup" {
        Write-Header "Cleaning Up"
        kubectl delete namespace orderflow --ignore-not-found=true
        Write-Success "Cleanup completed"
    }
    default {
        Write-Host "Usage: .\deploy.ps1 {deploy|kustomize|monitoring-only|status|logs|cleanup} [service-name]"
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  deploy          - Deploy all services step by step"
        Write-Host "  kustomize       - Deploy all using kustomize (recommended)"
        Write-Host "  monitoring-only - Deploy only Prometheus + Grafana"
        Write-Host "  status          - Show deployment status"
        Write-Host "  logs            - View logs for a service"
        Write-Host "  cleanup         - Remove all resources"
    }
}
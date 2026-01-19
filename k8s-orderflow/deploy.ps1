# =====================================================
# OrderFlow Kubernetes Deployment Script for Windows
# =====================================================
# This script deploys OrderFlow microservices to Minikube
#
# Prerequisites:
# - Minikube installed and running
# - kubectl configured
# - Run as Administrator for hosts file modification
# =====================================================

param(
    [Parameter(Position=0)]
    [ValidateSet("deploy", "kustomize", "status", "cleanup", "logs", "tunnel")]
    [string]$Command = "deploy",

    [Parameter(Position=1)]
    [string]$ServiceName = ""
)

# Colors
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Header($message) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Blue
    Write-Host $message -ForegroundColor Blue
    Write-Host "============================================" -ForegroundColor Blue
    Write-Host ""
}

function Write-Success($message) {
    Write-Host "✓ $message" -ForegroundColor Green
}

function Write-Warning($message) {
    Write-Host "⚠ $message" -ForegroundColor Yellow
}

function Write-Error($message) {
    Write-Host "✗ $message" -ForegroundColor Red
}

# Get script directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Check prerequisites
function Check-Prerequisites {
    Write-Header "Checking Prerequisites"

    # Check kubectl
    if (Get-Command kubectl -ErrorAction SilentlyContinue) {
        Write-Success "kubectl is installed"
    } else {
        Write-Error "kubectl is not installed"
        exit 1
    }

    # Check minikube
    if (Get-Command minikube -ErrorAction SilentlyContinue) {
        Write-Success "minikube is installed"

        $status = minikube status 2>&1
        if ($status -match "Running") {
            Write-Success "minikube is running"
        } else {
            Write-Warning "minikube is not running. Starting with 4 CPUs and 8GB RAM..."
            minikube start --cpus=4 --memory=8192
        }
    } else {
        Write-Error "minikube is not installed"
        exit 1
    }
}

# Enable required addons
function Enable-Addons {
    Write-Header "Enabling Minikube Addons"

    Write-Host "Enabling ingress addon..."
    minikube addons enable ingress
    Write-Success "Ingress addon enabled"

    Write-Host "Enabling metrics-server addon..."
    minikube addons enable metrics-server
    Write-Success "Metrics-server addon enabled"
}

# Deploy directly with kubectl
function Deploy-Direct {
    Write-Header "Deploying OrderFlow Services"

    Write-Host "Step 1: Creating namespace..."
    kubectl apply -f "$ScriptDir\base\namespace.yaml"
    Write-Success "Namespace created"

    Write-Host "Step 2: Creating secrets and configmap..."
    kubectl apply -f "$ScriptDir\base\secrets.yaml"
    kubectl apply -f "$ScriptDir\base\configmap.yaml"
    Write-Success "Secrets and ConfigMap created"

    Write-Host "Step 3: Deploying infrastructure services..."
    kubectl apply -f "$ScriptDir\infrastructure\postgres.yaml"
    kubectl apply -f "$ScriptDir\infrastructure\redis.yaml"
    kubectl apply -f "$ScriptDir\infrastructure\kafka.yaml"
    Write-Success "Infrastructure services deployed"

    Write-Host "Step 4: Waiting for infrastructure to be ready..."
    Write-Host "Waiting for PostgreSQL (this may take a while)..."
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s
    Write-Success "PostgreSQL is ready"

    Write-Host "Waiting for Redis..."
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s
    Write-Success "Redis is ready"

    Write-Host "Waiting for Kafka (this may take a while)..."
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s
    Write-Success "Kafka is ready"

    Write-Host "Step 5: Deploying application services..."
    kubectl apply -f "$ScriptDir\services\user-service.yaml"
    kubectl apply -f "$ScriptDir\services\product-service.yaml"
    kubectl apply -f "$ScriptDir\services\inventory-service.yaml"
    Write-Success "Application services deployed"

    Write-Host "Step 6: Creating Ingress..."
    kubectl apply -f "$ScriptDir\base\ingress.yaml"
    Write-Success "Ingress created"

    Write-Host "Step 7: Waiting for application services to be ready..."
    Write-Host "This may take a few minutes as services start up..."

    Start-Sleep -Seconds 30

    kubectl wait --for=condition=ready pod -l app=user-service -n orderflow --timeout=300s 2>$null
    kubectl wait --for=condition=ready pod -l app=product-service -n orderflow --timeout=300s 2>$null
    kubectl wait --for=condition=ready pod -l app=inventory-service -n orderflow --timeout=300s 2>$null
}

# Deploy with Kustomize
function Deploy-Kustomize {
    Write-Header "Deploying OrderFlow Services with Kustomize"

    Write-Host "Applying all resources with kustomize..."
    kubectl apply -k $ScriptDir

    Write-Host "Waiting for all pods to be ready..."
    Start-Sleep -Seconds 30

    # Wait for infrastructure
    kubectl wait --for=condition=ready pod -l app=postgres -n orderflow --timeout=300s 2>$null
    kubectl wait --for=condition=ready pod -l app=redis -n orderflow --timeout=120s 2>$null
    kubectl wait --for=condition=ready pod -l app=kafka -n orderflow --timeout=300s 2>$null

    # Wait for services
    kubectl wait --for=condition=ready pod -l app=user-service -n orderflow --timeout=300s 2>$null
    kubectl wait --for=condition=ready pod -l app=product-service -n orderflow --timeout=300s 2>$null
    kubectl wait --for=condition=ready pod -l app=inventory-service -n orderflow --timeout=300s 2>$null
}

# Show status
function Show-Status {
    Write-Header "Deployment Status"

    Write-Host "`nPods:" -ForegroundColor Yellow
    kubectl get pods -n orderflow -o wide

    Write-Host "`nServices:" -ForegroundColor Yellow
    kubectl get svc -n orderflow

    Write-Host "`nIngress:" -ForegroundColor Yellow
    kubectl get ingress -n orderflow

    # Get Minikube IP
    $MinikubeIP = minikube ip

    Write-Header "Access Information"
    Write-Host "Minikube IP: $MinikubeIP" -ForegroundColor Green
    Write-Host ""
    Write-Host "NodePort Access (direct):"
    Write-Host "  - User Service:      http://${MinikubeIP}:30081"
    Write-Host "  - Product Service:   http://${MinikubeIP}:30082"
    Write-Host "  - Inventory Service: http://${MinikubeIP}:30085"
    Write-Host ""
    Write-Host "Ingress Access (via nginx):"
    Write-Host "  Add this line to C:\Windows\System32\drivers\etc\hosts (as Administrator):"
    Write-Host "  $MinikubeIP orderflow.local" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Or run: 'minikube tunnel' in a separate terminal for LoadBalancer access"
    Write-Host ""
    Write-Host "  Then access via:"
    Write-Host "  - User API:      http://orderflow.local/api/users"
    Write-Host "  - Auth API:      http://orderflow.local/api/auth"
    Write-Host "  - Product API:   http://orderflow.local/api/products"
    Write-Host "  - Inventory API: http://orderflow.local/api/inventory"
    Write-Host ""
    Write-Host "Health Check Endpoints:"
    Write-Host "  - User Service:      http://${MinikubeIP}:30081/actuator/health"
    Write-Host "  - Product Service:   http://${MinikubeIP}:30082/actuator/health"
    Write-Host "  - Inventory Service: http://${MinikubeIP}:30085/actuator/health"
}

# Cleanup
function Invoke-Cleanup {
    Write-Header "Cleaning Up"

    Write-Host "Deleting orderflow namespace and all resources..."
    kubectl delete namespace orderflow --ignore-not-found=true

    Write-Success "Cleanup completed"
}

# View logs
function View-Logs {
    param([string]$Service)

    if ([string]::IsNullOrEmpty($Service)) {
        Write-Host "Usage: .\deploy.ps1 logs <service-name>"
        Write-Host "Available services: user-service, product-service, inventory-service, postgres, redis, kafka"
        exit 1
    }

    kubectl logs -f -l app=$Service -n orderflow --all-containers=true
}

# Start tunnel
function Start-Tunnel {
    Write-Header "Starting Minikube Tunnel"
    Write-Host "This will require Administrator privileges..."
    Write-Host "Press Ctrl+C to stop the tunnel"
    minikube tunnel
}

# Main
switch ($Command) {
    "deploy" {
        Check-Prerequisites
        Enable-Addons
        Deploy-Direct
        Show-Status
    }
    "kustomize" {
        Check-Prerequisites
        Enable-Addons
        Deploy-Kustomize
        Show-Status
    }
    "status" {
        Show-Status
    }
    "cleanup" {
        Invoke-Cleanup
    }
    "logs" {
        View-Logs -Service $ServiceName
    }
    "tunnel" {
        Start-Tunnel
    }
    default {
        Write-Host "Usage: .\deploy.ps1 {deploy|kustomize|status|cleanup|logs|tunnel} [service-name]"
        Write-Host ""
        Write-Host "Commands:"
        Write-Host "  deploy    - Deploy services step by step"
        Write-Host "  kustomize - Deploy using kustomize"
        Write-Host "  status    - Show deployment status"
        Write-Host "  cleanup   - Remove all resources"
        Write-Host "  logs      - View logs for a service"
        Write-Host "  tunnel    - Start minikube tunnel for LoadBalancer"
    }
}
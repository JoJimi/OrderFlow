# build-push-all.ps1
$DOCKER_USERNAME = "jimineutron"
$TAG = "latest"

$services = @(
    "eureka-server",
    "api-gateway", 
    "user-service",
    "product-service",
    "order-service",
    "payment-service",
    "inventory-service",
    "shipping-service",
    "notification-service",
    "event-logging-service"
)

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  OrderFlow Docker Build & Push Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Gradle 빌드
Write-Host "[1/3] Gradle Build Starting..." -ForegroundColor Yellow
.\gradlew.bat clean build -x test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Gradle build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Gradle Build Success!" -ForegroundColor Green
Write-Host ""

# 2. Docker 이미지 빌드
Write-Host "[2/3] Docker Build Starting..." -ForegroundColor Yellow
foreach ($service in $services) {
    $imageName = "${DOCKER_USERNAME}/orderflow-${service}:${TAG}"
    
    Write-Host "  Building: $imageName" -ForegroundColor Cyan
    docker build -t $imageName --build-arg SERVICE_NAME=$service -f Dockerfile ./$service
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  Failed to build $service" -ForegroundColor Red
        exit 1
    }
}
Write-Host "Docker Build Success!" -ForegroundColor Green
Write-Host ""

# 3. Docker 이미지 푸시
Write-Host "[3/3] Docker Push Starting..." -ForegroundColor Yellow
foreach ($service in $services) {
    $imageName = "${DOCKER_USERNAME}/orderflow-${service}:${TAG}"
    
    Write-Host "  Pushing: $imageName" -ForegroundColor Cyan
    docker push $imageName
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  Failed to push $service" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Magenta
Write-Host "  All images pushed to DockerHub!" -ForegroundColor Magenta
Write-Host "==========================================" -ForegroundColor Magenta

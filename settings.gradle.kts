rootProject.name = "OrderFlow"

include(
    "shared",
    "user-service",
    "product-service",
    "order-service",
    "payment-service",
    "inventory-service",
    "shipping-service",
    "notification-service",
    "event-logging-service",
    "api-gateway"
)

// 옵션: 프로젝트 구조 설정
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
// root의 settings.gradle.kts
rootProject.name = "OrderFlow"

include(
    "shared",
    "eureka-server",
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
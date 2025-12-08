// product-service의 build.gradle.kts
dependencies {
    // Spring Data Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Jackson (JSON 처리)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Test
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("org.example.product.ProductServiceApplication")
}
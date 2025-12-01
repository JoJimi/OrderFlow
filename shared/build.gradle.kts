// bootJar 태스크 비활성화 (shared는 실행 가능한 jar가 아님)
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

// 일반 jar 태스크 활성화
tasks.named<Jar>("jar") {
    enabled = true
}

dependencies {
    // Jackson (JSON 처리)
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
}
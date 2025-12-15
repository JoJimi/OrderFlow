dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.kafka:spring-kafka")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
	mainClass.set("org.example.payment.PaymentServiceApplication")
}
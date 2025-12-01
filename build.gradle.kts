plugins {
	java
	id("org.springframework.boot") version "3.2.0" apply false
	id("io.spring.dependency-management") version "1.1.4"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

allprojects {
	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	group = "org.example"
	version = "0.0.1-SNAPSHOT"

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	configurations {
		compileOnly {
			extendsFrom(annotationProcessor.get())
		}
	}

	dependencies {
		implementation("org.springframework.boot:spring-boot-starter-web")
		implementation("org.springframework.boot:spring-boot-starter-data-jpa")

		compileOnly("org.projectlombok:lombok")
		annotationProcessor("org.projectlombok:lombok")

		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}

// shared 라이브러리는 특별히 처리
project(":shared") {
	apply(plugin = "java-library")
}

// ⭐ JPA가 필요한 마이크로서비스들 (DB 사용)
listOf(
		"user-service",
		"product-service",
		"order-service",
		"payment-service",
		"inventory-service",
		"shipping-service",
		"notification-service",
		"event-logging-service"
).forEach { service ->
	project(":$service") {
		dependencies {
			// shared 라이브러리 의존
			implementation(project(":shared"))

			implementation("org.springframework.boot:spring-boot-starter-validation")
			implementation("org.springframework.boot:spring-boot-starter-actuator")

			runtimeOnly("org.postgresql:postgresql")
		}
	}
}
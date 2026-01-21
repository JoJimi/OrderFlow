// root의 build.gradle.kts
plugins {
	java
	id("org.springframework.boot") version "3.2.0" apply false
	id("io.spring.dependency-management") version "1.1.4"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

extra["springCloudVersion"] = "2023.0.3"
extra["springdocVersion"] = "2.3.0"

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
		compileOnly("org.projectlombok:lombok")
		annotationProcessor("org.projectlombok:lombok")

		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")

		implementation("me.paulschwarz:spring-dotenv:4.0.0")

		implementation("io.micrometer:micrometer-registry-prometheus")
	}

	the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
		imports {
			mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}

	if (name != "shared") {
		tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
			enabled = true
		}

		tasks.named<Jar>("jar") {
			enabled = false
		}
	}
}

// shared 라이브러리는 특별히 처리
project(":shared") {
	apply(plugin = "java-library")
}

// JPA가 필요한 마이크로서비스들 (DB 사용)
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
			implementation("org.springframework.boot:spring-boot-starter-security")
			implementation("me.paulschwarz:spring-dotenv:4.0.0")

			// Spring Boot 기본
			implementation("org.springframework.boot:spring-boot-starter-web")
			implementation("org.springframework.boot:spring-boot-starter-data-jpa")
			implementation("org.springframework.boot:spring-boot-starter-validation")
			implementation("org.springframework.boot:spring-boot-starter-actuator")

			// Eureka Client
			implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

			// Swagger/OpenAPI
			implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")

			// cache
			implementation("com.github.ben-manes.caffeine:caffeine")
			implementation("org.springframework.boot:spring-boot-starter-cache")

			runtimeOnly("org.postgresql:postgresql")

			// Jakarta 관련 의존성 추가 (Elasticsearch 필요)
			implementation("jakarta.activation:jakarta.activation-api:2.1.2")
			implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.1")
		}
	}
}

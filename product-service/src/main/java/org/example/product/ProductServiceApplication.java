package org.example.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {"org.example.product", "org.example.shared"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        // ========== 디버깅 코드 시작 ==========
        System.out.println("================================");
        System.out.println("환경 변수 확인:");
        System.out.println("JWT_SECRET: " + System.getenv("JWT_SECRET"));
        System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
        System.out.println("POSTGRES_USER: " + System.getenv("POSTGRES_USER"));
        System.out.println("REDIS_HOST: " + System.getenv("REDIS_HOST"));
        System.out.println("KAFKA_BOOTSTRAP_SERVERS: " + System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        System.out.println("================================");
        // ========== 디버깅 코드 끝 ==========

        SpringApplication.run(ProductServiceApplication.class, args);
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

}
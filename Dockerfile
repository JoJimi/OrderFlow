# Stage 1: Build
FROM gradle:8.6-jdk21 AS builder

ARG SERVICE_NAME

WORKDIR /app
COPY . .

# 특정 서비스만 빌드
RUN gradle ${SERVICE_NAME}:build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME

WORKDIR /app

# Stage 1에서 생성한 JAR 복사
COPY --from=builder /app/${SERVICE_NAME}/build/libs/*.jar app.jar

RUN apk add --no-cache curl

EXPOSE 8080 8081 8082 8083 8084 8085 8086 8087 8088 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
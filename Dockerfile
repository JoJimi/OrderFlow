# 루트의 Dockerfile
FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME

WORKDIR /app

RUN apk add --no-cache curl

COPY ${SERVICE_NAME}/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
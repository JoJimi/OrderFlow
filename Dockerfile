FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_NAME

WORKDIR /app

# 로컬에서 빌드된 jar 파일 복사
COPY ${SERVICE_NAME}/build/libs/*.jar app.jar

RUN apk add --no-cache curl

EXPOSE 8080 8081 8082 8083 8084 8085 8086 8087 8088 8761

ENTRYPOINT ["java", "-jar", "app.jar"]

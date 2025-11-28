# Build Stage
FROM gradle:8.5-jdk21 AS build

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY src ./src

RUN gradle bootJar --no-daemon

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

RUN apk add --no-cache curl

EXPOSE 8080 9080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew .
COPY gradle.properties .
COPY build.gradle.kts .
COPY settings.gradle.kts .

COPY src/ src/

RUN ./gradlew buildFatJar -x test --no-daemon

FROM openjdk:17-jre-slim

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

CMD java -jar app.jar -P:ktor.deployment.port=${PORT:-8080}

FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

COPY . .

RUN gradle buildFatJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

RUN addgroup -S appuser && adduser -S appuser -G appuser

WORKDIR /app

COPY --from=builder /app/build/libs/*-all.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

CMD java -jar app.jar -P:ktor.deployment.port=${PORT:-8080}

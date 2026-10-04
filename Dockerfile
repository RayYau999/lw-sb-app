# syntax=docker/dockerfile:1
# Build context = project root.
# Build both images from one Dockerfile:
#   docker build --target api-gateway -t lw-sb-api-gateway .
#   docker build --target application -t lw-sb-application .

# ---------- Build stage: compiles both modules ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY application/pom.xml application/
RUN mvn -B -q -DskipTests dependency:go-offline || true

COPY api-gateway/src api-gateway/src
COPY application/src application/src
RUN mvn -B -DskipTests package

# ---------- api-gateway image (port 8080) ----------
FROM eclipse-temurin:21-jre AS api-gateway
WORKDIR /app
RUN useradd --system --no-create-home --shell /usr/sbin/nologin appuser
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar /app/app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

# ---------- application image (port 8081) ----------
FROM eclipse-temurin:21-jre AS application
WORKDIR /app
RUN useradd --system --no-create-home --shell /usr/sbin/nologin appuser
COPY --from=build /workspace/application/target/application-*.jar /app/app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]

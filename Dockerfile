# 🏗️ Multi-stage build for optimal image size
FROM amazoncorretto:21-alpine AS builder

# 📦 Install Maven and build dependencies
RUN apk add --no-cache maven

# 📁 Set working directory
WORKDIR /app

# 📋 Copy Maven configuration files first (for better layer caching)
COPY pom.xml .
COPY src ./src

# 🔨 Build the application (skip tests in container build)
RUN mvn clean package -DskipTests

# 🚀 Runtime image
FROM amazoncorretto:21-alpine

# 📋 Labels for better container management
LABEL org.opencontainers.image.title="AWX Operator"
LABEL org.opencontainers.image.description="Kubernetes operator for managing AWX connections and projects"
LABEL org.opencontainers.image.vendor="Wolkenzentrale"
LABEL org.opencontainers.image.version="0.0.1"
LABEL org.opencontainers.image.source="https://github.com/wolkenzentrale/awx-operator"

# 🛡️ Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 📁 Create application directory
RUN mkdir -p /opt/app && \
    chown -R appuser:appgroup /opt/app

# 📁 Set working directory
WORKDIR /opt/app

# 📦 Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/target/*.jar app.jar

# 🔧 Install curl for health checks
RUN apk add --no-cache curl

# 👤 Switch to non-root user
USER 1001

# 🌐 Expose ports
EXPOSE 8080 8081

# ⚡ JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# 🔍 Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health/readiness || exit 1

# 🚀 Entry point
ENTRYPOINT ["java", "-jar", "app.jar"] 
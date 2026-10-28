# ==============================================================================
# STAGE 1: BUILD (compile the application with Maven)
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy only pom.xml first to cache dependencies between builds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and build the executable jar (tests run in CI, not in the image build)
COPY src ./src
RUN mvn package -B -DskipTests -Dspotless.check.skip=true -Djacoco.skip=true

# ==============================================================================
# STAGE 2: RUNTIME (minimal JRE image)
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

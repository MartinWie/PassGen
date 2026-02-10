# Stage 1: Build the application
FROM gradle:8.13-jdk21 AS builder

WORKDIR /app

# Copy gradle config files first for better layer caching
COPY gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached unless gradle files change)
RUN gradle dependencies --no-daemon --no-configuration-cache -x generateJooqClasses

# Copy source code (includes committed JOOQ classes in src/main/java/)
COPY src ./src

# Build the fat jar, skipping JOOQ generation (uses committed sources) and tests
RUN gradle shadowJar --no-daemon --no-configuration-cache -x generateJooqClasses -x test

# Stage 2: Runtime with distroless
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy the fat jar from builder
COPY --from=builder /app/build/libs/de.mw.passgen-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

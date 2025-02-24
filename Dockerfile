# Use Amazon Corretto 21 as the base image
FROM amazoncorretto:21-alpine

# Healthcheck
HEALTHCHECK --interval=20s --timeout=5s --retries=3 CMD curl -f http://localhost/health || exit 1

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from your build output directory to the container
COPY build/libs/de.mw.passgen-all.jar /app/app.jar

# Expose a port if your application requires it (replace 8080 with your application's port)
EXPOSE 8080

# Define the entry point for the container to run the application
CMD ["java", "-jar", "/app/app.jar"]
#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status.

echo "Starting full clean build process..."

# Function to check the last command's status
check_status() {
    if [ $? -ne 0 ]; then
        echo "Error: $1 failed"
        exit 1
    fi
}

# Clean Gradle build
echo "Cleaning Gradle build..."
./gradlew clean
check_status "Gradle clean"

# Refresh dependencies
echo "Refreshing dependencies..."
./gradlew --refresh-dependencies
check_status "Dependency refresh"

# Compile Tailwind CSS
echo "Compiling Tailwind CSS..."
npx tailwindcss -i ./src/main/resources/static/input.css -o ./src/main/resources/static/output.css || { echo "Tailwind CSS compilation failed"; exit 1; }
check_status "Tailwind CSS compilation"

# Run Flyway migrations
echo "Running Flyway migrations..."
./gradlew flywayMigrate --no-configuration-cache
check_status "Flyway migrations"

# Generate jOOQ classes
echo "Generating jOOQ classes..."
./gradlew generateJooq
check_status "jOOQ class generation"

# Final build to generate the jar
./gradlew build
check_status "Project build"

echo "Full clean build process completed successfully!"
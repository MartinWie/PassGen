val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val jooq_version: String by project
val postgresql_version: String by project
val flyway_version: String by project

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.3.4")
    }
}

plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.flywaydb.flyway") version "11.3.4"
    id("dev.monosoul.jooq-docker") version "7.0.14" // JOOQ generate classes with test container
}

group = "de.mw"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
}

val postgresHost: String = System.getenv("SECRET_PASSGEN_DB-HOST") ?: "localhost"
val postgresUser: String = System.getenv("SECRET_PASSGEN_DB-USER") ?: "admin"
val postgresPassword: String =
    System.getenv("SECRET_PASSGEN_DB-PASSWORD") ?: "Helpless-Phrase-Unrushed-Radar0-Buzz-Curling-Haggler"

application {
    mainClass.set("de.mw.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    maven { url = uri("https://jitpack.io") } // For kotlinx-htmx library
}

dependencies {
    // HTMX utilities for kotlinx-html
    implementation("com.github.MartinWie:kotlinx-htmx:v0.2.0")

    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.12.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css-jvm:2025.3.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // Database
    implementation("org.jooq:jooq:$jooq_version")
    jooqCodegen("org.jooq:jooq-meta:$jooq_version")
    jooqCodegen("org.jooq:jooq-codegen:$jooq_version")
    jooqCodegen("org.postgresql:postgresql:$postgresql_version")

    // Flyway dependency for database migrations
    implementation("org.flywaydb:flyway-database-postgresql:$flyway_version")
    implementation("org.postgresql:postgresql:$postgresql_version")

    // Database connection pooling
    implementation("com.zaxxer:HikariCP:6.2.1")

    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

jooq {
    withContainer {
        image {
            name = "postgres:15"
            envVars =
                mapOf(
                    "POSTGRES_DB" to "postgres",
                    "POSTGRES_USER" to "postgres",
                    "POSTGRES_PASSWORD" to "postgres",
                )
        }
    }
}

flyway {
    url = "jdbc:postgresql://$postgresHost:5432/passgen"
    user = postgresUser
    password = postgresPassword
    schemas = arrayOf("public")
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

tasks {
    generateJooqClasses {
        basePackageName.set("de.mw.generated")
    }

    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("all")
        configurations = listOf(project.configurations.runtimeClasspath.get())
        mergeServiceFiles()
    }

    named("flywayMigrate") {
        inputs.files(fileTree("src/main/resources/db/migration"))
        outputs.dir("${layout.buildDirectory}/flyway-output")
    }

    named("compileKotlin") {
        dependsOn("generateJooqClasses")
    }
}

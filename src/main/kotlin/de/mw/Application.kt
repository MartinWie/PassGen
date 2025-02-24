package de.mw

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.mw.plugins.configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import javax.sql.DataSource

// Simple base logger
private val logger = LoggerFactory.getLogger("de.mw.Application")

// Environment variables(the defaults are only for local services, do not use them in any other env!!!!!!!!!!!!!!!!!!!!)
val envHost = System.getenv("APP_HOST") ?: "0.0.0.0"
val postgresHost: String = System.getenv("SECRET_PASSGEN_DB-HOST") ?: "localhost"
val postgresUser: String = System.getenv("SECRET_PASSGEN_DB-USER") ?: "admin"
val postgresPassword: String =
    System.getenv("SECRET_PASSGEN_DB-PASSWORD") ?: "Helpless-Phrase-Unrushed-Radar0-Buzz-Curling-Haggler"
val fullDomain: String = System.getenv("SECRET_PASSGEN_DOMAIN") ?: "http://localhost:8080"

// HikariCP data source configuration
val hikariConfig = HikariConfig().apply {
    jdbcUrl = "jdbc:postgresql://$postgresHost:5432/passgen"
    username = postgresUser
    password = postgresPassword
    addDataSourceProperty("cachePrepStmts", "true")
    addDataSourceProperty("prepStmtCacheSize", "250")
    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
    addDataSourceProperty("maximumPoolSize", "2")
}

val dataSource: DataSource = HikariDataSource(hikariConfig)

// JOOQ dsl context setup
val dsl = DSL.using(dataSource, SQLDialect.POSTGRES)

fun main() {
    logger.info("Starting Ktor application...")

    embeddedServer(Netty, port = 8080, host = envHost, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureRouting()
}

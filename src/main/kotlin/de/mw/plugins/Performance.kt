package de.mw.plugins

import io.ktor.http.CacheControl
import io.ktor.http.content.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.request.path
import io.ktor.util.date.GMTDate

fun Application.configurePerformance() {
    install(Compression) {
        gzip {
            priority = 1.0
            minimumSize(256)
        }
        deflate {
            priority = 0.9
            minimumSize(256)
        }
    }

    install(ConditionalHeaders)

    install(CachingHeaders) {
        options { call, _ ->
            if (!call.request.path().startsWith("/static/")) return@options null

            val maxAgeSeconds = 3600
            val expiresAt = GMTDate(System.currentTimeMillis() + maxAgeSeconds * 1000L)

            CachingOptions(
                cacheControl = CacheControl.MaxAge(maxAgeSeconds = maxAgeSeconds),
                expires = expiresAt,
            )
        }
    }
}

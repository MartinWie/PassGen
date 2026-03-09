package de.mw.plugins

import io.ktor.http.CacheControl
import io.ktor.http.content.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders

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
        options { _, outgoingContent ->
            val isStaticResource = outgoingContent is URIFileContent
            if (!isStaticResource) return@options null

            CachingOptions(
                cacheControl = CacheControl.MaxAge(maxAgeSeconds = 3600),
            )
        }
    }
}

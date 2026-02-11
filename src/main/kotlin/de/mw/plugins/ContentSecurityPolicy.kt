package de.mw.plugins

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Builds the Content-Security-Policy header value.
 *
 * @param isDevelopment When `true` (`io.ktor.development=true`), the policy
 *   additionally allows `localhost:3000` origins for the Vite/Tailwind dev
 *   server and its WebSocket connections.
 *
 * **Note on `script-src`:** The directive uses `'unsafe-inline'` because
 * several templates rely on inline event handler attributes (`onclick`,
 * `onsubmit` via `onEvent`), and the theme-init `<script>` in the page
 * head is also inline.  A nonce is intentionally **not** included because
 * CSP-compliant browsers ignore `'unsafe-inline'` when a nonce or hash is
 * present in the same `script-src` directive — which would break every
 * inline event handler attribute.
 */
fun buildCspHeaderValue(isDevelopment: Boolean): String {
    val devOrigins =
        if (isDevelopment) " http://localhost:3000" else ""
    val devWsOrigins =
        if (isDevelopment) " http://localhost:3000 ws://localhost:3000 wss://localhost:3000" else ""

    return "default-src 'self';" +
        " script-src 'self' 'unsafe-inline'$devOrigins;" +
        " object-src 'none';" +
        " base-uri 'none';" +
        " frame-ancestors 'none';" +
        " form-action 'self';" +
        " img-src 'self' data:;" +
        " style-src 'self' 'unsafe-inline';" +
        " font-src 'self' data:;" +
        " connect-src 'self'$devWsOrigins;"
}

/**
 * Responds with an HTML body while appending the Content-Security-Policy header.
 *
 * Usage:
 * ```kotlin
 * get("/page") {
 *     call.respondHtmlWithCsp { getMyPage() }
 * }
 * ```
 */
suspend fun RoutingCall.respondHtmlWithCsp(htmlProvider: () -> String) {
    val html = htmlProvider()

    response.headers.append(
        "Content-Security-Policy",
        buildCspHeaderValue(application.developmentMode),
    )
    respondText(html, ContentType.Text.Html)
}

package de.mw.plugins

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URI

private const val DEFAULT_POSTHOG_KEY = "phc_GxF97xQ1R685lo6S7bwRf6HFB1Ta56lAAJLFhtln60p"

private fun posthogKeyConfigured(): Boolean =
    !System.getenv("POSTHOG_KEY").isNullOrBlank() ||
            !System.getenv("SECRET_POSTHOG_KEY").isNullOrBlank() ||
            DEFAULT_POSTHOG_KEY.isNotBlank()

private fun posthogEnabledForCsp(): Boolean =
    System.getenv("POSTHOG_ENABLED")?.lowercase() != "false" && posthogKeyConfigured()

private fun normalizeOrigin(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    val withScheme = if (value.startsWith("http://") || value.startsWith("https://")) value else "https://$value"
    return runCatching {
        val uri = URI(withScheme)
        val scheme = uri.scheme ?: return null
        val host = uri.host ?: return null
        val port = uri.port
        if (port == -1 || (scheme == "https" && port == 443) || (scheme == "http" && port == 80)) {
            "$scheme://$host"
        } else {
            "$scheme://$host:$port"
        }
    }.getOrNull()
}

private fun posthogOrigins(): Set<String> {
    if (!posthogEnabledForCsp()) return emptySet()

    val configuredHost = System.getenv("POSTHOG_HOST")?.takeIf { it.isNotBlank() } ?: "https://eu.i.posthog.com"
    val hostOrigin = normalizeOrigin(configuredHost) ?: return emptySet()
    val assetsOrigin =
        if (hostOrigin.contains(".i.posthog.com")) {
            hostOrigin.replace(".i.posthog.com", "-assets.i.posthog.com")
        } else {
            hostOrigin
        }

    return linkedSetOf(hostOrigin, assetsOrigin)
}

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

    val posthog = posthogOrigins()
    val posthogScriptSrc = if (posthog.isEmpty()) "" else " " + posthog.joinToString(" ")
    val posthogConnectSrc = if (posthog.isEmpty()) "" else " " + posthog.joinToString(" ")

    return "default-src 'self';" +
            " script-src 'self' 'unsafe-inline'$devOrigins$posthogScriptSrc;" +
            " object-src 'none';" +
            " base-uri 'none';" +
            " frame-ancestors 'none';" +
            " form-action 'self';" +
            " img-src 'self' data:;" +
            " style-src 'self' 'unsafe-inline';" +
            " font-src 'self' data:;" +
            " connect-src 'self'$devWsOrigins$posthogConnectSrc;"
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

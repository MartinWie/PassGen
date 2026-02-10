package de.mw.plugins

import io.github.martinwie.htmx.PageSecurityContext
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.SecureRandom
import java.util.*

/**
 * Builds the Content-Security-Policy header value.
 *
 * In development mode (`io.ktor.development=true`), the policy allows
 * `localhost:3000` origins for the Vite/Tailwind dev server.
 * In production these origins are omitted.
 */
fun buildCspHeaderValue(isDevelopment: Boolean): String {
    val devOrigins =
        if (isDevelopment) " http://localhost:3000" else ""
    val devWsOrigins =
        if (isDevelopment) " http://localhost:3000 ws://localhost:3000 wss://localhost:3000" else ""

    return "default-src 'self';" +
        " script-src 'self' 'unsafe-inline' 'unsafe-eval'$devOrigins;" +
        " object-src 'none';" +
        " base-uri 'none';" +
        " frame-ancestors 'none';" +
        " img-src 'self' data:;" +
        " style-src 'self' 'unsafe-inline';" +
        " font-src 'self' data:;" +
        " connect-src 'self'$devWsOrigins;"
}

/**
 * Responds with an HTML body while generating a fresh CSP nonce and
 * appending the Content-Security-Policy header.
 *
 * The nonce is stored in [PageSecurityContext.scriptNonce] for the duration
 * of the [htmlProvider] call, then cleaned up in a `finally` block.
 *
 * **Thread-safety note:** [PageSecurityContext.scriptNonce] uses a ThreadLocal
 * which is NOT inherently coroutine-safe. This is safe here because [htmlProvider]
 * is a non-suspend `() -> String` — no coroutine suspension can occur between
 * setting and reading the nonce. Do NOT change [htmlProvider] to a suspend function
 * without first making the nonce storage coroutine-aware (e.g. via
 * `ThreadLocal.asContextElement()` or `call.attributes`).
 *
 * **CSP note:** The current CSP includes `'unsafe-inline'`, which makes the nonce
 * ineffective from a security standpoint. The nonce infrastructure is kept in place
 * to simplify the future transition to a strict nonce-only CSP.
 *
 * Usage:
 * ```kotlin
 * get("/page") {
 *     call.respondHtmlWithCsp { getMyPage() }
 * }
 * ```
 */
suspend fun RoutingCall.respondHtmlWithCsp(htmlProvider: () -> String) {
    val nonceBytes = ByteArray(16)
    SecureRandom().nextBytes(nonceBytes)
    val nonce = Base64.getEncoder().encodeToString(nonceBytes)
    PageSecurityContext.scriptNonce = nonce

    try {
        response.headers.append(
            "Content-Security-Policy",
            buildCspHeaderValue(application.developmentMode),
        )
        respondText(htmlProvider(), ContentType.Text.Html)
    } finally {
        PageSecurityContext.scriptNonce = null
    }
}

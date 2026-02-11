package de.mw.plugins

import de.mw.frontend.pages.getLandingPage
import de.mw.frontend.utils.escapeHtml
import de.mw.plugins.routes.keyRouting
import de.mw.plugins.routes.passwordRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Responds with an error alert fragment that HTMX will swap into `#global-notification`.
 *
 * Sets `HX-Retarget` and `HX-Reswap` headers so the error toast appears in the
 * global notification region instead of the element that initiated the request.
 * The toast is auto-dismissed by app.js (htmx:afterSettle listener).
 */
suspend fun RoutingCall.respondHtmxError(
    message: String,
    status: HttpStatusCode = HttpStatusCode.BadRequest,
) {
    val escaped = message.escapeHtml()
    response.headers.append("HX-Retarget", "#global-notification")
    response.headers.append("HX-Reswap", "innerHTML")
    respondText(
        """<div class="alert alert-error shadow-lg" role="alert"><span>$escaped</span></div>""",
        ContentType.Text.Html,
        status,
    )
}

fun Application.configureRouting() {
    install(StatusPages) {
        status(HttpStatusCode.TooManyRequests) { call, _ ->
            call.application.log.warn(
                "Rate limited: {} on {}",
                call.request.origin.remoteHost,
                call.request.path(),
            )
            if (call.request.headers["HX-Request"] != null) {
                // Retarget the response into the global notification region so HTMX
                // never accidentally replaces the form or other page content.
                call.response.headers.append("HX-Retarget", "#global-notification")
                call.response.headers.append("HX-Reswap", "innerHTML")
                call.respondText(
                    """<div class="alert alert-warning shadow-lg" role="alert">""" +
                        """<span>Too many requests — please wait a moment and try again.</span>""" +
                        """</div>""",
                    ContentType.Text.Html,
                    HttpStatusCode.TooManyRequests,
                )
            } else {
                call.respondText(
                    "Too many requests — please wait a moment and try again.",
                    ContentType.Text.Plain,
                    HttpStatusCode.TooManyRequests,
                )
            }
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        }
    }
    routing {
        get("/") {
            call.respondHtmlWithCsp { getLandingPage("PassGen") }
        }

        get("/health") {
            call.respondText(status = HttpStatusCode.OK, text = "OK")
        }

        passwordRouting()
        keyRouting()

        staticResources("/static", "static")
    }
}

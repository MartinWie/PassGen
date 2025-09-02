package de.mw.plugins

import de.mw.frontend.pages.getLandingPage
import de.mw.frontend.utils.PageSecurityContext
import de.mw.plugins.routes.passwordRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.security.SecureRandom
import java.util.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        }
    }
    routing {
        get("/") {
            val nonceBytes = ByteArray(16)
            SecureRandom().nextBytes(nonceBytes)
            val nonce = Base64.getEncoder().encodeToString(nonceBytes)
            PageSecurityContext.scriptNonce = nonce
            try {
                call.response.headers.append(
                    "Content-Security-Policy",
                    "default-src 'self'; script-src 'self' 'nonce-$nonce'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; font-src 'self' data:"
                )
                call.respondText(getLandingPage("PassGen"), ContentType.Text.Html)
            } finally {
                PageSecurityContext.scriptNonce = null
            }
        }

        get("/health") {
            call.respondText(status = HttpStatusCode.OK, text = "OK")
        }

        passwordRouting()

        staticResources("/static", "static")
    }
}

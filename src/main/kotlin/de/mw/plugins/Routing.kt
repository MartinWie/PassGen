package de.mw.plugins

import de.mw.frontend.pages.getLandingPage
import de.mw.plugins.routes.passwordRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal Server Error")
        }
    }
    routing {
        get("/") {
            call.respondText(getLandingPage("PassGen"), ContentType.Text.Html)
        }

        get("/health") {
            call.respondText(status = HttpStatusCode.OK, text = "OK")
        }

        passwordRouting()

        staticResources("/static", "static")
    }
}

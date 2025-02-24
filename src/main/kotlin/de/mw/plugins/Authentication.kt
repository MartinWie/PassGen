package de.mw.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

/* Keeping that for later
fun Application.configureAuthentication() {
    install(Authentication) {
        session<UserSession>(authenticationUserSession) {
            validate { session ->
                if (userAndSessionService.validateSession(session)) {
                    UserIdPrincipal(session.id.toString())
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/event/login")
            }
        }
        session<SaasSession>(authenticationSaasSession) {
            validate { saasSession ->
                if (userAndSessionService.validateSaasUserSession(saasSession)) {
                    UserIdPrincipal(saasSession.id.toString())
                } else {
                    null
                }
            }
            challenge {
                call.respondRedirect("/register")
            }
        }
    }
}
 */
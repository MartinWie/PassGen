package de.mw.plugins.routes

import de.mw.models.WordLanguage
import de.mw.passwordService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// TODO: add request parameters
fun Route.passwordRouting(){
    get("/word") {
        call.respondText(passwordService.getWords(4, WordLanguage.ENG).joinToString("-"), ContentType.Text.Plain)
    }
}
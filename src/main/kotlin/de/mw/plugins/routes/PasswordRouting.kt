package de.mw.plugins.routes

import de.mw.frontend.utils.buildHTMLString
import de.mw.models.WordLanguage
import de.mw.passwordService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.textArea

fun Route.passwordRouting() {
    get("/word") {
        val parameters = call.queryParameters

        val language = parameters["language-select"]?.let { WordLanguage.valueOf(it) } ?: return@get call.respond(
            HttpStatusCode.BadRequest, "Missing language"
        )

        val wordAmount = parameters["word-amount-slider"]?.toInt() ?: return@get call.respond(
            HttpStatusCode.BadRequest, "Missing amount"
        )

        val spacialChars = parameters["include-special"]?.let { it.uppercase() == "ON" } ?: false

        val numbers = parameters["include-numbers"]?.let { it.uppercase() == "ON" } ?: false

        val separator = parameters["separator"]?.first()?.toString() ?: "-"

        if (wordAmount > 50) return@get call.respond(
            HttpStatusCode.BadRequest, "Why Waste Time When Few Word Do Trick"
        )

        val textarea = buildHTMLString {
            textArea {
                id = "password-input"
                classes =
                    setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                +passwordService.getWords(wordAmount, language, spacialChars, numbers).joinToString(separator)
            }
        }
        call.respondText(textarea, ContentType.Text.Html)
    }

    // TODO: implement routing(get for base page and post to fetch and delete the entry + null handling)
    post("/share") {
        // TODO: implement method to create a share
    }

    get("/share") {
        // TODO: implement to get share page
    }

    post("/share/{shareId}/{salt}") {
        val shareId = getUUIDorNull(call.parameters["shareId"]) ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Invalid share ID format"
        )

        val salt = getUUIDorNull(call.parameters["salt"]) ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Invalid salt format"
        )

        val decryptedValue = passwordService.getShare(shareId, salt) ?: return@post call.respond(
            HttpStatusCode.NotFound, "Share not found or expired"
        )

        call.respondText(decryptedValue)
    }
}
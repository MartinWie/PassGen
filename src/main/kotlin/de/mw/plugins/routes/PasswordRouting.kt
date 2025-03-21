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

// TODO: add request parameters
fun Route.passwordRouting(){
    get("/word") {
        val parameters = call.queryParameters

        val language = parameters["language-select"]?.let { WordLanguage.valueOf(it) } ?: return@get call.respond(
            HttpStatusCode.BadRequest,"Missing language"
        )

        val wordAmount = parameters["word-amount-slider"]?.let { it.toInt() } ?:return@get call.respond(
            HttpStatusCode.BadRequest,"Missing amount"
        )

        if (wordAmount > 50) return@get call.respond(
            HttpStatusCode.BadRequest,"Why Waste Time When Few Word Do Trick"
        )

        val textarea = buildHTMLString {
            textArea {
                id = "password-input"
                classes = setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                +passwordService.getWords(wordAmount, language).joinToString("-")
            }
        }
        call.respondText(textarea, ContentType.Text.Html)
    }
}
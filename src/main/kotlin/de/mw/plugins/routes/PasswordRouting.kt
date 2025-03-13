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
        val textarea = buildHTMLString {
            textArea {
                id = "password-input"
                classes = setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                +passwordService.getWords(4, WordLanguage.GER).joinToString("-")
            }
        }
        call.respondText(textarea, ContentType.Text.Html)
    }
}
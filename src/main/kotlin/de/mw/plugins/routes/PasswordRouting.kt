package de.mw.plugins.routes

import de.mw.frontend.pages.getPasswordLoaded
import de.mw.frontend.pages.getShareCreateResult
import de.mw.frontend.pages.getSharePage
import de.mw.frontend.utils.JsEvent
import de.mw.frontend.utils.addJs
import de.mw.frontend.utils.buildHTMLString
import de.mw.frontend.utils.onEvent
import de.mw.models.WordLanguage
import de.mw.passwordService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.classes
import kotlinx.html.id
import kotlinx.html.p
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
                name = "password-input"
                classes =
                    setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px]")
                onEvent(
                    JsEvent.ON_INPUT, """
                            this.parentNode.dataset.clonedVal = this.value;
                            const lineCount = (this.value.match(/\n/g) || []).length + 1;
                            this.style.height = Math.min(675,Math.max(56, lineCount * 25)) + 'px';
                        """.trimIndent()
                )
                +passwordService.getWords(wordAmount, language, spacialChars, numbers).joinToString(separator)
            }
        }
        call.respondText(textarea, ContentType.Text.Html)
    }

    // Handle POST requests to create a new share
    post("/share") {
        val parameters = call.receiveParameters()
        val value = parameters["password-input"] ?: return@post call.respond(
            HttpStatusCode.BadRequest, buildHTMLString {
                p {
                    addJs("alert('Missing value to share');")
                }
            }
        )

        val viewCount = parameters["view-count"]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ONE

        val shareResult = passwordService.createShare(value, viewCount) ?: return@post call.respond(
            HttpStatusCode.BadRequest, buildHTMLString {
                p {
                    addJs("alert('Failed to create share - value too large');")
                }
            }
        )

        val (shareId, salt) = shareResult
        call.respondText(
            getShareCreateResult(shareId, salt),
            ContentType.Text.Html
        )
    }

    get("/share/{shareId}/{salt}") {
        val shareId = getUUIDorNull(call.parameters["shareId"]) ?: return@get call.respond(
            HttpStatusCode.BadRequest, "Invalid share ID format"
        )

        val salt = getUUIDorNull(call.parameters["salt"]) ?: return@get call.respond(
            HttpStatusCode.BadRequest, "Invalid salt format"
        )

        call.respondText(
            getSharePage(shareId, salt),
            ContentType.Text.Html
        )
    }

    // Fetch the info for a given share
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

        val share = getPasswordLoaded(decryptedValue)

        call.respondText(share, ContentType.Text.Html)
    }
}
package de.mw.plugins.routes

import de.mw.frontend.pages.getPasswordLoaded
import de.mw.frontend.pages.getShareCreateResult
import de.mw.frontend.pages.getSharePage
import de.mw.models.WordLanguage
import de.mw.passwordService
import de.mw.plugins.RateLimitTiers
import de.mw.plugins.respondHtmlWithCsp
import de.mw.plugins.respondHtmxError
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun Route.passwordRouting() {
    rateLimit(RateLimitTiers.GENERATE) {
        get("/wordlist") {
            val wordLists = passwordService.getWordLists(500)
            val payload =
                WordListResponse(
                    eng = wordLists[WordLanguage.ENG] ?: emptyList(),
                    ger = wordLists[WordLanguage.GER] ?: emptyList(),
                )
            call.respondText(
                Json.encodeToString(payload),
                ContentType.Application.Json,
            )
        }
    }

    rateLimit(RateLimitTiers.CREATE_SHARE) {
        // Handle POST requests to create a new share
        post("/share") {
            val parameters = call.receiveParameters()
            val value =
                parameters["password-input"] ?: return@post call.respondHtmxError(
                    "Missing value to share",
                )
            if (value.isBlank()) {
                return@post call.respondHtmxError("Password value must not be empty")
            }

            val viewCount = parameters["view-count"]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ONE

            val shareResult =
                passwordService.createShare(value, viewCount) ?: return@post call.respondHtmxError(
                    "Failed to create share — value too large",
                )

            val (shareId, salt) = shareResult
            call.respondText(
                getShareCreateResult(shareId, salt),
                ContentType.Text.Html,
            )
        }
    }

    rateLimit(RateLimitTiers.VIEW_SHARE) {
        get("/share/{shareId}/{salt}") {
            val shareId =
                getUUIDorNull(call.parameters["shareId"]) ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid share ID format",
                )

            val salt =
                getUUIDorNull(call.parameters["salt"]) ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid salt format",
                )

            call.respondHtmlWithCsp { getSharePage(shareId, salt) }
        }

        // Fetch the info for a given share
        post("/share/{shareId}/{salt}") {
            val shareId =
                getUUIDorNull(call.parameters["shareId"]) ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid share ID format",
                )

            val salt =
                getUUIDorNull(call.parameters["salt"]) ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid salt format",
                )

            val decryptedValue =
                passwordService.getShare(shareId, salt) ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    "Share not found, already viewed or expired",
                )

            val share = getPasswordLoaded(decryptedValue)

            call.respondText(share, ContentType.Text.Html)
        }
    }
}

@Serializable
private data class WordListResponse(
    val eng: List<String>,
    val ger: List<String>,
)

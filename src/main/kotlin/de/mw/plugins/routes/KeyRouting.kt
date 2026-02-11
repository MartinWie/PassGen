package de.mw.plugins.routes

import de.mw.frontend.pages.getKeyShareCompletedFragment
import de.mw.frontend.pages.getKeyShareCreateResult
import de.mw.frontend.pages.getKeySharePage
import de.mw.keyService
import de.mw.plugins.RateLimitTiers
import de.mw.plugins.respondHtmlWithCsp
import de.mw.plugins.respondHtmxError
import io.github.martinwie.htmx.buildHTMLString
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.div

fun Route.keyRouting() {
    rateLimit(RateLimitTiers.CREATE_SHARE) {
        // Handle POST requests to create a new PENDING key share (link creator flow)
        // Link creator specifies algorithm, purpose, and optional label
        post("/key/share") {
            val parameters = call.receiveParameters()

            val algorithm =
                parameters["algorithm"] ?: return@post call.respondHtmxError(
                    "Missing algorithm",
                )

            val purpose =
                parameters["purpose"] ?: return@post call.respondHtmxError(
                    "Missing purpose",
                )

            val label = parameters["label"]?.takeIf { it.isNotBlank() }

            val shareId =
                keyService.createPendingShare(algorithm, purpose, label)
                    ?: return@post call.respondHtmxError(
                        "Failed to create key share — invalid parameters",
                    )

            call.respondText(
                getKeyShareCreateResult(shareId),
                ContentType.Text.Html,
            )
        }
    }

    rateLimit(RateLimitTiers.VIEW_SHARE) {
        // Display the key share page (pending or completed view)
        get("/key/share/{shareId}") {
            val shareId =
                getUUIDorNull(call.parameters["shareId"]) ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid share ID format",
                )

            val share =
                keyService.getShare(shareId) ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    "Key share not found",
                )

            call.respondHtmlWithCsp { getKeySharePage(share) }
        }
    }

    rateLimit(RateLimitTiers.COMPLETE_SHARE) {
        // Complete a pending share by submitting the generated public key (recipient flow)
        post("/key/share/{shareId}/complete") {
            val shareId =
                getUUIDorNull(call.parameters["shareId"]) ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    buildHTMLString {
                        div("alert alert-error") {
                            +"Invalid share ID format"
                        }
                    },
                )

            val parameters = call.receiveParameters()

            val publicKey =
                parameters["public-key"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    buildHTMLString {
                        div("alert alert-error") {
                            +"Missing public key"
                        }
                    },
                )

            val algorithm =
                parameters["algorithm"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    buildHTMLString {
                        div("alert alert-error") {
                            +"Missing algorithm"
                        }
                    },
                )

            val success = keyService.completeShare(shareId, publicKey, algorithm)

            if (!success) {
                // Could be: share not found, already completed, or validation failed
                val share = keyService.getShare(shareId)
                val errorMessage =
                    when {
                        share == null -> "Share not found"
                        share.isCompleted() -> "This share has already been completed"
                        else -> "Invalid public key format or algorithm mismatch"
                    }
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    buildHTMLString {
                        div("alert alert-error") {
                            +errorMessage
                        }
                    },
                )
            }

            // Success - return the completed share view for HTMX to swap in
            val completedShare = keyService.getShare(shareId)!!
            call.respondText(
                getKeyShareCompletedFragment(completedShare),
                ContentType.Text.Html,
            )
        }
    }
}

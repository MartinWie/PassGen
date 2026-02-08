package de.mw.plugins.routes

import de.mw.frontend.pages.getKeyShareCompletedFragment
import de.mw.frontend.pages.getKeyShareCreateResult
import de.mw.frontend.pages.getKeySharePage
import de.mw.keyService
import io.github.martinwie.htmx.PageSecurityContext
import io.github.martinwie.htmx.addJs
import io.github.martinwie.htmx.buildHTMLString
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.div
import kotlinx.html.p
import java.security.SecureRandom
import java.util.*

fun Route.keyRouting() {
    // Handle POST requests to create a new PENDING key share (link creator flow)
    // Link creator specifies algorithm, purpose, and optional label
    post("/key/share") {
        val parameters = call.receiveParameters()

        val algorithm =
            parameters["algorithm"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                buildHTMLString {
                    p {
                        addJs("alert('Missing algorithm');")
                    }
                },
            )

        val purpose =
            parameters["purpose"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                buildHTMLString {
                    p {
                        addJs("alert('Missing purpose');")
                    }
                },
            )

        val label = parameters["label"]?.takeIf { it.isNotBlank() }

        val shareId =
            keyService.createPendingShare(algorithm, purpose, label)
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    buildHTMLString {
                        p {
                            addJs("alert('Failed to create key share - invalid parameters');")
                        }
                    },
                )

        call.respondText(
            getKeyShareCreateResult(shareId),
            ContentType.Text.Html,
        )
    }

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

        // Generate nonce for CSP
        val nonceBytes = ByteArray(16)
        SecureRandom().nextBytes(nonceBytes)
        val nonce = Base64.getEncoder().encodeToString(nonceBytes)
        PageSecurityContext.scriptNonce = nonce

        try {
            call.response.headers.append(
                "Content-Security-Policy",
                "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:3000; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; font-src 'self' data:; connect-src 'self' http://localhost:3000 ws://localhost:3000 wss://localhost:3000;",
            )
            call.respondText(
                getKeySharePage(share),
                ContentType.Text.Html,
            )
        } finally {
            PageSecurityContext.scriptNonce = null
        }
    }

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

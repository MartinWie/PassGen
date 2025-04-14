package de.mw.plugins.routes

import de.mw.frontend.pages.getSharePage
import de.mw.frontend.utils.*
import de.mw.models.WordLanguage
import de.mw.passwordService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

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
                    setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                +passwordService.getWords(wordAmount, language, spacialChars, numbers).joinToString(separator)
            }
        }
        call.respondText(textarea, ContentType.Text.Html)
    }

    // Handle POST requests to create a new share
    post("/share") {
        val parameters = call.receiveParameters()
        val value = parameters["password-input"] ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Missing value to share"
        )

        val viewCount = parameters["view-count"]?.toBigDecimalOrNull() ?: java.math.BigDecimal.ONE

        val shareResult = passwordService.createShare(value, viewCount) ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Failed to create share - value too large"
        )

        val (shareId, salt) = shareResult
        call.respondText(
            buildHTMLString {
                a {
                    classes = setOf("")
                    href = "/share/$shareId/$salt"
                }

                dialog {
                    id = "share_modal"
                    classes = setOf("modal")
                    div {
                        classes = setOf("modal-box")

                        div {
                            classes = setOf("p-3")
                            div {
                                classes = setOf("flex items-center gap-3 mb-4")
                                div {
                                    classes = setOf("bg-green-100 p-2 rounded-full")
                                    embedSvg("/static/svg/ok.svg")
                                }
                                h2 {
                                    classes = setOf("text-xl font-semibold")
                                    +"Password Shared"
                                }
                            }
                            p {
                                classes = setOf("mb-4")
                                +"Your password has been securely shared. Use this link:"
                            }
                            div {
                                classes = setOf("flex flex-col gap-3 mb-6")
                                // Link Container
                                div {
                                    classes =
                                        setOf("border border-gray-200 rounded-lg p-3 flex items-center justify-between")
                                    a {
                                        href = "/share/$shareId/$salt"
                                        classes = setOf("text-primary font-medium break-all hover:underline")
                                        +"/share/$shareId/$salt"
                                    }
                                    button {
                                        classes = setOf("btn btn-ghost gap-2 md:gap-3 ml-2")
                                        onEvent(JsEvent.ON_CLICK, "copyShareUrl();")
                                        embedSvg("/static/svg/copy.svg")
                                    }
                                }
                            }
                            div {
                                classes = setOf("p-3 rounded-lg text-sm")
                                ul {
                                    classes = setOf("list-disc list-inside space-y-1")
                                    li {
                                        +"This link can only be accessed once"
                                    }
                                    li {
                                        +"No account is required to view the password"
                                    }
                                }
                            }
                        }

                    }
                    form {
                        classes = setOf("modal-backdrop")
                        onEvent(
                            JsEvent.ON_SUBMIT, """
                            event.preventDefault();
                            document.getElementById('share_modal').close();
                        """.trimIndent()
                        )
                        button {
                            +"close"
                        }
                    }
                }
                div {
                    addJs("document.getElementById('share_modal').showModal()")
                }
            },
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

        call.respondText(decryptedValue)
    }
}
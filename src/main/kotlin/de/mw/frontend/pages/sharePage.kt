package de.mw.frontend.pages

import io.github.martinwie.htmx.*
import kotlinx.html.*
import java.util.*

fun getSharePage(
    shareId: UUID,
    salt: UUID,
): String =
    getBasePage("PassGen - Share") {
        div {
            classes = setOf("flex", "items-center", "justify-center", "min-h-screen", "p-4")
            div {
                id = "password-container"
                classes = setOf("border", "border-base-300", "rounded-xl", "p-6", "shadow-sm", "bg-base-100", "max-w-md", "w-full")

                // Header
                div("flex items-center gap-4 mb-6") {
                    div("bg-warning/20 p-4 rounded-full") {
                        attributes["aria-hidden"] = "true"
                        span("w-8 h-8 text-warning") {
                            embedSvg("/static/svg/lock.svg")
                        }
                    }
                    div {
                        h1("text-2xl font-bold text-base-content") {
                            +"Shared Password"
                        }
                        p("text-sm text-base-content/60 mt-0.5") {
                            +"Someone shared a password with you"
                        }
                    }
                }

                // Warning box
                div("bg-base-200/50 border border-warning/30 text-base-content p-4 rounded-lg mb-6") {
                    div("flex items-start gap-3") {
                        span("w-5 h-5 flex-shrink-0 mt-0.5 text-warning") {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/alert-info.svg")
                        }
                        div {
                            p("font-medium mb-1") {
                                +"One-Time View"
                            }
                            p("text-sm text-base-content/70") {
                                +"This password can only be viewed once. After you leave this page, it cannot be retrieved again."
                            }
                        }
                    }
                }

                // View button
                button {
                    classes = setOf("btn", "btn-primary", "w-full", "gap-2")
                    hxPost("/share/$shareId/$salt")
                    hxTarget("#password-container")

                    embedSvg("/static/svg/reveal.svg")
                    +"View Password"
                }
            }
        }
    }

fun getPasswordLoaded(decryptedValue: String): String =
    buildHTMLString {
        div {
            classes = setOf("w-full", "max-w-md", "mx-auto")
            div {
                classes = setOf("border", "border-base-300", "rounded-xl", "p-6", "shadow-sm", "bg-base-100")

                // Header
                div("flex items-center gap-4 mb-6") {
                    div("bg-success/20 p-4 rounded-full") {
                        attributes["aria-hidden"] = "true"
                        span("w-8 h-8 text-success") {
                            embedSvg("/static/svg/ok.svg")
                        }
                    }
                    div {
                        h1("text-2xl font-bold text-base-content") {
                            +"Password Retrieved"
                        }
                        p("text-sm text-base-content/60 mt-0.5") {
                            +"Click to reveal or copy"
                        }
                    }
                }

                // Password Display Container
                div("mb-6") {
                    label("label py-1") {
                        span("label-text font-medium") { +"Password" }
                    }
                    div("relative") {
                        // Masked password display (clickable to reveal)
                        div {
                            id = "password-field-placeholder"
                            classes =
                                setOf(
                                    "flex",
                                    "items-center",
                                    "justify-center",
                                    "h-16",
                                    "bg-base-200/30",
                                    "border",
                                    "border-base-300",
                                    "rounded-lg",
                                    "cursor-pointer",
                                    "hover:bg-base-200/50",
                                    "transition-colors",
                                    "font-mono",
                                    "tracking-widest",
                                )
                            onEvent(JsEvent.ON_CLICK, "document.getElementById('view_share_modal').showModal()")
                            +"* * * * * * * * * * * *"
                        }
                    }
                }

                // Action buttons
                div("flex gap-3 mb-6") {
                    button(classes = "btn btn-primary flex-1") {
                        id = "reveal-btn"
                        title = "Reveal password"
                        onEvent(JsEvent.ON_CLICK, "document.getElementById('view_share_modal').showModal()")
                        embedSvg("/static/svg/reveal.svg")
                        span("ml-1") { +"Reveal" }
                    }
                    button(classes = "btn btn-outline flex-1") {
                        title = "Copy to clipboard"
                        onEvent(JsEvent.ON_CLICK, "copyToClipboard('password-field');")
                        embedSvg("/static/svg/copy.svg")
                        span("ml-1") { +"Copy" }
                    }
                }

                // Security Information
                div("bg-base-200/50 border border-info/30 text-base-content p-4 rounded-lg") {
                    div("flex items-start gap-3") {
                        span("w-5 h-5 flex-shrink-0 mt-0.5 text-info") {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/alert-info.svg")
                        }
                        div {
                            p("font-medium mb-1") {
                                +"Security Notice"
                            }
                            p("text-sm text-base-content/70") {
                                +"This password is only available for viewing once. After you leave this page, it cannot be retrieved again."
                            }
                        }
                    }
                }
            }
        }

        // Modal outside the main container - dialog works at buildHTMLString level
        dialog {
            id = "view_share_modal"
            attributes["aria-labelledby"] = "view-share-modal-title"
            attributes["aria-modal"] = "true"
            classes = setOf("modal")
            div {
                classes = setOf("modal-box", "max-w-2xl")
                h3("font-bold text-lg mb-4") {
                    id = "view-share-modal-title"
                    +"Your Password"
                }
                div("bg-base-200/50 rounded-lg p-4") {
                    p {
                        id = "password-field"
                        classes = setOf("font-mono", "text-lg", "whitespace-pre-wrap", "break-all")
                        +decryptedValue
                    }
                }
                div("modal-action") {
                    button(classes = "btn btn-ghost") {
                        onEvent(JsEvent.ON_CLICK, "copyToClipboard('password-field');")
                        span {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/copy.svg")
                        }
                        span("ml-1") { +"Copy" }
                    }
                    form {
                        onEvent(
                            JsEvent.ON_SUBMIT,
                            """
                            event.preventDefault();
                            document.getElementById('view_share_modal').close();
                            """.trimIndent(),
                        )
                        button(classes = "btn") {
                            +"Close"
                        }
                    }
                }
            }
            form {
                classes = setOf("modal-backdrop")
                onEvent(
                    JsEvent.ON_SUBMIT,
                    """
                    event.preventDefault();
                    document.getElementById('view_share_modal').close();
                    """.trimIndent(),
                )
                button {
                    attributes["aria-label"] = "Close dialog"
                    +"close"
                }
            }
        }
    }

fun getShareCreateResult(
    shareId: UUID,
    salt: UUID,
) = buildHTMLString {
    // Hidden anchor used by copyShareUrl() in app.js to resolve the full share URL.
    // aria-hidden + tabIndex -1 prevents screen readers and keyboard from reaching it.
    a {
        id = "password-share-link"
        attributes["aria-hidden"] = "true"
        attributes["tabindex"] = "-1"
        classes = setOf("hidden")
        href = "/share/$shareId/$salt"
    }

    dialog {
        id = "share_modal"
        attributes["aria-labelledby"] = "share-modal-title"
        attributes["aria-modal"] = "true"
        classes = setOf("modal")
        div {
            classes = setOf("modal-box")
            div {
                classes = setOf("flex items-center justify-center gap-3 mb-3")
                div {
                    classes = setOf("bg-success/20 p-3 rounded-full")
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/ok.svg")
                }
                h2 {
                    id = "share-modal-title"
                    classes = setOf("text-xl font-semibold")
                    +"Password Shared"
                }
            }
            div {
                classes = setOf("flex flex-col gap-3 mb-6 mt-6")
                // Link Container
                div {
                    classes =
                        setOf("border border-base-300 rounded-lg p-3 flex items-center justify-between")
                    a {
                        href = "/share/$shareId/$salt"
                        classes = setOf("text-primary font-medium break-all hover:underline")
                        +"/share/$shareId/$salt"
                    }
                    button {
                        classes = setOf("btn btn-ghost gap-3 ml-2")
                        onEvent(JsEvent.ON_CLICK, "copyShareUrl();")
                        span {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/copy.svg")
                        }
                    }
                }
            }
            div {
                classes = setOf("rounded-lg text-sm")
                p {
                    +"The password can only be viewed once!"
                }
            }
        }
        form {
            classes = setOf("modal-backdrop")
            onEvent(
                JsEvent.ON_SUBMIT,
                """
                event.preventDefault();
                document.getElementById('share_modal').close();
                """.trimIndent(),
            )
            button {
                attributes["aria-label"] = "Close dialog"
                +"close"
            }
        }
    }
}

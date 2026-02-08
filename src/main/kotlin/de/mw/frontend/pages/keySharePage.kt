package de.mw.frontend.pages

import de.mw.models.SharePublicKey
import io.github.martinwie.htmx.JsEvent
import io.github.martinwie.htmx.buildHTMLString
import io.github.martinwie.htmx.embedSvg
import io.github.martinwie.htmx.onEvent
import kotlinx.html.*
import java.util.*

fun getKeySharePage(share: SharePublicKey): String =
    getBasePage("PassGen - ${if (share.isPending()) "Generate Key" else "Shared Public Key"}") {
        div {
            classes = setOf("flex items-center justify-center min-h-screen p-4")
            div {
                classes = setOf("w-full max-w-2xl")
                div {
                    id = "share-content"
                    if (share.isPending()) {
                        // PENDING STATE: Show key generation UI for recipient
                        getKeySharePendingContent(share)
                    } else {
                        // COMPLETED STATE: Show the public key
                        getKeyShareCompletedContent(share)
                    }
                }
            }
        }
    }

/**
 * Content for a PENDING share - shows generation UI for the recipient.
 * The recipient will generate the key pair locally and only the public key is sent to the server.
 */
private fun FlowContent.getKeySharePendingContent(share: SharePublicKey) {
    div {
        classes = setOf("border", "border-base-300", "rounded-xl", "p-6", "shadow-sm", "bg-base-100")
        attributes["data-testid"] = "share-pending-content"

        // Header
        div("flex items-center gap-4 mb-6") {
            div("bg-warning/20 p-3 rounded-full") {
                attributes["aria-hidden"] = "true"
                span("w-6 h-6 text-warning") {
                    embedSvg("/static/svg/key.svg")
                }
            }
            div {
                h1("text-2xl font-bold text-base-content") {
                    +"Generate Your Key"
                }
                p("text-sm text-base-content/60 mt-0.5") {
                    val algoDisplay = formatAlgorithm(share.algorithm)
                    val purposeDisplay = if (share.purpose == "git") "Git Signing" else "SSH"
                    +"$algoDisplay • $purposeDisplay"
                    if (share.label != null) {
                        +" • ${share.label}"
                    }
                }
            }
        }

        // Info box explaining the flow
        div("bg-base-200/50 border border-base-300 text-base-content p-4 rounded-lg mb-6") {
            div("flex items-start gap-3") {
                span("w-5 h-5 flex-shrink-0 mt-0.5 text-info") {
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/alert-info.svg")
                }
                div {
                    p("font-medium mb-2") {
                        +"How This Works"
                    }
                    ul("list-disc list-inside space-y-1 text-sm text-base-content/70") {
                        li { +"Click the button below to generate a key pair on your device" }
                        li { +"Your private key will be downloaded automatically - keep it safe!" }
                        li { +"Only the public key will be shared (private key never leaves your device)" }
                    }
                }
            }
        }

        // Error alert (hidden by default)
        div("alert alert-error py-2 mb-4 rounded-xl hidden") {
            id = "share-key-error"
            span {
                id = "share-key-error-text"
                classes = setOf("text-sm")
            }
        }

        // Generate button
        div("flex justify-center") {
            button(classes = "btn btn-primary btn-lg gap-2") {
                id = "generate-share-key-btn"
                attributes["data-testid"] = "generate-share-key-btn"
                span {
                    id = "share-keygen-icon"
                    embedSvg("/static/svg/regen.svg")
                }
                +"Generate Key Pair"
            }
        }

        // Hidden inputs with share data for JavaScript
        input(InputType.hidden) {
            id = "share-id"
            value = share.id.toString()
        }
        input(InputType.hidden) {
            id = "share-algorithm"
            value = share.algorithm
        }
        input(InputType.hidden) {
            id = "share-purpose"
            value = share.purpose
        }
        input(InputType.hidden) {
            id = "share-label"
            value = share.label ?: ""
        }

        // Result container for HTMX swap after completion
        div {
            id = "share-complete-result"
        }
    }
}

/**
 * Content for a COMPLETED share - shows the public key.
 */
private fun FlowContent.getKeyShareCompletedContent(share: SharePublicKey) {
    div {
        classes = setOf("border", "border-base-300", "rounded-xl", "p-6", "shadow-sm", "bg-base-100")
        attributes["data-testid"] = "share-completed-content"

        // Header
        div("flex items-center gap-4 mb-6") {
            div("bg-success/20 p-4 rounded-full") {
                attributes["aria-hidden"] = "true"
                span("w-8 h-8 text-success") {
                    embedSvg("/static/svg/key.svg")
                }
            }
            div {
                h1("text-2xl font-bold text-base-content") {
                    +"Shared Public Key"
                }
                p("text-sm text-base-content/60 mt-0.5") {
                    val algoDisplay = formatAlgorithm(share.algorithm)
                    val purposeDisplay = if (share.purpose == "git") "Git Signing" else "SSH"
                    +"$algoDisplay • $purposeDisplay"
                    if (share.label != null) {
                        +" • ${share.label}"
                    }
                }
            }
        }

        // Public Key Display
        div("mb-6") {
            label("label py-1") {
                span("label-text font-medium") { +"Public Key" }
            }
            textArea {
                id = "public-key-display"
                attributes["data-testid"] = "public-key-display"
                attributes["aria-label"] = "Public Key"
                classes =
                    setOf(
                        "textarea",
                        "textarea-bordered",
                        "font-mono",
                        "text-xs",
                        "leading-relaxed",
                        "h-32",
                        "w-full",
                        "bg-base-200/30",
                    )
                attributes["readonly"] = "readonly"
                +(share.publicKey ?: "")
            }
        }

        // Action buttons - Download and Copy side by side
        div("flex gap-3") {
            button(classes = "btn btn-primary flex-1") {
                id = "download-public-btn"
                onEvent(JsEvent.ON_CLICK, "downloadSharePublicKey();")
                span {
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/download.svg")
                }
                span("ml-1") { +"Download" }
            }
            button(classes = "btn btn-outline flex-1") {
                title = "Copy public key"
                onEvent(JsEvent.ON_CLICK, "copyToClipboard('public-key-display');")
                span {
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/copy.svg")
                }
                span("ml-1") { +"Copy" }
            }
        }

        // Hidden data for JS (used by downloadSharePublicKey in app.js)
        input(InputType.hidden) {
            id = "share-algorithm"
            value = share.algorithm
        }
        input(InputType.hidden) {
            id = "share-purpose"
            value = share.purpose
        }
    }
}

/**
 * HTML fragment returned after successfully completing a share.
 * Used by HTMX to replace the pending UI.
 */
fun getKeyShareCompletedFragment(share: SharePublicKey): String =
    buildHTMLString {
        div {
            classes = setOf("border", "border-base-300", "rounded-xl", "p-6", "shadow-sm", "bg-base-100")
            attributes["data-testid"] = "share-completed-content"

            // Success header
            div("flex items-center gap-4 mb-6") {
                div("bg-success/20 p-4 rounded-full") {
                    attributes["aria-hidden"] = "true"
                    span("w-8 h-8 text-success") {
                        embedSvg("/static/svg/ok.svg")
                    }
                }
                div {
                    h1("text-2xl font-bold text-base-content") {
                        +"Key Generated Successfully!"
                    }
                    p("text-sm text-base-content/60 mt-0.5") {
                        val algoDisplay = formatAlgorithm(share.algorithm)
                        val purposeDisplay = if (share.purpose == "git") "Git Signing" else "SSH"
                        +"$algoDisplay • $purposeDisplay"
                        if (share.label != null) {
                            +" • ${share.label}"
                        }
                    }
                }
            }

            // Public Key Display
            div("mb-6") {
                label("label py-1") {
                    span("label-text font-medium") { +"Public Key (now shared)" }
                }
                textArea {
                    id = "public-key-display"
                    attributes["data-testid"] = "public-key-display"
                    attributes["aria-label"] = "Public Key"
                    classes =
                        setOf(
                            "textarea",
                            "textarea-bordered",
                            "font-mono",
                            "text-xs",
                            "leading-relaxed",
                            "h-32",
                            "w-full",
                            "bg-base-200/30",
                        )
                    attributes["readonly"] = "readonly"
                    +(share.publicKey ?: "")
                }
            }

            // Action buttons - Download and Copy side by side
            div("flex gap-3") {
                button(classes = "btn btn-primary flex-1") {
                    id = "download-public-btn"
                    onEvent(JsEvent.ON_CLICK, "downloadSharePublicKey();")
                    span {
                        attributes["aria-hidden"] = "true"
                        embedSvg("/static/svg/download.svg")
                    }
                    span("ml-1") { +"Download" }
                }
                button(classes = "btn btn-outline flex-1") {
                    title = "Copy public key"
                    onEvent(JsEvent.ON_CLICK, "copyToClipboard('public-key-display');")
                    span {
                        attributes["aria-hidden"] = "true"
                        embedSvg("/static/svg/copy.svg")
                    }
                    span("ml-1") { +"Copy" }
                }
            }

            // Hidden data for JS (used by downloadSharePublicKey in app.js)
            input(InputType.hidden) {
                id = "share-algorithm"
                value = share.algorithm
            }
            input(InputType.hidden) {
                id = "share-purpose"
                value = share.purpose
            }
        }
    }

/**
 * Helper to format algorithm name for display.
 */
private fun formatAlgorithm(algorithm: String): String =
    when (algorithm) {
        "ed25519" -> "Ed25519"
        "ecdsa-p256" -> "ECDSA P-256"
        "ecdsa-p384" -> "ECDSA P-384"
        "rsa-2048" -> "RSA 2048"
        "rsa-4096" -> "RSA 4096"
        else -> algorithm
    }

fun getKeyShareCreateResult(shareId: UUID) =
    buildHTMLString {
        dialog {
            id = "key_share_modal"
            attributes["data-testid"] = "key-share-modal"
            attributes["aria-labelledby"] = "key-share-modal-title"
            attributes["aria-modal"] = "true"
            classes = setOf("modal")
            div {
                classes = setOf("modal-box", "max-w-2xl")

                // Header with success icon
                div {
                    classes = setOf("flex", "flex-col", "items-center", "text-center", "mb-6")
                    div {
                        classes = setOf("bg-success/20", "p-4", "rounded-full", "mb-3")
                        attributes["aria-hidden"] = "true"
                        span("w-8 h-8 text-success") {
                            embedSvg("/static/svg/ok.svg")
                        }
                    }
                    h2 {
                        id = "key-share-modal-title"
                        classes = setOf("text-2xl", "font-bold", "text-base-content")
                        +"Share Link Created"
                    }
                    p("text-sm text-base-content/60 mt-1") {
                        +"Send this link to the person who needs a key"
                    }
                }

                // Link Container - prominent display
                div("mb-6") {
                    label("label py-1") {
                        span("label-text font-medium text-sm") { +"Share Link" }
                    }
                    div {
                        classes = setOf(
                            "flex",
                            "items-center",
                            "gap-2",
                            "bg-base-200/50",
                            "border",
                            "border-base-300",
                            "rounded-lg",
                            "p-3"
                        )
                        div("flex-1 min-w-0") {
                            a {
                                id = "key-share-link"
                                attributes["data-testid"] = "key-share-link"
                                href = "/key/share/$shareId"
                                classes = setOf("text-primary", "font-mono", "text-sm", "break-all", "hover:underline")
                                +"/key/share/$shareId"
                            }
                        }
                        button {
                            classes = setOf("btn", "btn-ghost", "ml-2")
                            onEvent(JsEvent.ON_CLICK, "copyKeyShareUrl();")
                            span {
                                attributes["aria-hidden"] = "true"
                                embedSvg("/static/svg/copy.svg")
                            }
                        }
                    }
                }

                // How it works - explanation
                div("bg-base-200/50 border border-base-300 text-base-content p-4 rounded-lg mb-4 text-left") {
                    div("flex items-start gap-3") {
                        span("w-5 h-5 flex-shrink-0 mt-0.5 text-info") {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/alert-info.svg")
                        }
                        div {
                            p("font-medium mb-2") {
                                +"How It Works"
                            }
                            ol("list-decimal list-inside space-y-1 text-sm text-base-content/70") {
                                li { +"Recipient opens this link" }
                                li { +"Key pair is generated on their device" }
                                li { +"Private key downloads automatically (never sent to server)" }
                                li { +"Public key is saved and visible at this link" }
                            }
                        }
                    }
                }

                // Security note
                div("bg-base-200/50 border border-warning/30 text-base-content p-4 rounded-lg text-left") {
                    div("flex items-center gap-3") {
                        span("w-5 h-5 flex-shrink-0 text-warning") {
                            attributes["aria-hidden"] = "true"
                            embedSvg("/static/svg/alert-info.svg")
                        }
                        p("text-sm text-base-content/70") {
                            strong("text-base-content") { +"Security: " }
                            +"The private key never leaves the recipient's device."
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
                    document.getElementById('key_share_modal').close();
                    """.trimIndent(),
                )
                button {
                    attributes["aria-label"] = "Close dialog"
                    +"close"
                }
            }
        }
        // copyKeyShareUrl() is defined in app.js (centralized with clipboard fallback).
        // Modal is opened by the htmx:afterSwap handler in app.js when this fragment is swapped in.
    }

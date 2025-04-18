package de.mw.frontend.pages

import de.mw.frontend.utils.*
import kotlinx.html.*
import java.util.*

fun getSharePage(shareId: UUID, salt: UUID): String {
    return getBasePage("PassGen - Share") {
        div {
            classes = setOf("flex items-center justify-center min-h-screen p-3")
            div {
                id = "password-container"
                classes = setOf("border border-gray-200 rounded-xl p-3 shadow-xs text-center")
                h2 {
                    classes = setOf("text-xl font-semibold mb-3")
                    +"View Shared Password"
                }
                div {
                    classes = setOf("bg-amber-50 text-amber-800 p-3 rounded-lg mb-6 text-sm text-left")
                    p {
                        strong {
                            +"Warning:"
                        }
                        +"This password can only be viewed once."
                    }
                }
                button {
                    classes = setOf("btn w-full mb-3")
                    hxPost("/share/$shareId/$salt")
                    hxTarget("#password-container")

                    embedSvg("/static/svg/reveal.svg")
                    +"View Password"
                }
            }
        }
    }
}

fun getPasswordLoaded(decryptedValue: String): String {
    return buildHTMLString {
        div {
            classes = setOf("w-full max-w-md mx-auto")
            div {
                classes = setOf("rounded-xl shadow-md p-6")
                h1 {
                    classes = setOf("text-2xl font-semibold mb-3")
                    +"Shared Password"
                }
                // Password Display Container
                div {
                    classes = setOf("mb-6")
                    div {
                        classes = setOf("flex flex-col gap-3")
                        label {
                            classes = setOf("text-sm font-medium")
                            +"Password"
                        }
                        div {
                            classes = setOf("flex items-stretch gap-3")
                            // Password Display Field (Read-Only)
                            div {
                                classes = setOf("flex-grow border border-gray-200 rounded-lg p-3 relative")
                                p {
                                    id = "password-field"
                                    classes = setOf("password-field text-lg select-none")
                                    +"••••••••••••••••"
                                }
                                // Hidden password storage
                                input {
                                    type = InputType.hidden
                                    id = "hidden-password"
                                    value = decryptedValue
                                }
                            }
                            // Button Group
                            div("flex flex-col gap-3") {
                                // Reveal Button
                                button {
                                    id = "reveal-btn"
                                    classes = setOf("btn btn-ghost")
                                    title = "Reveal password"
                                    embedSvg("/static/svg/reveal.svg")

                                }
                                // Copy Button
                                div("relative") {
                                    button {
                                        classes = setOf("btn btn-ghost")
                                        title = "Copy to clipboard"
                                        embedSvg("/static/svg/copy.svg")
                                    }
                                    // Copy Success Tooltip
                                    div {
                                        id = "copy-tooltip"
                                        classes =
                                            setOf("hidden absolute bottom-full right-0 mb-3 px-3 py-1 text-xs rounded shadow-lg whitespace-nowrap")
                                        +"Copied!"
                                    }
                                }
                            }
                        }
                    }
                }
                // Information
                div {
                    classes = setOf("bg-blue-50 text-blue-800 p-3 rounded-lg text-sm")
                    div {
                        classes = setOf("flex items-start")
                        embedSvg("/static/svg/alert-info.svg")
                        div {
                            p {
                                classes = setOf("font-medium mb-1")
                                +"Security Information"
                            }
                            p {
                                +"This password will only be available for viewing once. After you leave this page, it cannot be retrieved again."
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getShareCreateResult(shareId: UUID, salt: UUID) =
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
                    classes = setOf("flex items-center justify-center gap-3 mb-3")
                    div {
                        classes = setOf("bg-green-100 p-3 rounded-full")
                        embedSvg("/static/svg/ok.svg")
                    }
                    h2 {
                        classes = setOf("text-xl font-semibold")
                        +"Password Shared"
                    }
                }
                div {
                    classes = setOf("flex flex-col gap-3 mb-6 mt-6")
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
                            classes = setOf("btn btn-ghost gap-3 ml-2")
                            onEvent(JsEvent.ON_CLICK, "copyShareUrl();")
                            embedSvg("/static/svg/copy.svg")
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
    }
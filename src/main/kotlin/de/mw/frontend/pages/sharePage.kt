package de.mw.frontend.pages

import de.mw.frontend.utils.buildHTMLString
import de.mw.frontend.utils.embedSvg
import kotlinx.html.*
import java.util.*

fun getSharePage(shareId: UUID, salt: UUID): String {
    return getBasePage("PassGen - Share") {
        div {
            classes = setOf("w-full max-w-md mx-auto")
            div {
                id = "password-container"
                classes = setOf("bg-white rounded-lg shadow p-6 text-center")
                h2 {
                    classes = setOf("text-xl font-semibold mb-4")
                    +"Shared Password"
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
                    classes = setOf("btn btn-primary w-full")
                    +"Reveal Password"
                }
            }
        }
    }
}

fun getPasswordLoaded(shareId: UUID, salt: UUID): String {
    return buildHTMLString {
        div {
            classes = setOf("w-full max-w-md mx-auto")
            div {
                classes = setOf("bg-white rounded-xl shadow-md p-6")
                h1 {
                    classes = setOf("text-2xl font-semibold text-gray-800 mb-2")
                    +"Shared Password"
                }
                p {
                    classes = setOf("text-gray-600 mb-6")
                    +"Someone has shared a secure password with you."
                }
                // Password Display Container
                div {
                    classes = setOf("mb-6")
                    div {
                        classes = setOf("flex flex-col gap-2")
                        label {
                            classes = setOf("text-sm font-medium text-gray-700")
                            +"Password"
                        }
                        div {
                            classes = setOf("flex items-stretch gap-2")
                            // Password Display Field (Read-Only)
                            div {
                                classes = setOf("flex-grow bg-gray-50 border border-gray-200 rounded-lg p-3 relative")
                                p {
                                    id = "password-field"
                                    classes = setOf("password-field text-lg text-gray-800 select-none")
                                    +"••••••••••••••••"
                                }
                                // Hidden password storage
                                input {
                                    type = InputType.hidden
                                    id = "hidden-password"
                                    value = ""
                                }
                            }
                            // Button Group
                            div {
                                classes = setOf("flex flex-col gap-2")
                                // Reveal Button
                                button {
                                    id = "reveal-btn"
                                    classes = setOf("btn btn-ghost")
                                    title = "Reveal password"
                                    embedSvg("/static/svg/reveal.svg")

                                }
                                // Copy Button
                                div {
                                    classes = setOf("relative")
                                    button {
                                        classes = setOf("btn btn-ghost")
                                        title = "Copy to clipboard"
                                        embedSvg("/static/svg/copy.svg")
                                    }
                                    // Copy Success Tooltip
                                    div {
                                        id = "copy-tooltip"
                                        classes =
                                            setOf("hidden absolute bottom-full right-0 mb-2 px-3 py-1 bg-gray-800 text-white text-xs rounded shadow-lg whitespace-nowrap")
                                        +"Copied!"
                                    }
                                }
                            }
                        }
                    }
                }
                // Information
                div {
                    classes = setOf("bg-blue-50 text-blue-800 p-4 rounded-lg text-sm")
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
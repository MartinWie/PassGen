package de.mw.frontend.pages

import de.mw.frontend.utils.buildHTMLString
import de.mw.frontend.utils.getFooter
import de.mw.frontend.utils.getPageHead
import de.mw.utils.HtmxExtension
import de.mw.utils.hxExt
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String {
    return getBasePage(pageTitle) {
        div("flex items-center justify-center min-h-screen p-4") {
            div("w-full max-w-xl") {
                div("flex items-center gap-2 border border-gray-200 rounded-lg p-2 focus-within:ring-1 focus-within:ring-primary focus-within:border-primary") {
                    textArea {
                        classes = setOf("flex-grow resize-none h-12 min-h-[48px] border-none focus:outline-none text-gray-700 bg-transparent p-2 leading-[1.5] pt-[0.75rem] pb-[0.75rem] box-border")
                        placeholder = ""
                    }
                    div("flex gap-2") {
                        // Settings
                        div("dropdown dropdown-end") {
                            label {
                                tabIndex = "0"
                                classes = setOf("btn btn-sm btn-ghost")
                                title = "Settings"
                                unsafe {
                                    +"""
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    </svg>
                                    """.trimIndent()
                                }
                            }
                            div("dropdown-content z-[1] menu p-4 shadow bg-base-100 rounded-box w-64 text-sm") {
                                h3 {
                                    classes = setOf("font-medium mb-2")
                                    +"Settings"
                                }
                                // Language picker
                                div("form-control mb-2") {
                                    label {
                                        classes = setOf("label py-1")
                                        span {
                                            classes = setOf("label-text text-xs")
                                            +"Language"
                                        }
                                    }
                                    select {
                                        id = "model-select"
                                        classes = setOf("select select-sm select-bordered w-full")
                                        option {
                                            value = "ENG"
                                            +"English"
                                        }
                                        option {
                                            value = "GER"
                                            +"Deutsch"
                                        }
                                    }
                                }
                                // Amount slider
                                div("form-control mb-2") {
                                    // TODO: add
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getBasePage(
    pageTitle: String,
    bodyTags: TagConsumer<StringBuilder>.() -> Unit
): String {
    return "<!DOCTYPE html>" + buildHTMLString {
        getPageHead(pageTitle)

        body {
            classes = setOf(
                "min-h-screen flex flex-col"
            )

            hxExt(HtmxExtension.LOADING_STATES)
            // Saas Navbar

            div {
                classes = setOf("flex-grow")
                bodyTags()
            }

            getFooter()
        }
    }
}
package de.mw.frontend.pages

import de.mw.frontend.utils.*
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String {
    return getBasePage(pageTitle) {
        div("flex items-center justify-center min-h-screen p-4 md:p-6") {
            div("w-full max-w-3xl mx-auto") {
                div("flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-gray-200 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-primary focus-within:border-primary shadow-xs") {
                    textArea {
                        id = "password-input"
                        classes = setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                        placeholder = "Type or generate content..."
                    }

                    div("flex justify-center md:justify-end gap-2 md:gap-3 mt-1 md:mt-0") {
                        // Settings Dropdown
                        div("dropdown dropdown-top md:dropdown-end") {
                            label {
                                tabIndex = "0"
                                classes = setOf("btn", "btn-ghost")
                                title = "Settings"
                                unsafe {
                                    +"""
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                        </svg>
                                    """.trimIndent()
                                }
                            }
                            div("dropdown-content z-1 menu p-4 shadow-lg bg-base-100 rounded-xl w-64 md:w-72 text-base") {
                                tabIndex = "0"
                                h3 {
                                    classes = setOf("font-medium", "mb-3", "text-lg")
                                    +"Settings"
                                }

                                // Language Selection
                                div("form-control mb-3") {
                                    label {
                                        classes = setOf("label", "py-1")
                                        span {
                                            classes = setOf("label-text", "text-sm")
                                            +"Language"
                                        }
                                    }
                                    select {
                                        id = "language-select"
                                        classes = setOf("select", "select-bordered", "w-full")
                                        option {
                                            value = "ENG"
                                            selected = true
                                            +"English"
                                        }
                                        option {
                                            value = "GER"
                                            +"Deutsch"
                                        }
                                    }
                                }

                                // Amount Slider
                                div("form-control mb-3") {
                                    label {
                                        classes = setOf("label", "py-1")
                                        span {
                                            classes = setOf("label-text", "text-sm")
                                            +"Words:"
                                            span {
                                                id = "word-amount"
                                                +"4"
                                            }
                                        }
                                    }
                                    input(InputType.range) {
                                        min = "1"
                                        max = "20"
                                        step = "1"
                                        value = "4"
                                        id = "word-amount-slider"
                                        classes = setOf("range mb-3")
                                        attributes["oninput"] = """
                                            document.getElementById('word-amount').textContent = this.value
                                            document.getElementById('word-input').value = this.value
                                        """.trimIndent()
                                    }
                                    input(InputType.number) {
                                        min = "1"
                                        max = "50"
                                        value = "4"
                                        id = "word-input"
                                        classes = setOf("input", "input-bordered", "w-full")
                                        attributes["oninput"] = """
                                            document.getElementById('word-amount').textContent = this.value;
                                            document.getElementById('word-amount-slider').value = this.value;
                                        """.trimIndent()
                                    }
                                }
                            }
                        }

                        // Generate Button
                        button(classes = "btn btn-ghost") {
                            hxGet("/word")
                            hxTrigger("click")
                            hxTarget("#password-input")
                            hxSwap(HxSwapOption.OUTER_HTML)
                            hxInclude("[id='language-select'], [id='word-amount']") // TODO: check if "id" works, if not use name= and add names
                            title = "Generate"
                            unsafe {
                                +"""
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                                    </svg>
                                """.trimIndent()
                            }
                        }

                        // Share Button
                        button(classes = "btn btn-ghost") {
                            title = "Share"
                            unsafe {
                                +"""
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
                                        <path d="M15 8a3 3 0 10-2.977-2.63l-4.94 2.47a3 3 0 100 4.319l4.94 2.47a3 3 0 10.895-1.789l-4.94-2.47a3.027 3.027 0 000-.74l4.94-2.47C13.456 7.68 14.19 8 15 8z" />
                                    </svg>
                                """.trimIndent()
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

            div {
                classes = setOf("grow")
                bodyTags()
            }

            getFooter()
        }
    }
}
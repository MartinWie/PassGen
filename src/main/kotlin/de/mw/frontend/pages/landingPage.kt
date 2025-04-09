package de.mw.frontend.pages

import de.mw.frontend.utils.*
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String {
    return getBasePage(pageTitle) {
        div("flex items-center justify-center min-h-screen p-4 md:p-6") {

            div {
                classes = setOf("toast toast-top toast-center")
                div {
                    id = "copy-tooltip"
                    classes = setOf("alert alert-success animate-bounce fade hidden")
                    span {
                        +"Copy successful!"
                    }
                }
                div {
                    id = "copy-tooltip-failed"
                    classes = setOf("alert alert-warning animate-bounce fade hidden")
                    span {
                        +"Copy failed :("
                    }
                }
            }

            div("w-full max-w-3xl mx-auto") {
                div("flex flex-col md:flex-row items-stretch md:items-center gap-2 md:gap-3 border border-gray-200 rounded-xl p-2 md:p-3 focus-within:ring-1 focus-within:ring-primary focus-within:border-primary shadow-xs") {
                    textArea { // Note: when modifying this one do not forget the copy of this in the endpoint
                        id = "password-input"
                        classes =
                            setOf("grow resize-none h-14 min-h-[56px] border-none focus:outline-hidden bg-transparent px-2 box-border text-base align-middle leading-[1.5] py-[14px] md:py-[14px]")
                        hxGet("/word")
                        hxInclude("[name='language-select'], [name='word-amount-slider'], [name='include-special'], [name='include-numbers'], [name='separator']")
                        hxSwap(HxSwapOption.OUTER_HTML)
                        hxTrigger("intersect once")
                    }

                    div("flex justify-center md:justify-end gap-2 md:gap-3 mt-1 md:mt-0") {
                        // Copy button
                        button(classes = "btn btn-ghost") {
                            title = "Copy to clipboard"
                            attributes["onclick"] = "copyToClipboard()"
                            embedSvg("/static/svg/copy.svg")
                        }

                        // Generate Button
                        button(classes = "btn btn-ghost animate-spin-reverse") {
                            id = "regen-button"
                            hxGet("/word")
                            hxTrigger("click")
                            //hxDisabled("#regen-button")
                            hxSync("this", SyncModifier.REPLACE)
                            hxTarget("#password-input")
                            hxSwap(HxSwapOption.OUTER_HTML)
                            hxInclude("[name='language-select'], [name='word-amount-slider'], [name='include-special'], [name='include-numbers'], [name='separator']")
                            title = "Generate"
                            embedSvg("/static/svg/regen.svg")
                        }

                        // Settings Dropdown
                        div("dropdown md:dropdown-top dropdown-left") {
                            label {
                                tabIndex = "0"
                                classes = setOf("btn", "btn-ghost")
                                title = "Settings"
                                embedSvg("/static/svg/settings.svg")
                            }
                            div("dropdown-content z-1 menu p-4 shadow-lg bg-base-100 rounded-xl w-48 md:w-64 text-base") {
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
                                        name = "language-select"
                                        classes = setOf("select", "select-bordered", "w-full")
                                        attributes["onchange"] = "document.getElementById('regen-button').click()"
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
                                        name = "word-amount-slider"
                                        min = "1"
                                        max = "50"
                                        step = "1"
                                        value = "4"
                                        id = "word-amount-slider"
                                        classes = setOf("range mb-3")
                                        attributes["oninput"] = """
                                            document.getElementById('word-amount').textContent = this.value
                                            document.getElementById('word-input').value = this.value
                                            document.getElementById('regen-button').click()
                                        """.trimIndent()
                                    }
                                    input(InputType.number) {
                                        min = "1"
                                        max = "50"
                                        value = "4"
                                        id = "word-input"
                                        classes = setOf("input", "input-bordered", "w-full")
                                        attributes["oninput"] = """
                                            if(this.value > 50) this.value = 50
                                            document.getElementById('word-amount').textContent = this.value;
                                            document.getElementById('word-amount-slider').value = this.value;
                                            document.getElementById('regen-button').click()
                                        """.trimIndent()
                                    }
                                }

                                div("form-control mb-3") {
                                    label {
                                        classes = setOf("label", "py-1")
                                        span {
                                            classes = setOf("label-text", "text-sm")
                                            +"Separator:"
                                        }
                                    }
                                    input(InputType.text) {
                                        name = "separator"
                                        maxLength = 1.toString()
                                        value = "-"
                                        id = "word-amount-slider"
                                        classes = setOf("input", "input-bordered", "w-full")
                                        attributes["oninput"] = "document.getElementById('regen-button').click()"
                                    }
                                }

                                // Checkbox include numbers
                                div {
                                    classes = setOf("form-control")
                                    label {
                                        classes = setOf("label cursor-pointer flex justify-between gap-2 py-1")
                                        input {
                                            type = InputType.checkBox
                                            name = "include-numbers"
                                            classes = setOf("checkbox checkbox-sm")
                                            checked = false
                                            attributes["onchange"] = "document.getElementById('regen-button').click()"
                                        }
                                        span {
                                            classes = setOf("label-text text-sm")
                                            +"Include numbers"
                                        }
                                    }
                                }
                                // Checkbox for special characters
                                div {
                                    classes = setOf("form-control")
                                    label {
                                        classes = setOf("label cursor-pointer flex justify-between gap-2 py-1")
                                        input {
                                            type = InputType.checkBox
                                            name = "include-special"
                                            classes = setOf("checkbox checkbox-sm")
                                            checked = false
                                            attributes["onchange"] = "document.getElementById('regen-button').click()"
                                        }
                                        span {
                                            classes = setOf("label-text text-sm")
                                            +"Include special characters"
                                        }
                                    }
                                }
                            }
                        }

                        // Share Button
                        button(classes = "btn btn-ghost") {
                            id = "shareButton"
                            title = "Share"
                            hxPost("/share")
                            hxInclude("[name='password-input']")
                            hxDisabled("#shareButton")
                            embedSvg("/static/svg/share.svg")
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
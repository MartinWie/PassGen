package de.mw.frontend.pages

import de.mw.frontend.utils.buildHTMLString
import de.mw.frontend.utils.getFooter
import de.mw.frontend.utils.getPageHead
import de.mw.utils.HtmxExtension
import de.mw.utils.hxExt
import kotlinx.html.*

fun getLandingPage(pageTitle: String): String {
    return getBasePage(pageTitle) {
        div {
            classes = setOf("")
            input {
                classes = setOf("")
                type = InputType.text
                placeholder = "Input"
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
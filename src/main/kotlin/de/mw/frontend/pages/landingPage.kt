package de.mw.frontend.pages

import de.mw.frontend.utils.buildHTMLString
import de.mw.frontend.utils.getFooter
import de.mw.frontend.utils.getPageHead
import de.mw.utils.HtmxExtension
import de.mw.utils.hxExt
import kotlinx.html.*

fun getSaasLandingPage(pageTitle: String): String {
    return getSaasBasePage(pageTitle) {
    }
}

fun getSaasBasePage(
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
            getSaasNavbar()

            div {
                classes = setOf("flex-grow")
                bodyTags()
            }

            getFooter()
        }
    }
}

fun TagConsumer<StringBuilder>.getSaasNavbar() {
    nav {
        id = "navbar"
        classes = setOf(
            "navbar sticky top-0 z-50 bg-base-200 shadow-md w-full",
            "flex flex-row flex-wrap items-center justify-between",
            "lg:px-8"
        )

        // Logo and title (left side)
        div {
            classes = setOf(
                "flex-1",
                "lg:ml-8"
            )
            a(href = "/") {
                classes = setOf(
                    "flex items-center",
                    "btn-ghost hover:border hover:border-accent hover:rounded-lg"
                )
                img {
                    src = "/static/PassGen-logo.png"
                    alt = "PassGen Logo"
                    classes = setOf(
                        "h-40 w-40 object-cover",
                        "lg:h-20 lg:w-20"
                    )
                }
                h1 {
                    classes = setOf(
                        "text-6xl font-semibold",
                        "sm:mr-10",
                        "lg:text-2xl lg:mr-5"
                    )
                    +"PassGen"
                }
            }
        }
    }
}
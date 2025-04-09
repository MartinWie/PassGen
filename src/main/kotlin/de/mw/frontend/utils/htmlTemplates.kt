package de.mw.frontend.utils

import kotlinx.html.*
import java.time.Year

/**
 * Generates the head section of an HTML page.
 *
 * @param pageTitle The title of the page to be displayed in the browser tab.
 */
fun TagConsumer<StringBuilder>.getPageHead(pageTitle: String = "") {
    head {
        title = pageTitle

        link {
            rel = "icon"
            href = "/static/favicon.ico"
            sizes = "any"
        }

        link {
            rel = "apple-touch-icon"
            href = "/static/apple-touch-icon.png"
        }

        // Web Application Manifest
        link {
            rel = "manifest"
            href = "/static/site.webmanifest"
        }

        meta {
            name = "description"
            content = "A simple password tool."
        }

        meta {
            name = "mobile-web-app-capable"
            content = "yes"
        }
        meta {
            name = "apple-mobile-web-app-status-bar-style"
            content = "black-translucent"
        }
        meta {
            name = "apple-mobile-web-app-title"
            content = "PassGen"
        }

        // HTMX 2.0.4 minified
        script { src = "/static/htmx.min.js" }

        // Own JS functions
        script { src = "/static/app.js" }

        // CSS (mainly Tailwind)
        link {
            rel = "stylesheet"
            href = "/static/output.css"
        }

        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }

        meta {
            charset = "UTF-8"
        }

        // Default font and font smoothing styles
        style {
            unsafe {
                raw(
                    """
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                    }
                """.trimIndent()
                )
            }
        }
    }
}

fun TagConsumer<StringBuilder>.getFooter() {
    footer {
        id = "footer"
        classes = setOf("")

        aside {
            p { +"Copyright © ${Year.now()} - All right reserved" }
        }
    }
}
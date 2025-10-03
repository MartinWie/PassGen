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

        // Set theme immediately before page renders to prevent flash
        script {
            PageSecurityContext.scriptNonce?.let { attributes["nonce"] = it }
            unsafe {
                raw(
                    """
                    (function() {
                        const savedTheme = localStorage.getItem('theme');
                        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                        const activeTheme = savedTheme ? savedTheme : (prefersDark ? 'dark' : 'light');
                        document.documentElement.setAttribute('data-theme', activeTheme);
                    })();
                    """.trimIndent()
                )
            }
        }

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

        // TweetNaCl for key generation
        script { src = "/static/tweetnacl.min.js" }

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
        classes = setOf("flex flex-col items-center justify-center gap-2")

        div("flex flex-row items-center gap-3") {
            a(href = "https://www.buymeacoffee.com/martinwie", target = "_blank") {
                classes = setOf("flex items-center gap-2 text-sm hover:text-amber-500")
                title = "Support this project"
                span {
                    classes = setOf("w-6 h-6 inline-flex items-center")
                    embedSvg("/static/svg/buymeacoffee.svg")
                }
                +"Support this project"
            }

            a(href = "https://github.com/MartinWie/PassGen/issues", target = "_blank") {
                classes = setOf("flex items-center gap-3 text-sm hover:text-amber-500")
                title = "Feedback or report a bug"
                span {
                    classes = setOf("w-6 h-6 inline-flex items-center")
                    embedSvg("/static/svg/github.svg")
                }
            }
        }

        aside {
            p { +"Copyright © ${Year.now()} - All right reserved" }
        }
    }
}
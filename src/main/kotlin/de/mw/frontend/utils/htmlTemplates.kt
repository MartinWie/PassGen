package de.mw.frontend.utils

import de.mw.utils.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.time.Year
import java.util.*
import kotlin.collections.set

/**
 * Builds an HTML string using the provided builder action.
 *
 * @param builderAction The action to build the HTML content.
 * @return The generated HTML string.
 *
 * Example usage:
 * body {
 *     h1 { +"Hello, World!" }
 * }
 */
fun buildHTMLString(builderAction: TagConsumer<StringBuilder>.() -> Unit): String {
    return buildString {
        appendHTML().builderAction()
    }
}

/**
 * Generates the head section of an HTML page.
 *
 * @param pageTitle The title of the page to be displayed in the browser tab.
 */
fun TagConsumer<StringBuilder>.getPageHead(pageTitle: String = "") {
    head {
        title = pageTitle

        // Web Application Manifest
        link { // TODO: get this fixed and add the other icons to the page head
            rel = "manifest"
            href = "/static/site.webmanifest"
        }

        // HTMX 2.0.4 minified
        script { src = "/static/htmx.min.js" }

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
                raw("""
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        -webkit-font-smoothing: antialiased;
                        -moz-osx-font-smoothing: grayscale;
                    }
                """.trimIndent())
            }
        }

        script(type = ScriptType.textJavaScript) {
            unsafe {
                // Using unsafe{}.raw() to insert raw HTML/JS.
                // Be cautious with 'unsafe' as it can potentially open up for XSS vulnerabilities
                // if untrusted user input is inserted into the HTML.
                raw(
                    """
            document.addEventListener("DOMContentLoaded", (event) => {
                document.body.addEventListener('htmx:beforeSwap', function(evt) {
                    if (evt.detail.xhr.status === 422 || evt.detail.xhr.status === 401 || evt.detail.xhr.status === 400) {
                        // Allow 422, 401 and 400 responses to swap.
                        //
                        // set isError to false to avoid error logging in console
                        evt.detail.shouldSwap = true;
                        evt.detail.isError = false;
                    }
                });
            });
            
            document.addEventListener("htmx:configRequest", function(evt) {
                // Overriding the event when htmx starts a request
                let element = evt.detail.elt;
                element.classList.add('skeleton');
            
                // Adding an event listener to remove the class after the request completes
                element.addEventListener('htmx:afterRequest', function clearLoading() {
                    element.classList.remove('skeleton');
                
                     // Optionally remove the event listener afterward to prevent memory leaks
                     element.removeEventListener('htmx:afterRequest', clearLoading);
                });
            });
            
            function emitCustomEvent(elementId, eventName) {
                const event = new CustomEvent(eventName, {
                bubbles: true,
                cancelable: true,
                detail: { id: elementId }
            });
            document.getElementById(elementId).dispatchEvent(event);
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

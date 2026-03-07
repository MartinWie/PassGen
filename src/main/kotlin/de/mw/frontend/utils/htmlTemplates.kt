package de.mw.frontend.utils

import io.github.martinwie.htmx.JsEvent
import io.github.martinwie.htmx.embedSvg
import io.github.martinwie.htmx.onEvent
import kotlinx.html.*
import java.time.Year

private const val COOKIE_CONSENT_KEY = "cookie_consent"
private const val DEFAULT_POSTHOG_KEY = "phc_GxF97xQ1R685lo6S7bwRf6HFB1Ta56lAAJLFhtln60p"

private fun posthogKey(): String? =
    System
        .getenv("POSTHOG_KEY")
        ?.takeIf { it.isNotBlank() }
        ?: System.getenv("SECRET_POSTHOG_KEY")?.takeIf { it.isNotBlank() }
        ?: DEFAULT_POSTHOG_KEY

private fun posthogEnabled(): Boolean = System.getenv("POSTHOG_ENABLED")?.lowercase() != "false" && posthogKey() != null

private fun posthogHost(): String = System.getenv("POSTHOG_HOST")?.takeIf { it.isNotBlank() } ?: "https://eu.i.posthog.com"

private fun jsEscape(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("'", "\\'")

private fun HEAD.posthogScript() {
    if (!posthogEnabled()) return

    val key = jsEscape(posthogKey() ?: return)
    val host = jsEscape(posthogHost())

    script {
        unsafe {
            raw(
                """
                (function() {
                    var CONSENT_KEY = '$COOKIE_CONSENT_KEY';
                    var consent = localStorage.getItem(CONSENT_KEY);

                    function initPosthogIfAccepted() {
                        if (localStorage.getItem(CONSENT_KEY) !== 'accepted') return;
                        if (window.__passgenPosthogInitialized) return;

                        !function(t,e){var o,n,p,r;e.__SV||(window.posthog=e,e._i=[],e.init=function(i,s,a){function g(t,e){var o=e.split(".");2==o.length&&(t=t[o[0]],e=o[1]),t[e]=function(){t.push([e].concat(Array.prototype.slice.call(arguments,0)))}}(p=t.createElement("script")).type="text/javascript",p.crossOrigin="anonymous",p.async=!0,p.src=s.api_host.replace(".i.posthog.com","-assets.i.posthog.com")+"/static/array.js",(r=t.getElementsByTagName("script")[0]).parentNode.insertBefore(p,r);var u=e;for(void 0!==a?u=e[a]=[]:a="posthog",u.people=u.people||[],u.toString=function(t){var e="posthog";return"posthog"!==a&&(e+="."+a),t||(e+=" (stub)"),e},u.people.toString=function(){return u.toString(1)+".people (stub)"},o="init capture register register_once register_for_session unregister opt_out_capturing has_opted_out_capturing opt_in_capturing reset isFeatureEnabled getFeatureFlag getFeatureFlagPayload reloadFeatureFlags group identify setPersonProperties setPersonPropertiesForFlags resetPersonPropertiesForFlags setGroupPropertiesForFlags resetGroupPropertiesForFlags resetGroups onFeatureFlags addFeatureFlagsHandler onSessionId getSurveys getActiveMatchingSurveys renderSurvey canRenderSurvey getNextSurveyStep".split(" "),n=0;n<o.length;n++)g(u,o[n]);e._i.push([i,s,a])},e.__SV=1)}(document,window.posthog||{});

                        posthog.init('$key', {
                            api_host: '$host',
                            defaults: '2026-01-30',
                            person_profiles: 'identified_only'
                        });
                        posthog.opt_in_capturing();
                        window.__passgenPosthogInitialized = true;
                    }

                    initPosthogIfAccepted();

                    window.openCookieSettings = function() {
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.remove('hidden');
                    };

                    window.acceptCookies = function() {
                        localStorage.setItem(CONSENT_KEY, 'accepted');
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.add('hidden');
                        initPosthogIfAccepted();
                    };

                    window.rejectCookies = function() {
                        localStorage.setItem(CONSENT_KEY, 'rejected');
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.add('hidden');
                        if (window.posthog && typeof posthog.opt_out_capturing === 'function') {
                            posthog.opt_out_capturing();
                        }
                        if (window.posthog && typeof posthog.reset === 'function') {
                            posthog.reset();
                        }
                    };
                })();
                """.trimIndent(),
            )
        }
    }
}

fun BODY.getCookieConsentBanner() {
    if (!posthogEnabled()) return

    div {
        id = "cookie-banner"
        classes =
            setOf(
                "hidden",
                "fixed",
                "bottom-4",
                "left-1/2",
                "-translate-x-1/2",
                "z-50",
                "w-[calc(100%-1.5rem)]",
                "max-w-2xl",
                "rounded-xl",
                "border",
                "border-base-300",
                "bg-base-100",
                "shadow-xl",
                "p-4",
            )

        p("text-sm text-base-content/80") {
            +"We use optional analytics cookies to improve PassGen. You can accept or reject this anytime."
            +" "
            a(href = "/privacy") {
                classes = setOf("link", "link-hover")
                +"Learn more in the privacy policy"
            }
            +"."
        }

        div("mt-3 flex flex-wrap gap-2 justify-end") {
            button {
                classes = setOf("btn", "btn-ghost", "btn-sm")
                onEvent(JsEvent.ON_CLICK, "rejectCookies()")
                +"Reject"
            }
            button {
                classes = setOf("btn", "btn-primary", "btn-sm")
                onEvent(JsEvent.ON_CLICK, "acceptCookies()")
                +"Accept"
            }
        }
    }

    script {
        unsafe {
            raw(
                """
                (function() {
                    var consent = localStorage.getItem('$COOKIE_CONSENT_KEY');
                    if (consent === null) {
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.remove('hidden');
                    }
                })();
                """.trimIndent(),
            )
        }
    }
}

/**
 * Generates the head section of an HTML page.
 *
 * @param pageTitle The title of the page to be displayed in the browser tab.
 */
fun TagConsumer<StringBuilder>.getPageHead(pageTitle: String = "") {
    head {
        title { +pageTitle }

        // Set theme immediately before page renders to prevent flash.
        // Allowed by 'unsafe-inline' in script-src (no nonce needed).
        script {
            unsafe {
                raw(
                    """
                    (function() {
                        const savedTheme = localStorage.getItem('theme');
                        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                        const activeTheme = savedTheme ? savedTheme : (prefersDark ? 'dark' : 'light');
                        document.documentElement.setAttribute('data-theme', activeTheme);
                    })();
                    """.trimIndent(),
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

        // Own JS functions
        script { src = "/static/app.js" }

        posthogScript()

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
    }
}

fun TagConsumer<StringBuilder>.getFooter() {
    footer {
        id = "footer"
        classes = setOf("flex flex-col items-center justify-center gap-3 pb-4")

        div("flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-sm") {
            a(href = "/") {
                classes = setOf("link", "link-hover")
                +"Generate"
            }
            span("opacity-50") { +"•" }
            a(href = "/how-it-works") {
                classes = setOf("link", "link-hover")
                +"How it works"
            }
            span("opacity-50") { +"•" }
            a(href = "/privacy") {
                classes = setOf("link", "link-hover")
                +"Privacy"
            }
            span("opacity-50") { +"•" }
            a(href = "/imprint") {
                classes = setOf("link", "link-hover")
                +"Imprint"
            }
            if (posthogEnabled()) {
                span("opacity-50") { +"•" }
                button {
                    classes = setOf("link", "link-hover", "cursor-pointer")
                    onEvent(JsEvent.ON_CLICK, "openCookieSettings()")
                    +"Cookie settings"
                }
            }
        }

        div("flex flex-row items-center gap-3") {
            a(href = "https://www.buymeacoffee.com/martinwie", target = "_blank") {
                classes = setOf("flex items-center gap-2 text-sm hover:text-warning")
                attributes["rel"] = "noopener noreferrer"
                title = "Support this project"
                span {
                    classes = setOf("w-6 h-6 inline-flex items-center")
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/buymeacoffee.svg")
                }
                +"Support this project"
            }

            a(href = "https://github.com/MartinWie/PassGen/issues", target = "_blank") {
                classes = setOf("flex items-center gap-3 text-sm hover:text-warning")
                attributes["rel"] = "noopener noreferrer"
                title = "Feedback or report a bug"
                span {
                    classes = setOf("w-6 h-6 inline-flex items-center")
                    attributes["aria-hidden"] = "true"
                    embedSvg("/static/svg/github.svg")
                }
            }
        }

        aside {
            p { +"Copyright © ${Year.now()} - All right reserved" }
        }
    }
}

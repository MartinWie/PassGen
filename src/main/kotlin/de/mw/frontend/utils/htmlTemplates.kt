package de.mw.frontend.utils

import de.mw.config.AnalyticsConfig
import io.github.martinwie.htmx.JsEvent
import io.github.martinwie.htmx.embedSvg
import io.github.martinwie.htmx.onEvent
import kotlinx.html.*
import java.time.Year

enum class FooterPage {
    GENERATE,
    HOW_IT_WORKS,
    PRIVACY,
    IMPRINT,
}

private fun jsEscape(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("'", "\\'")

private fun HEAD.posthogScript() {
    if (!AnalyticsConfig.posthogEnabled()) return

    val key = jsEscape(AnalyticsConfig.posthogKey() ?: return)
    val host = jsEscape(AnalyticsConfig.posthogHost())

    script {
        unsafe {
            raw(
                """
                (function() {
                    var CONSENT_KEY = '${AnalyticsConfig.COOKIE_CONSENT_KEY}';

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

                    window.openCookieSettings = function() {
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.remove('hidden');
                    };

                    window.acceptCookies = function() {
                        localStorage.setItem(CONSENT_KEY, 'accepted');
                        var banner = document.getElementById('cookie-banner');
                        if (banner) banner.classList.add('hidden');
                        try {
                            initPosthogIfAccepted();
                        } catch (e) {
                            console.error('PostHog init failed after consent:', e);
                        }
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

                    try {
                        initPosthogIfAccepted();
                    } catch (e) {
                        console.error('PostHog init failed on startup:', e);
                    }
                })();
                """.trimIndent(),
            )
        }
    }
}

fun BODY.getCookieConsentBanner() {
    if (!AnalyticsConfig.posthogEnabled()) return

    div {
        id = "cookie-banner"
        classes =
            setOf(
                "hidden",
                "fixed",
                "z-50",
                "bottom-3",
                "inset-x-3",
                "sm:inset-x-auto",
                "sm:right-4",
                "sm:w-[26rem]",
            )

        div("card border border-base-300 bg-base-100 shadow-2xl") {
            div("card-body p-4") {
                div("flex items-start gap-3") {
                    span("w-5 h-5 mt-0.5 text-info") {
                        attributes["aria-hidden"] = "true"
                        embedSvg("/static/svg/alert-info.svg")
                    }
                    div("min-w-0") {
                        p("text-sm font-semibold text-base-content") { +"Cookie preferences" }
                        p("mt-1 text-sm leading-relaxed text-base-content/70") {
                            +"Optional analytics helps us improve PassGen."
                            +" It only starts after your consent."
                            +" "
                            a(href = "/privacy") {
                                classes = setOf("link", "link-hover")
                                +"Privacy details"
                            }
                            +"."
                        }
                    }
                }

                div("mt-3 flex items-center justify-end gap-2") {
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
        }
    }

    script {
        unsafe {
            raw(
                """
                (function() {
                    var consent = localStorage.getItem('${AnalyticsConfig.COOKIE_CONSENT_KEY}');
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

private fun footerLinkClasses(active: Boolean): Set<String> =
    if (active) {
        setOf("link", "link-primary", "font-semibold", "underline", "underline-offset-4")
    } else {
        setOf("link", "link-hover")
    }

fun TagConsumer<StringBuilder>.getFooter(activePage: FooterPage = FooterPage.GENERATE) {
    footer {
        id = "footer"
        classes = setOf("mt-8", "border-t", "border-base-300/70", "bg-base-200/40")

        div("mx-auto w-full max-w-5xl px-4 py-8") {
            div("flex flex-col gap-6 md:flex-row md:items-start md:justify-between") {
                div("max-w-md") {
                    p("text-sm font-semibold tracking-wide uppercase text-base-content/70") { +"PassGen" }
                    p("mt-2 text-sm leading-relaxed text-base-content/60") {
                        +"Generate passwords and key pairs in your browser, then share securely when needed."
                    }
                }

                div("rounded-box border border-base-300 bg-base-100/80 px-4 py-3") {
                    div("flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-sm") {
                        a(href = "/") {
                            classes = footerLinkClasses(activePage == FooterPage.GENERATE)
                            +"Generate"
                        }
                        a(href = "/how-it-works") {
                            classes = footerLinkClasses(activePage == FooterPage.HOW_IT_WORKS)
                            +"How it works"
                        }
                        a(href = "/privacy") {
                            classes = footerLinkClasses(activePage == FooterPage.PRIVACY)
                            +"Privacy"
                        }
                        a(href = "/imprint") {
                            classes = footerLinkClasses(activePage == FooterPage.IMPRINT)
                            +"Imprint"
                        }
                        if (AnalyticsConfig.posthogEnabled()) {
                            button {
                                classes = setOf("link", "link-hover", "cursor-pointer")
                                onEvent(JsEvent.ON_CLICK, "openCookieSettings()")
                                +"Cookie settings"
                            }
                        }
                    }
                }
            }

            div("mt-6 flex flex-wrap items-center justify-center md:justify-start gap-3") {
                a(href = "https://www.buymeacoffee.com/martinwie", target = "_blank") {
                    classes = setOf("btn", "btn-sm", "btn-outline", "gap-2")
                    attributes["rel"] = "noopener noreferrer"
                    title = "Support this project"
                    span {
                        classes = setOf("w-5", "h-5", "inline-flex", "items-center")
                        attributes["aria-hidden"] = "true"
                        embedSvg("/static/svg/buymeacoffee.svg")
                    }
                    +"Support"
                }

                a(href = "https://github.com/MartinWie/PassGen/issues", target = "_blank") {
                    classes = setOf("btn", "btn-ghost", "btn-sm", "gap-2")
                    attributes["rel"] = "noopener noreferrer"
                    title = "Feedback or report a bug"
                    span {
                        classes = setOf("w-5", "h-5", "inline-flex", "items-center")
                        attributes["aria-hidden"] = "true"
                        embedSvg("/static/svg/github.svg")
                    }
                    +"Feedback"
                }
            }

            aside("mt-6 text-center md:text-left") {
                p("text-xs text-base-content/50") { +"Copyright © ${Year.now()} PassGen" }
            }
        }
    }
}

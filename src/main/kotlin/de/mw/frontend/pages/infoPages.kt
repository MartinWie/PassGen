package de.mw.frontend.pages

import de.mw.frontend.utils.FooterPage
import kotlinx.html.*

private fun legalValue(
    key: String,
    fallback: String,
): String = System.getenv(key)?.takeIf { it.isNotBlank() } ?: fallback

private val legalEmail = legalValue("LEGAL_EMAIL", "info-7mw@googlegroups.com")

private fun TagConsumer<StringBuilder>.infoPageContainer(
    title: String,
    subtitle: String,
    content: DIV.() -> Unit,
) {
    div {
        classes = setOf("mx-auto", "max-w-5xl", "px-4", "pt-28", "pb-14")

        div("rounded-2xl border border-base-300 bg-base-100/80 shadow-sm p-6 md:p-8") {
            div("flex flex-wrap items-center gap-2 text-xs text-base-content/60") {
                a(href = "/") {
                    classes = setOf("badge", "badge-outline", "badge-sm")
                    +"Back to generator"
                }
            }
            h1("mt-3 text-3xl md:text-4xl font-bold tracking-tight") { +title }
            p("mt-3 text-base leading-relaxed text-base-content/70 max-w-3xl") { +subtitle }
        }

        div("mt-8 grid gap-5") {
            content()
        }
    }
}

private fun FlowContent.infoCard(
    heading: String,
    content: DIV.() -> Unit,
) {
    div("card bg-base-100/85 border border-base-300 shadow-sm") {
        div("card-body") {
            h2("card-title text-xl md:text-2xl") { +heading }
            content()
        }
    }
}

fun getHowItWorksPage(): String =
    getBasePage("PassGen - How It Works", FooterPage.HOW_IT_WORKS) {
        infoPageContainer(
            title = "How PassGen Works",
            subtitle = "Password generation, sharing, key generation, and key sharing — with the security model behind each flow.",
        ) {
            infoCard("Password generation") {
                p {
                    +"Passwords are generated in your browser using a local word list and cryptographically secure randomness (Web Crypto)."
                }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"No password generation request is sent to the server." }
                    li { +"Word list data is cached locally in your browser for performance." }
                    li { +"You control length, separator, numbers, and special characters client-side." }
                }
            }

            infoCard("Password sharing") {
                p {
                    +"When you choose to share, the password is encrypted on the server with AES-GCM and stored as a one-time secret."
                }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"The share URL contains both IDs required for decryption, including the salt which is not stored on the server." }
                    li { +"View counters are decremented atomically to prevent race-condition double views." }
                    li { +"Each shared password is intended for one successful view/decryption, then it is deleted." }
                }
            }

            infoCard("Key generation") {
                p {
                    +"SSH/Git key pairs are generated in your browser using Web Crypto."
                }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"Private keys are created and kept on your device." }
                    li { +"Private keys are never uploaded to PassGen." }
                }
            }

            infoCard("Key sharing") {
                p {
                    +"Key sharing uses a pending-share flow designed so recipients generate their own private key locally."
                }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"Sender creates a pending key-share link with algorithm/purpose metadata." }
                    li { +"Recipient opens link and generates key pair in browser." }
                    li { +"Only recipient public key is submitted and stored." }
                    li { +"Recipient private key is downloaded locally and not sent to the backend." }
                }
            }

            infoCard("Security assumptions and limits") {
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"A compromised browser/device can still leak secrets." }
                    li {
                        +"PassGen cannot read private keys, and shared passwords are encrypted with a salt that is only present in the share link (without the full link, decryption is not possible)."
                    }
                }
            }
        }
    }

fun getPrivacyPage(): String =
    getBasePage("PassGen - Privacy", FooterPage.PRIVACY) {
        infoPageContainer(
            title = "Privacy Policy",
            subtitle = "How data is processed when you use PassGen.",
        ) {
            infoCard("1. Data Controller") {
                p { +"Responsible for data processing on this website:" }
                p { +"Email: $legalEmail" }
                p { +"This is a private, non-commercial hobby project." }
            }

            infoCard("2. Data Collected") {
                p { +"This website processes the following data:" }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"Session cookies: Technically necessary for functionality. Deleted when browser is closed." }
                    li { +"Temporary technical metadata required to provide the service." }
                    li { +"Usage statistics: Only with your explicit consent via PostHog (servers in the EU)." }
                }
            }

            infoCard("3. Legal Basis") {
                p { +"Processing is based on:" }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"Art. 6(1)(f) GDPR (legitimate interest) for technically necessary cookies" }
                    li { +"Art. 6(1)(a) GDPR (consent) for analytics cookies" }
                }
            }

            infoCard("4. Retention Period") {
                p {
                    +"Session data is deleted when you close the browser. Runtime data is retained only as long as required for operation and abuse prevention. Analytics data is stored according to PostHog policies."
                }
            }

            infoCard("5. Your Rights") {
                p { +"You have the following rights:" }
                ul("list-disc marker:text-primary pl-5 space-y-1 text-base-content/80") {
                    li { +"Access to your stored data (Art. 15 GDPR)" }
                    li { +"Rectification of inaccurate data (Art. 16 GDPR)" }
                    li { +"Erasure of your data (Art. 17 GDPR)" }
                    li { +"Restriction of processing (Art. 18 GDPR)" }
                    li { +"Withdraw consent at any time (Art. 7 GDPR)" }
                    li { +"Lodge a complaint with a supervisory authority (Art. 77 GDPR)" }
                }
            }

            infoCard("6. Cookies") {
                p {
                    +"We use technically necessary session cookies for functionality. Analytics (PostHog) is only enabled with your explicit consent. You can withdraw consent at any time via cookie settings."
                }
            }

            infoCard("7. Third Parties") {
                p {
                    +"PostHog (Analytics): Data is processed on servers in the European Union. More information: posthog.com/privacy"
                }
            }
        }
    }

fun getImprintPage(): String =
    getBasePage("PassGen - Imprint", FooterPage.IMPRINT) {
        infoPageContainer(
            title = "Imprint",
            subtitle = "Provider information for this PassGen deployment.",
        ) {
            infoCard("Information according to § 5 TMG") {
                p { +"This is a private, non-commercial hobby project." }
            }

            infoCard("Contact") {
                p { +"Email: $legalEmail" }
            }

            infoCard("Liability for Content") {
                p {
                    +"As a service provider, we are responsible for our own content on these pages in accordance with § 7 para.1 TMG. The content has been created with the utmost care. However, no guarantee can be given for accuracy, completeness and timeliness."
                }
            }

            infoCard("Liability for Links") {
                p {
                    +"This website may contain links to external third-party websites over whose content we have no influence. The respective provider is always responsible for linked content."
                }
            }
        }
    }

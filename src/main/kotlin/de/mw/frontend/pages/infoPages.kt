package de.mw.frontend.pages

import kotlinx.html.*

private fun legalValue(
    key: String,
    fallback: String,
): String = System.getenv(key)?.takeIf { it.isNotBlank() } ?: fallback

private val legalName = legalValue("LEGAL_NAME", "PassGen")
private val legalAddress = legalValue("LEGAL_ADDRESS", "Please configure LEGAL_ADDRESS")
private val legalEmail = legalValue("LEGAL_EMAIL", "Please configure LEGAL_EMAIL")
private val legalPhone = legalValue("LEGAL_PHONE", "Not provided")
private val legalVatId = legalValue("LEGAL_VAT_ID", "Not provided")

private fun TagConsumer<StringBuilder>.infoPageContainer(
    title: String,
    subtitle: String,
    content: DIV.() -> Unit,
) {
    div {
        classes = setOf("mx-auto", "max-w-4xl", "px-4", "pt-28", "pb-12")

        div("mb-8") {
            h1("text-3xl md:text-4xl font-bold") { +title }
            p("mt-2 text-base-content/70") { +subtitle }
        }

        div("space-y-6") {
            content()
        }
    }
}

private fun FlowContent.infoCard(
    heading: String,
    content: DIV.() -> Unit,
) {
    div("card bg-base-100 border border-base-300") {
        div("card-body") {
            h2("card-title text-xl") { +heading }
            content()
        }
    }
}

fun getHowItWorksPage(): String =
    getBasePage("PassGen - How It Works") {
        infoPageContainer(
            title = "How PassGen Works",
            subtitle = "Password generation, sharing, key generation, and key sharing — with the security model behind each flow.",
        ) {
            infoCard("Password generation") {
                p {
                    +"Passwords are generated in your browser using a local word list and cryptographically secure randomness (Web Crypto)."
                }
                ul("list-disc pl-5 space-y-1") {
                    li { +"No password generation request is sent to the server." }
                    li { +"Word list data is cached locally in your browser for performance." }
                    li { +"You control length, separator, numbers, and special characters client-side." }
                }
            }

            infoCard("Password sharing") {
                p {
                    +"When you choose to share, the password is encrypted on the server with AES-GCM and stored as a one-time secret."
                }
                ul("list-disc pl-5 space-y-1") {
                    li { +"The share URL includes random IDs needed to retrieve and decrypt the secret." }
                    li { +"View counters are decremented atomically to prevent race-condition double views." }
                    li { +"Once views are exhausted, the share is deleted." }
                }
            }

            infoCard("Key generation") {
                p {
                    +"SSH/Git key pairs are generated in your browser using Web Crypto."
                }
                ul("list-disc pl-5 space-y-1") {
                    li { +"Private keys are created and kept on your device." }
                    li { +"Private keys are never uploaded by default." }
                    li { +"Only generated public keys are meant for distribution." }
                }
            }

            infoCard("Key sharing") {
                p {
                    +"Key sharing uses a pending-share flow designed so recipients generate their own private key locally."
                }
                ul("list-disc pl-5 space-y-1") {
                    li { +"Sender creates a pending key-share link with algorithm/purpose metadata." }
                    li { +"Recipient opens link and generates key pair in browser." }
                    li { +"Only recipient public key is submitted and stored." }
                    li { +"Recipient private key is downloaded locally and not sent to the backend." }
                }
            }

            infoCard("Security assumptions and limits") {
                ul("list-disc pl-5 space-y-1") {
                    li { +"Use HTTPS in production to protect traffic and integrity." }
                    li { +"A compromised browser/device can still leak secrets." }
                    li { +"Treat shared links as sensitive — possession of the link grants access." }
                    li { +"For high-risk use cases, add additional operational controls (short TTLs, stricter policies)." }
                }
            }
        }
    }

fun getPrivacyPage(): String =
    getBasePage("PassGen - Privacy") {
        infoPageContainer(
            title = "Privacy Policy",
            subtitle = "How PassGen processes data when you generate, share, and retrieve secrets.",
        ) {
            infoCard("What is processed") {
                ul("list-disc pl-5 space-y-1") {
                    li { +"Generated passwords are created in-browser and are not sent to the server unless you click Share." }
                    li { +"Generated private keys are created in-browser and stay local by design." }
                    li { +"Shared passwords are stored encrypted server-side until consumed/expired." }
                    li { +"Key-share entries store metadata and submitted public keys only." }
                    li { +"Server logs may include operational metadata (timestamps, route access, and technical diagnostics)." }
                }
            }

            infoCard("Local browser storage") {
                p {
                    +"PassGen stores non-secret preferences in localStorage (for example UI settings and cached word list data)."
                }
                p {
                    +"Do not use shared/untrusted devices for secret workflows without clearing browser data afterward."
                }
            }

            infoCard("Analytics and cookies") {
                p {
                    +"If analytics is enabled for this deployment, a cookie consent banner is shown."
                }
                ul("list-disc pl-5 space-y-1") {
                    li { +"Accepted: analytics is initialized and may use persistent storage for product insights." }
                    li { +"Rejected or no decision: analytics is not initialized." }
                    li { +"You can clear your consent decision by clearing local site storage." }
                }
            }

            infoCard("Data retention") {
                ul("list-disc pl-5 space-y-1") {
                    li { +"Password shares are removed after view limits are reached." }
                    li { +"Key-share data remains available until explicitly removed by operational policy." }
                }
            }

            infoCard("Contact") {
                p { +"For privacy inquiries, contact: $legalEmail" }
            }
        }
    }

fun getImprintPage(): String =
    getBasePage("PassGen - Imprint") {
        infoPageContainer(
            title = "Imprint",
            subtitle = "Provider information for this PassGen deployment.",
        ) {
            infoCard("Provider") {
                p { +legalName }
                p { +legalAddress }
            }

            infoCard("Contact") {
                p { +"Email: $legalEmail" }
                p { +"Phone: $legalPhone" }
            }

            infoCard("VAT") {
                p { +"VAT ID: $legalVatId" }
            }

            infoCard("Liability") {
                p {
                    +"Content and technical availability are provided in good faith. No guarantee is given for uninterrupted service or suitability for every use case."
                }
            }
        }
    }

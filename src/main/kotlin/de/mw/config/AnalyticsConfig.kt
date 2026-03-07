package de.mw.config

object AnalyticsConfig {
    const val COOKIE_CONSENT_KEY = "cookie_consent"

    private const val DEFAULT_POSTHOG_KEY = "phc_GxF97xQ1R685lo6S7bwRf6HFB1Ta56lAAJLFhtln60p"

    fun posthogKey(): String? =
        System
            .getenv("POSTHOG_KEY")
            ?.takeIf { it.isNotBlank() }
            ?: System.getenv("SECRET_POSTHOG_KEY")?.takeIf { it.isNotBlank() }
            ?: DEFAULT_POSTHOG_KEY

    fun posthogEnabled(): Boolean = System.getenv("POSTHOG_ENABLED")?.lowercase() != "false" && posthogKey() != null

    fun posthogHost(): String = System.getenv("POSTHOG_HOST")?.takeIf { it.isNotBlank() } ?: "https://eu.i.posthog.com"
}

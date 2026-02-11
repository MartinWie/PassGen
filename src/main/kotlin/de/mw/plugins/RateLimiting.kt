package de.mw.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

/**
 * Rate limit configuration loaded from environment variables with sensible defaults.
 *
 * Environment variables (all optional — defaults match production values):
 * - `RATE_LIMIT_CREATE_SHARE`   — limit for POST /share, POST /key/share (default: 10)
 * - `RATE_LIMIT_COMPLETE_SHARE` — limit for POST /key/share/{id}/complete (default: 5)
 * - `RATE_LIMIT_VIEW_SHARE`     — limit for GET/POST share views (default: 30)
 * - `RATE_LIMIT_GENERATE`       — limit for GET /word (default: 120)
 * - `RATE_LIMIT_REFILL_SECONDS` — refill period in seconds for all tiers (default: 60)
 */
data class RateLimitConfig(
    val createShareLimit: Int = envInt("RATE_LIMIT_CREATE_SHARE", 10),
    val completeShareLimit: Int = envInt("RATE_LIMIT_COMPLETE_SHARE", 5),
    val viewShareLimit: Int = envInt("RATE_LIMIT_VIEW_SHARE", 30),
    val generateLimit: Int = envInt("RATE_LIMIT_GENERATE", 120),
    val refillSeconds: Long = envLong("RATE_LIMIT_REFILL_SECONDS", 60),
) {
    companion object {
        private fun envInt(
            name: String,
            default: Int,
        ): Int = System.getenv(name)?.toIntOrNull() ?: default

        private fun envLong(
            name: String,
            default: Long,
        ): Long = System.getenv(name)?.toLongOrNull() ?: default
    }
}

/**
 * Named rate limit tiers for different endpoint categories.
 * All limits are per client IP using the token bucket algorithm.
 */
object RateLimitTiers {
    /** Creating new shares (POST /share, POST /key/share) — writes to DB */
    val CREATE_SHARE = RateLimitName("create-share")

    /** Completing a key share (POST /key/share/{id}/complete) — one-shot mutation */
    val COMPLETE_SHARE = RateLimitName("complete-share")

    /** Viewing/revealing shares (GET /share/..., GET /key/share/..., POST /share/{id}/{salt}) */
    val VIEW_SHARE = RateLimitName("view-share")

    /** Password generation (GET /word) */
    val GENERATE = RateLimitName("generate")
}

/**
 * Installs the Ktor RateLimit plugin with per-IP rate limiters for each tier.
 *
 * Limits are read from [config] (which defaults to environment-variable-backed
 * values with sensible production defaults).
 *
 * Also installs [XForwardedHeaders] so that `request.origin.remoteHost` resolves
 * to the real client IP when running behind a reverse proxy (Traefik, nginx, etc.).
 *
 * NOTE: The in-memory rate limiter works correctly for single-instance deployments.
 * If you scale to multiple replicas, switch to a distributed limiter (e.g. Redis).
 */
fun Application.configureRateLimiting(config: RateLimitConfig = RateLimitConfig()) {
    // Honour X-Forwarded-* headers from the reverse proxy (Traefik / Dokploy)
    // so that request.origin.remoteHost is the real client IP, not the proxy's.
    // TODO: If the app is ever exposed directly (without a trusted proxy in front),
    //       restrict XForwardedHeaders to only trust known proxy IPs, or remove it,
    //       to prevent clients from spoofing X-Forwarded-For to bypass rate limits.
    install(XForwardedHeaders)

    val refill = config.refillSeconds.seconds

    install(RateLimit) {
        /** Shared request key: rate-limit per client IP. */
        fun RateLimitProviderConfig.byClientIp() {
            requestKey { call -> call.request.origin.remoteHost }
        }

        register(RateLimitTiers.CREATE_SHARE) {
            rateLimiter(limit = config.createShareLimit, refillPeriod = refill)
            byClientIp()
        }

        register(RateLimitTiers.COMPLETE_SHARE) {
            rateLimiter(limit = config.completeShareLimit, refillPeriod = refill)
            byClientIp()
        }

        register(RateLimitTiers.VIEW_SHARE) {
            rateLimiter(limit = config.viewShareLimit, refillPeriod = refill)
            byClientIp()
        }

        register(RateLimitTiers.GENERATE) {
            rateLimiter(limit = config.generateLimit, refillPeriod = refill)
            byClientIp()
        }
    }
}

package de.mw.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import kotlin.time.Duration.Companion.seconds

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
 * Also installs [XForwardedHeaders] so that `request.origin.remoteHost` resolves
 * to the real client IP when running behind a reverse proxy (Traefik, nginx, etc.).
 *
 * NOTE: The in-memory rate limiter works correctly for single-instance deployments.
 * If you scale to multiple replicas, switch to a distributed limiter (e.g. Redis).
 */
fun Application.configureRateLimiting() {
    // Honour X-Forwarded-* headers from the reverse proxy (Traefik / Dokploy)
    // so that request.origin.remoteHost is the real client IP, not the proxy's.
    // TODO: If the app is ever exposed directly (without a trusted proxy in front),
    //       restrict XForwardedHeaders to only trust known proxy IPs, or remove it,
    //       to prevent clients from spoofing X-Forwarded-For to bypass rate limits.
    install(XForwardedHeaders)
    install(RateLimit) {
        /** Shared request key: rate-limit per client IP. */
        fun RateLimitProviderConfig.byClientIp() {
            requestKey { call -> call.request.origin.remoteHost }
        }

        register(RateLimitTiers.CREATE_SHARE) {
            rateLimiter(limit = 10, refillPeriod = 60.seconds)
            byClientIp()
        }

        register(RateLimitTiers.COMPLETE_SHARE) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
            byClientIp()
        }

        register(RateLimitTiers.VIEW_SHARE) {
            rateLimiter(limit = 30, refillPeriod = 60.seconds)
            byClientIp()
        }

        register(RateLimitTiers.GENERATE) {
            rateLimiter(limit = 30, refillPeriod = 60.seconds)
            byClientIp()
        }
    }
}

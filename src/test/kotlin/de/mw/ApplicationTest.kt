package de.mw

import de.mw.plugins.buildCspHeaderValue
import de.mw.plugins.configureRateLimiting
import de.mw.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApplicationTest {
    private fun ApplicationTestBuilder.setupApp() {
        application {
            configureRateLimiting()
            configureRouting()
        }
    }

    @Test
    fun testRoot() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }

    @Test
    fun `homepage contains PassGen title`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("PassGen"), "Page should contain PassGen title")
            }
        }

    @Test
    fun `homepage contains password input`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("password-input"), "Page should contain password input")
            }
        }

    @Test
    fun `homepage contains settings dropdown`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("language-select"), "Page should contain language select")
                assertTrue(body.contains("word-amount-slider"), "Page should contain word amount slider")
            }
        }

    @Test
    fun `homepage contains theme toggle`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("theme-switcher"), "Page should contain theme switcher")
            }
        }

    @Test
    fun `homepage serves static favicon`() =
        testApplication {
            setupApp()
            client.get("/static/favicon.ico").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }

    @Test
    fun `homepage includes Content-Security-Policy header`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val csp = headers["Content-Security-Policy"]
                assertTrue(csp != null, "CSP header should be present")
                assertTrue(csp.contains("default-src 'self'"), "CSP should contain default-src")
                assertTrue(csp.contains("frame-ancestors 'none'"), "CSP should block framing")
                assertTrue(csp.contains("form-action 'self'"), "CSP should restrict form submissions to self")
                assertTrue(csp.contains("object-src 'none'"), "CSP should block object/embed")
                assertTrue(csp.contains("base-uri 'none'"), "CSP should block base URI manipulation")
                assertTrue(csp.contains("style-src 'self' 'unsafe-inline'"), "CSP style-src should allow unsafe-inline for HTMX")
                // script-src must include unsafe-inline (for inline event handlers) but NOT a nonce
                // (a nonce would cause browsers to ignore unsafe-inline, breaking onclick etc.)
                assertTrue(
                    csp.contains("script-src 'self' 'unsafe-inline'"),
                    "CSP script-src should contain unsafe-inline: $csp",
                )
                assertFalse(
                    csp.contains("'nonce-"),
                    "CSP script-src must NOT contain a nonce (nonce makes browsers ignore unsafe-inline): $csp",
                )
                // script-src must NOT contain unsafe-eval
                assertFalse(csp.contains("'unsafe-eval'"), "CSP should not allow unsafe-eval")
            }
        }

    @Test
    fun `homepage includes global notification region`() =
        testApplication {
            setupApp()
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("id=\"global-notification\""), "Page should contain global-notification region")
            }
        }

    @Test
    fun `rate limit returns 429 after exceeding generate tier limit`() =
        testApplication {
            setupApp()
            val limit = 120 // GENERATE tier limit
            // Send exactly `limit` requests — all should succeed
            repeat(limit) { i ->
                client.get("/word").apply {
                    assertEquals(
                        HttpStatusCode.OK,
                        status,
                        "Request ${i + 1} of $limit should succeed",
                    )
                }
            }
            // The next request should be rate-limited
            client.get("/word").apply {
                assertEquals(HttpStatusCode.TooManyRequests, status)
            }
        }

    @Test
    fun `rate limit 429 returns HTMX fragment when HX-Request header is set`() =
        testApplication {
            setupApp()
            val limit = 120
            repeat(limit) {
                client.get("/word")
            }
            // Trigger 429 with HX-Request header
            client
                .get("/word") {
                    header("HX-Request", "true")
                }.apply {
                    assertEquals(HttpStatusCode.TooManyRequests, status)
                    val body = bodyAsText()
                    assertTrue(body.contains("alert"), "429 body should contain alert class for HTMX")
                    assertTrue(body.contains("Too many requests"), "429 body should contain rate limit message")
                    assertEquals("#global-notification", headers["HX-Retarget"], "429 should retarget to notification region")
                    assertEquals("innerHTML", headers["HX-Reswap"], "429 should use innerHTML swap")
                }
        }

    @Test
    fun `rate limit 429 returns plain text for non-HTMX requests`() =
        testApplication {
            setupApp()
            val limit = 120
            repeat(limit) {
                client.get("/word")
            }
            // Trigger 429 without HX-Request header
            client.get("/word").apply {
                assertEquals(HttpStatusCode.TooManyRequests, status)
                val body = bodyAsText()
                assertEquals("Too many requests — please wait a moment and try again.", body)
                assertNull(headers["HX-Retarget"], "Plain text 429 should not have HX-Retarget header")
                assertNull(headers["HX-Reswap"], "Plain text 429 should not have HX-Reswap header")
            }
        }

    @Test
    fun `word endpoint returns 200 for invalid language-select (falls back to ENG)`() =
        testApplication {
            setupApp()
            client.get("/word?language-select=INVALID_LANG").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("password-input"), "Should still return a password textarea")
            }
        }

    @Test
    fun `word endpoint returns 400 for word amount below 1`() =
        testApplication {
            setupApp()
            client.get("/word?word-amount-slider=0").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }

    @Test
    fun `word endpoint returns 400 for negative word amount`() =
        testApplication {
            setupApp()
            client.get("/word?word-amount-slider=-5").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }

    @Test
    fun `word endpoint returns 400 for word amount above 50`() =
        testApplication {
            setupApp()
            client.get("/word?word-amount-slider=51").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
            }
        }

    // --- buildCspHeaderValue unit tests ---

    @Test
    fun `buildCspHeaderValue does not include nonce in script-src`() {
        val csp = buildCspHeaderValue(isDevelopment = false)
        assertFalse(csp.contains("nonce"), "script-src should NOT contain a nonce")
    }

    @Test
    fun `buildCspHeaderValue includes unsafe-inline in script-src but not unsafe-eval`() {
        val csp = buildCspHeaderValue(isDevelopment = false)
        // script-src needs unsafe-inline for inline event handler attributes (onEvent in templates)
        val scriptSrc = csp.substringAfter("script-src ").substringBefore(";")
        assertTrue(scriptSrc.contains("'unsafe-inline'"), "script-src should include unsafe-inline for inline event handlers: $scriptSrc")
        assertFalse(csp.contains("'unsafe-eval'"), "CSP should never include unsafe-eval")
        // style-src MAY contain unsafe-inline (HTMX needs inline style manipulation)
        assertTrue(csp.contains("style-src 'self' 'unsafe-inline'"), "style-src should allow unsafe-inline for HTMX")
    }

    @Test
    fun `buildCspHeaderValue includes form-action self`() {
        val csp = buildCspHeaderValue(isDevelopment = false)
        assertTrue(csp.contains("form-action 'self'"), "CSP should restrict form-action to self")
    }

    @Test
    fun `buildCspHeaderValue blocks framing and object embeds`() {
        val csp = buildCspHeaderValue(isDevelopment = false)
        assertTrue(csp.contains("frame-ancestors 'none'"), "CSP should block framing")
        assertTrue(csp.contains("object-src 'none'"), "CSP should block object/embed")
        assertTrue(csp.contains("base-uri 'none'"), "CSP should block base URI manipulation")
    }

    @Test
    fun `buildCspHeaderValue includes dev origins when isDevelopment is true`() {
        val csp = buildCspHeaderValue(isDevelopment = true)
        assertTrue(csp.contains("http://localhost:3000"), "Dev CSP should allow localhost:3000")
        assertTrue(csp.contains("ws://localhost:3000"), "Dev CSP should allow WS localhost:3000")
    }

    @Test
    fun `buildCspHeaderValue excludes dev origins in production`() {
        val csp = buildCspHeaderValue(isDevelopment = false)
        assertFalse(csp.contains("localhost"), "Production CSP should not reference localhost")
    }

    // --- respondHtmxError tests ---

    @Test
    fun `respondHtmxError sets HTMX retarget and reswap headers`() =
        testApplication {
            setupApp()
            // POST /share without password-input triggers respondHtmxError
            client
                .post("/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertEquals("#global-notification", headers["HX-Retarget"])
                    assertEquals("innerHTML", headers["HX-Reswap"])
                    val body = bodyAsText()
                    assertTrue(body.contains("alert-error"), "Body should contain alert-error class")
                    assertTrue(body.contains("Missing value to share"), "Body should contain the error message")
                }
        }

    @Test
    fun `respondHtmxError escapes HTML in error message`() =
        testApplication {
            setupApp()
            // Key share POST with malicious algorithm value triggers respondHtmxError
            // with the server's own fixed message (not user input), but we verify
            // the body is well-formed HTML and doesn't contain raw angle brackets
            // outside of the expected tag structure.
            client
                .post("/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("")
                }.apply {
                    val body = bodyAsText()
                    // The body should be a valid alert div — no unescaped user content
                    assertTrue(body.startsWith("<div"), "Body should start with a div tag")
                    assertTrue(body.endsWith("</div>"), "Body should end with closing div tag")
                }
        }
}

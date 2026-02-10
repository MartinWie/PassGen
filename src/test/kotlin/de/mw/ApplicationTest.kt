package de.mw

import de.mw.plugins.configureRateLimiting
import de.mw.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun `rate limit returns 429 after exceeding generate tier limit`() =
        testApplication {
            setupApp()
            val limit = 30 // GENERATE tier limit
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
            val limit = 30
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
                }
        }

    @Test
    fun `rate limit 429 returns plain text for non-HTMX requests`() =
        testApplication {
            setupApp()
            val limit = 30
            repeat(limit) {
                client.get("/word")
            }
            // Trigger 429 without HX-Request header
            client.get("/word").apply {
                assertEquals(HttpStatusCode.TooManyRequests, status)
                val body = bodyAsText()
                assertEquals("Too many requests — please wait a moment and try again.", body)
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
}

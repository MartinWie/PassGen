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
    private val validEd25519PublicKey =
        "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ user@example.com"
    private val validRsaPublicKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC7z+5X rsa@example.com"

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
                client.get("/wordlist").apply {
                    assertEquals(
                        HttpStatusCode.OK,
                        status,
                        "Request ${i + 1} of $limit should succeed",
                    )
                }
            }
            // The next request should be rate-limited
            client.get("/wordlist").apply {
                assertEquals(HttpStatusCode.TooManyRequests, status)
            }
        }

    @Test
    fun `rate limit 429 returns HTMX fragment when HX-Request header is set`() =
        testApplication {
            setupApp()
            val limit = 120
            repeat(limit) {
                client.get("/wordlist")
            }
            // Trigger 429 with HX-Request header
            client
                .get("/wordlist") {
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
                client.get("/wordlist")
            }
            // Trigger 429 without HX-Request header
            client.get("/wordlist").apply {
                assertEquals(HttpStatusCode.TooManyRequests, status)
                val body = bodyAsText()
                assertEquals("Too many requests — please wait a moment and try again.", body)
                assertNull(headers["HX-Retarget"], "Plain text 429 should not have HX-Retarget header")
                assertNull(headers["HX-Reswap"], "Plain text 429 should not have HX-Reswap header")
            }
        }

    @Test
    fun `wordlist endpoint returns JSON payload with both languages`() =
        testApplication {
            setupApp()
            client.get("/wordlist").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertTrue(
                    headers[HttpHeaders.ContentType]?.contains(ContentType.Application.Json.toString()) == true,
                    "Expected application/json content type",
                )
                val body = bodyAsText()
                assertTrue(body.contains("\"eng\":"), "Payload should contain eng key")
                assertTrue(body.contains("\"ger\":"), "Payload should contain ger key")
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

    @Test
    fun `share endpoint rejects blank password value`() =
        testApplication {
            setupApp()
            client
                .post("/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("password-input=   ")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertEquals("#global-notification", headers["HX-Retarget"])
                    assertEquals("innerHTML", headers["HX-Reswap"])
                    assertTrue(bodyAsText().contains("Password value must not be empty"))
                }
        }

    @Test
    fun `health endpoint returns OK`() =
        testApplication {
            setupApp()
            client.get("/health").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("OK", bodyAsText())
            }
        }

    @Test
    fun `key share create returns htmx error when algorithm is missing`() =
        testApplication {
            setupApp()
            client
                .post("/key/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("purpose=ssh")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertEquals("#global-notification", headers["HX-Retarget"])
                    assertEquals("innerHTML", headers["HX-Reswap"])
                    assertTrue(bodyAsText().contains("Missing algorithm"))
                }
        }

    @Test
    fun `key share create returns htmx error when purpose is missing`() =
        testApplication {
            setupApp()
            client
                .post("/key/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertEquals("#global-notification", headers["HX-Retarget"])
                    assertEquals("innerHTML", headers["HX-Reswap"])
                    assertTrue(bodyAsText().contains("Missing purpose"))
                }
        }

    @Test
    fun `key share page returns 400 for invalid share id format`() =
        testApplication {
            setupApp()
            client.get("/key/share/not-a-uuid").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Invalid share ID format", bodyAsText())
            }
        }

    @Test
    fun `key share complete returns 400 for invalid share id format`() =
        testApplication {
            setupApp()
            client
                .post("/key/share/not-a-uuid/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("Invalid share ID format"))
                }
        }

    @Test
    fun `key share complete returns 400 when public key is missing`() =
        testApplication {
            setupApp()
            val id = "00000000-0000-0000-0000-000000000001"
            client
                .post("/key/share/$id/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("Missing public key"))
                }
        }

    @Test
    fun `key share complete returns 400 when algorithm is missing`() =
        testApplication {
            setupApp()
            val id = "00000000-0000-0000-0000-000000000001"
            client
                .post("/key/share/$id/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("public-key=ssh-ed25519+AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("Missing algorithm"))
                }
        }

    @Test
    fun `password share get returns 400 for invalid share id format`() =
        testApplication {
            setupApp()
            client.get("/share/not-a-uuid/not-a-uuid").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Invalid share ID format", bodyAsText())
            }
        }

    @Test
    fun `password share post returns 400 for invalid share id format`() =
        testApplication {
            setupApp()
            client.post("/share/not-a-uuid/not-a-uuid").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Invalid share ID format", bodyAsText())
            }
        }

    @Test
    fun `password share get returns 400 for invalid salt format`() =
        testApplication {
            setupApp()
            val validShareId = "00000000-0000-0000-0000-000000000001"
            client.get("/share/$validShareId/not-a-uuid").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Invalid salt format", bodyAsText())
            }
        }

    @Test
    fun `password share post returns 400 for invalid salt format`() =
        testApplication {
            setupApp()
            val validShareId = "00000000-0000-0000-0000-000000000001"
            client.post("/share/$validShareId/not-a-uuid").apply {
                assertEquals(HttpStatusCode.BadRequest, status)
                assertEquals("Invalid salt format", bodyAsText())
            }
        }

    @Test
    fun `key share page returns 404 for unknown valid share id`() =
        testApplication {
            setupApp()
            client.get("/key/share/00000000-0000-0000-0000-000000000001").apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("Key share not found", bodyAsText())
            }
        }

    @Test
    fun `key share complete returns share not found for unknown valid share id`() =
        testApplication {
            setupApp()
            val unknownId = "00000000-0000-0000-0000-000000000001"
            client
                .post("/key/share/$unknownId/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("public-key=$validEd25519PublicKey&algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("Share not found"))
                }
        }

    @Test
    fun `key share complete returns already completed message on second completion`() =
        testApplication {
            setupApp()

            val createResponse =
                client.post("/key/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("algorithm=ed25519&purpose=ssh")
                }
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val shareBody = createResponse.bodyAsText()
            val shareId =
                Regex("/key/share/([0-9a-fA-F-]{36})")
                    .find(shareBody)
                    ?.groupValues
                    ?.getOrNull(1)
            assertTrue(shareId != null, "Expected share id in key share create response")

            // First completion succeeds
            client
                .post("/key/share/$shareId/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("public-key=$validEd25519PublicKey&algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.OK, status)
                }

            // Second completion should fail with already completed
            client
                .post("/key/share/$shareId/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("public-key=$validEd25519PublicKey&algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("already been completed"))
                }
        }

    @Test
    fun `key share complete returns invalid key mismatch message`() =
        testApplication {
            setupApp()

            val createResponse =
                client.post("/key/share") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("algorithm=ed25519&purpose=ssh")
                }
            assertEquals(HttpStatusCode.OK, createResponse.status)
            val shareBody = createResponse.bodyAsText()
            val shareId =
                Regex("/key/share/([0-9a-fA-F-]{36})")
                    .find(shareBody)
                    ?.groupValues
                    ?.getOrNull(1)
            assertTrue(shareId != null, "Expected share id in key share create response")

            // Use RSA key while claiming ed25519 to trigger mismatch/validation failure
            client
                .post("/key/share/$shareId/complete") {
                    header("Content-Type", "application/x-www-form-urlencoded")
                    setBody("public-key=$validRsaPublicKey&algorithm=ed25519")
                }.apply {
                    assertEquals(HttpStatusCode.BadRequest, status)
                    assertTrue(bodyAsText().contains("Invalid public key format or algorithm mismatch"))
                }
        }

    @Test
    fun `password share post returns 404 for unknown valid share and salt`() =
        testApplication {
            setupApp()
            val unknownShare = "00000000-0000-0000-0000-000000000001"
            val unknownSalt = "00000000-0000-0000-0000-000000000002"
            client.post("/share/$unknownShare/$unknownSalt").apply {
                assertEquals(HttpStatusCode.NotFound, status)
                assertEquals("Share not found, already viewed or expired", bodyAsText())
            }
        }
}

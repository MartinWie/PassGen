package de.mw

import de.mw.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            application {
                configureRouting()
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }

    @Test
    fun `homepage contains PassGen title`() =
        testApplication {
            application {
                configureRouting()
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("PassGen"), "Page should contain PassGen title")
            }
        }

    @Test
    fun `homepage contains password input`() =
        testApplication {
            application {
                configureRouting()
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("password-input"), "Page should contain password input")
            }
        }

    @Test
    fun `homepage contains settings dropdown`() =
        testApplication {
            application {
                configureRouting()
            }
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
            application {
                configureRouting()
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                val body = bodyAsText()
                assertTrue(body.contains("theme-switcher"), "Page should contain theme switcher")
            }
        }

    @Test
    fun `homepage serves static favicon`() =
        testApplication {
            application {
                configureRouting()
            }
            client.get("/static/favicon.ico").apply {
                assertEquals(HttpStatusCode.OK, status)
            }
        }
}

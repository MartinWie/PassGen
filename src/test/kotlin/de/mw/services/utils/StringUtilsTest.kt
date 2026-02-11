package de.mw.services.utils

import de.mw.frontend.utils.escapeHtml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringUtilsTest {
    @Test
    fun `getCurrentTimeAsString returns formatted string`() {
        val result = getCurrentTimeAsString()

        // Should match format: yyyy_MM_dd_HH_mm_ss
        val regex = "\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}".toRegex()
        assertTrue(regex.matches(result), "Time string should match expected format, got: $result")
    }

    @Test
    fun `generateEventCode returns correct length`() {
        val code8 = generateEventCode(8)
        assertEquals(8, code8.length)

        val code12 = generateEventCode(12)
        assertEquals(12, code12.length)

        val code4 = generateEventCode(4)
        assertEquals(4, code4.length)
    }

    @Test
    fun `generateEventCode default length is 8`() {
        val code = generateEventCode()
        assertEquals(8, code.length)
    }

    @Test
    fun `generateEventCode only contains allowed characters`() {
        val allowedChars = "abcdefghijklmnopqrstuvwxyz123456789"

        // Generate multiple codes to increase chance of catching issues
        repeat(100) {
            val code = generateEventCode(20)
            for (char in code) {
                assertTrue(char in allowedChars, "Character '$char' not in allowed chars")
            }
        }
    }

    @Test
    fun `generateEventCode does not contain zero or uppercase`() {
        repeat(100) {
            val code = generateEventCode(20)
            assertFalse('0' in code, "Code should not contain '0'")
            assertFalse(code.any { it.isUpperCase() }, "Code should not contain uppercase letters")
        }
    }

    @Test
    fun `isValidEmail accepts valid emails`() {
        val validEmails =
            listOf(
                "test@example.com",
                "user.name@domain.org",
                "user+tag@example.co.uk",
                "firstname.lastname@company.com",
                "email@subdomain.domain.com",
                "user123@test.io",
            )

        for (email in validEmails) {
            assertTrue(isValidEmail(email), "Expected '$email' to be valid")
        }
    }

    @Test
    fun `isValidEmail rejects invalid emails`() {
        val invalidEmails =
            listOf(
                "plainaddress",
                "@missinglocal.com",
                "missing@.com",
                "missing@domain",
                "spaces in@email.com",
                "missing.domain@",
                "",
            )

        for (email in invalidEmails) {
            assertFalse(isValidEmail(email), "Expected '$email' to be invalid")
        }
    }

    @Test
    fun `SPECIAL_CHARS contains expected characters`() {
        // Verify the constant has the expected special characters
        assertTrue(SPECIAL_CHARS.contains('!'))
        assertTrue(SPECIAL_CHARS.contains('@'))
        assertTrue(SPECIAL_CHARS.contains('#'))
        assertTrue(SPECIAL_CHARS.contains('$'))
        assertTrue(SPECIAL_CHARS.contains('%'))
        assertTrue(SPECIAL_CHARS.contains('&'))
        assertTrue(SPECIAL_CHARS.contains('*'))
        assertTrue(SPECIAL_CHARS.contains('-'))
        assertTrue(SPECIAL_CHARS.contains('_'))
    }

    @Test
    fun `SPECIAL_CHARS does not contain alphanumeric`() {
        for (char in SPECIAL_CHARS) {
            assertFalse(char.isLetterOrDigit(), "SPECIAL_CHARS should not contain '$char'")
        }
    }

    @Test
    fun `escapeHtml escapes ampersand`() {
        assertEquals("foo &amp; bar", "foo & bar".escapeHtml())
    }

    @Test
    fun `escapeHtml escapes angle brackets`() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", "<script>alert(1)</script>".escapeHtml())
    }

    @Test
    fun `escapeHtml escapes quotes`() {
        assertEquals("&quot;hello&quot; &#x27;world&#x27;", "\"hello\" 'world'".escapeHtml())
    }

    @Test
    fun `escapeHtml returns empty string unchanged`() {
        assertEquals("", "".escapeHtml())
    }

    @Test
    fun `escapeHtml preserves safe text`() {
        assertEquals("Hello World 123", "Hello World 123".escapeHtml())
    }
}

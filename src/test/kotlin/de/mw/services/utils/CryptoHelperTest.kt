package de.mw.services.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CryptoHelperTest {
    @Test
    fun `encrypt and decrypt returns original text`() {
        val plainText = "Hello, World! This is a test password."
        val token = "test-token-uuid"
        val salt = "test-salt-uuid"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, salt)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt produces different output for same input`() {
        val plainText = "Same text"
        val token = "token"
        val salt = "salt"

        val encrypted1 = CryptoHelper.encrypt(plainText, token, salt)
        val encrypted2 = CryptoHelper.encrypt(plainText, token, salt)

        // Due to random IV, encrypted values should be different
        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun `decrypt with wrong token returns null`() {
        val plainText = "Secret message"
        val token = "correct-token"
        val wrongToken = "wrong-token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, wrongToken, salt)

        assertNull(decrypted)
    }

    @Test
    fun `decrypt with wrong salt returns null`() {
        val plainText = "Secret message"
        val token = "token"
        val salt = "correct-salt"
        val wrongSalt = "wrong-salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, wrongSalt)

        assertNull(decrypted)
    }

    @Test
    fun `encrypt handles special characters`() {
        val plainText = "Password with special chars: !@#$%^&*()_+-={}[]|:;<>?,./"
        val token = "token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, salt)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt handles unicode characters`() {
        val plainText = "Unicode: 你好世界 🔐 Здравствуй мир"
        val token = "token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, salt)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt handles empty string`() {
        val plainText = ""
        val token = "token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, salt)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypt handles long text`() {
        val plainText = "A".repeat(5000)
        val token = "token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)
        val decrypted = CryptoHelper.decrypt(encrypted, token, salt)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun `encrypted output is base64 url safe`() {
        val plainText = "Test"
        val token = "token"
        val salt = "salt"

        val encrypted = CryptoHelper.encrypt(plainText, token, salt)

        // Base64 URL safe doesn't contain + or /
        assertTrue(!encrypted.contains('+'))
        assertTrue(!encrypted.contains('/'))
        // And doesn't have padding =
        assertTrue(!encrypted.endsWith('='))
    }
}

package de.mw.services

import de.mw.daos.IKeyShareDao
import de.mw.models.SharePublicKey
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fake implementation of IKeyShareDao for testing KeyService.
 */
class FakeKeyShareDao : IKeyShareDao {
    var createdShares: MutableList<SharePublicKey> = mutableListOf()
    var shares: MutableMap<UUID, SharePublicKey> = mutableMapOf()
    var deletedShareIds: MutableList<UUID> = mutableListOf()

    override fun createShare(sharePublicKey: SharePublicKey): String {
        createdShares.add(sharePublicKey)
        shares[sharePublicKey.id] = sharePublicKey
        return sharePublicKey.id.toString()
    }

    override fun getShare(id: UUID): SharePublicKey? = shares[id]

    override fun deleteShare(id: UUID) {
        deletedShareIds.add(id)
        shares.remove(id)
    }

    override fun completeShare(
        id: UUID,
        publicKey: String,
    ): Boolean {
        val share = shares[id] ?: return false
        if (share.publicKey != null) return false // Already completed

        // Update the share with the public key
        shares[id] =
            share.copy(
                publicKey = publicKey,
                completedAt = LocalDateTime.now(),
            )
        return true
    }
}

class KeyServiceTest {
    // Sample valid public keys for testing
    private val validEd25519Key = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ user@example.com"
    private val validEd25519KeyNoComment = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ"
    private val validRsaKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQC7z+5X rsa@example.com"
    private val validEcdsaP256Key = "ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBA== ecdsa@example.com"
    private val validEcdsaP384Key = "ecdsa-sha2-nistp384 AAAAE2VjZHNhLXNoYTItbmlzdHAzODQAAAAIbmlzdHAzODQAAABhBA== ecdsa@example.com"
    private val validEcdsaP521Key = "ecdsa-sha2-nistp521 AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1MjEAAACFBA== ecdsa@example.com"

    // --- sanitizePublicKey Tests ---

    @Test
    fun `sanitizePublicKey accepts valid ed25519 key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEd25519Key)

        assertNotNull(result)
        assertTrue(result.startsWith("ssh-ed25519"))
    }

    @Test
    fun `sanitizePublicKey accepts valid ed25519 key without comment`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEd25519KeyNoComment)

        assertNotNull(result)
        assertEquals(validEd25519KeyNoComment, result)
    }

    @Test
    fun `sanitizePublicKey accepts valid RSA key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validRsaKey)

        assertNotNull(result)
        assertTrue(result.startsWith("ssh-rsa"))
    }

    @Test
    fun `sanitizePublicKey accepts valid ECDSA P256 key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP256Key)

        assertNotNull(result)
        assertTrue(result.startsWith("ecdsa-sha2-nistp256"))
    }

    @Test
    fun `sanitizePublicKey accepts valid ECDSA P384 key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP384Key)

        assertNotNull(result)
        assertTrue(result.startsWith("ecdsa-sha2-nistp384"))
    }

    @Test
    fun `sanitizePublicKey accepts valid ECDSA P521 key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP521Key)

        assertNotNull(result)
        assertTrue(result.startsWith("ecdsa-sha2-nistp521"))
    }

    @Test
    fun `sanitizePublicKey rejects key that is too long`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val tooLongKey = "ssh-ed25519 " + "A".repeat(5000)

        val result = service.sanitizePublicKey(tooLongKey)

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey rejects key with invalid algorithm prefix`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidKey = "invalid-algo AAAAB3NzaC1yc2EA comment"

        val result = service.sanitizePublicKey(invalidKey)

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey rejects key with missing base64 data`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidKey = "ssh-ed25519"

        val result = service.sanitizePublicKey(invalidKey)

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey rejects key with invalid base64 characters`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidKey = "ssh-ed25519 AAA!@#invalid base64"

        val result = service.sanitizePublicKey(invalidKey)

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey rejects algorithm mismatch - ed25519 selected but RSA key provided`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validRsaKey, expectedAlgorithm = "ed25519")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey rejects algorithm mismatch - rsa selected but ed25519 key provided`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEd25519Key, expectedAlgorithm = "rsa-2048")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - ed25519`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEd25519Key, expectedAlgorithm = "ed25519")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - rsa-2048`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validRsaKey, expectedAlgorithm = "rsa-2048")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - rsa-4096`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validRsaKey, expectedAlgorithm = "rsa-4096")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - rsa-8192`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validRsaKey, expectedAlgorithm = "rsa-8192")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - ecdsa-p256`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP256Key, expectedAlgorithm = "ecdsa-p256")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - ecdsa-p384`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP384Key, expectedAlgorithm = "ecdsa-p384")

        assertNotNull(result)
    }

    @Test
    fun `sanitizePublicKey accepts matching algorithm - ecdsa-p521`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validEcdsaP521Key, expectedAlgorithm = "ecdsa-p521")

        assertNotNull(result)
    }

    // --- Comment sanitization tests ---

    @Test
    fun `sanitizePublicKey removes HTML tags from comment`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val keyWithHtmlComment = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ <script>alert('xss')</script>"

        val result = service.sanitizePublicKey(keyWithHtmlComment)

        assertNotNull(result)
        assertTrue(!result.contains("<script>"), "Should not contain script tags")
        assertTrue(!result.contains("</script>"), "Should not contain closing script tags")
    }

    @Test
    fun `sanitizePublicKey replaces dangerous characters in comment`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val keyWithDangerousChars = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ user<test>\"name'"

        val result = service.sanitizePublicKey(keyWithDangerousChars)

        assertNotNull(result)
        assertTrue(!result.contains("<"), "Should not contain <")
        assertTrue(!result.contains(">"), "Should not contain >")
        assertTrue(!result.contains("\""), "Should not contain double quote")
        assertTrue(!result.contains("'"), "Should not contain single quote")
    }

    @Test
    fun `sanitizePublicKey removes non-printable ASCII from comment`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val keyWithNonPrintable = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ user\u0000\u0001name"

        val result = service.sanitizePublicKey(keyWithNonPrintable)

        assertNotNull(result)
        // The sanitized comment should only have printable ASCII
        val parts = result.split(" ", limit = 3)
        if (parts.size == 3) {
            val comment = parts[2]
            comment.forEach { char ->
                assertTrue(char.code in 0x20..0x7E, "Character '$char' (${char.code}) should be printable ASCII")
            }
        }
    }

    @Test
    fun `sanitizePublicKey truncates long comment to 256 chars`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val longComment = "a".repeat(500)
        val keyWithLongComment = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ $longComment"

        val result = service.sanitizePublicKey(keyWithLongComment)

        assertNotNull(result)
        val parts = result.split(" ", limit = 3)
        if (parts.size == 3) {
            val comment = parts[2]
            assertTrue(comment.length <= 256, "Comment should be truncated to 256 chars, got ${comment.length}")
        }
    }

    @Test
    fun `sanitizePublicKey trims whitespace`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val keyWithWhitespace = "  ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIGpKWz8i5RuBaVG2EyIwU7IiG7NzJ3y9FdJqtFLDZ8lZ user@example.com  "

        val result = service.sanitizePublicKey(keyWithWhitespace)

        assertNotNull(result)
        assertTrue(!result.startsWith(" "), "Should not start with whitespace")
        assertTrue(!result.endsWith(" "), "Should not end with whitespace")
    }

    // --- getShare Tests ---

    @Test
    fun `getShare returns share when exists`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        val result = service.getShare(shareId)

        assertNotNull(result)
        assertEquals(shareId, result.id)
    }

    @Test
    fun `getShare returns null when not exists`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val randomId = UUID.randomUUID()

        val result = service.getShare(randomId)

        assertNull(result)
    }

    // --- deleteShare Tests ---

    @Test
    fun `deleteShare removes share from dao`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        service.deleteShare(shareId)

        assertTrue(fakeDao.deletedShareIds.contains(shareId))
        assertNull(fakeDao.shares[shareId])
    }

    // --- Edge case tests ---

    @Test
    fun `createPendingShare accepts all valid algorithms`() {
        val validAlgorithms = listOf("ed25519", "ecdsa-p256", "ecdsa-p384", "ecdsa-p521", "rsa-2048", "rsa-4096", "rsa-8192")

        validAlgorithms.forEach { algorithm ->
            val fakeDao = FakeKeyShareDao()
            val service = KeyService(fakeDao)

            val result = service.createPendingShare(algorithm, "ssh", null)

            assertNotNull(result, "Should accept algorithm: $algorithm")
        }
    }

    @Test
    fun `createPendingShare accepts all valid purposes`() {
        val validPurposes = listOf("ssh", "git")

        validPurposes.forEach { purpose ->
            val fakeDao = FakeKeyShareDao()
            val service = KeyService(fakeDao)

            val result = service.createPendingShare("ed25519", purpose, null)

            assertNotNull(result, "Should accept purpose: $purpose")
        }
    }

    // --- createPendingShare Tests ---

    @Test
    fun `createPendingShare returns id for valid input`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", null)

        assertNotNull(result)
        assertEquals(1, fakeDao.createdShares.size)
        assertNull(fakeDao.createdShares[0].publicKey, "Public key should be null for pending share")
    }

    @Test
    fun `createPendingShare stores label when provided`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", "My Test Label")

        assertNotNull(result)
        assertEquals("My Test Label", fakeDao.createdShares[0].label)
    }

    @Test
    fun `createPendingShare sanitizes label`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", "<script>alert('xss')</script>")

        assertNotNull(result)
        val label = fakeDao.createdShares[0].label
        assertNotNull(label)
        assertFalse(label.contains("<script>"), "Label should not contain script tags")
    }

    @Test
    fun `createPendingShare returns null for invalid algorithm`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("invalid-algo", "ssh", null)

        assertNull(result)
        assertEquals(0, fakeDao.createdShares.size)
    }

    @Test
    fun `createPendingShare returns null for invalid purpose`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "invalid-purpose", null)

        assertNull(result)
        assertEquals(0, fakeDao.createdShares.size)
    }

    // --- completeShare Tests ---

    @Test
    fun `completeShare succeeds for pending share with valid public key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        val result = service.completeShare(shareId, validEd25519Key, "ed25519")

        assertTrue(result)
        val completedShare = fakeDao.shares[shareId]
        assertNotNull(completedShare?.publicKey, "Public key should be set after completion")
    }

    @Test
    fun `completeShare fails for non-existent share`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val randomId = UUID.randomUUID()

        val result = service.completeShare(randomId, validEd25519Key, "ed25519")

        assertFalse(result)
    }

    @Test
    fun `completeShare fails for already completed share`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!
        service.completeShare(shareId, validEd25519Key, "ed25519")

        // Try to complete again
        val result = service.completeShare(shareId, validEd25519Key, "ed25519")

        assertFalse(result)
    }

    @Test
    fun `completeShare fails for algorithm mismatch`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        // Try to complete with a different algorithm
        val result = service.completeShare(shareId, validEd25519Key, "rsa-2048")

        assertFalse(result)
        val share = fakeDao.shares[shareId]
        assertNull(share?.publicKey, "Public key should still be null after failed completion")
    }

    @Test
    fun `completeShare fails for invalid public key format`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        val result = service.completeShare(shareId, "invalid-key-format", "ed25519")

        assertFalse(result)
    }

    @Test
    fun `completeShare fails when public key algorithm doesnt match share algorithm`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        // Try to complete with an RSA key (doesn't match ed25519)
        val result = service.completeShare(shareId, validRsaKey, "ed25519")

        assertFalse(result)
    }

    @Test
    fun `isPending returns true for pending share`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!

        val share = service.getShare(shareId)

        assertNotNull(share)
        assertTrue(share.isPending())
        assertFalse(share.isCompleted())
    }

    @Test
    fun `isCompleted returns true for completed share`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null)!!
        service.completeShare(shareId, validEd25519Key, "ed25519")

        val share = service.getShare(shareId)

        assertNotNull(share)
        assertFalse(share.isPending())
        assertTrue(share.isCompleted())
    }

    // --- PEM format sanitizePublicKey Tests ---

    // Sample valid PEM public keys for testing
    private val validPemEd25519Key =
        "-----BEGIN PUBLIC KEY-----\n" +
            "MCowBQYDK2VwAyEAAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=\n" +
            "-----END PUBLIC KEY-----"

    private val validPemEcdsaKey =
        "-----BEGIN PUBLIC KEY-----\n" +
            "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEAAECAwQFBgcICQoLDA0ODxAREhMU\n" +
            "FRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+Pw==\n" +
            "-----END PUBLIC KEY-----"

    private val validPemRsaKey =
        "-----BEGIN PUBLIC KEY-----\n" +
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4v\n" +
            "MDEyMzQ1Njc4OTo7PD0+P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5f\n" +
            "YGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6e3x9fn+AgYKDhIWGh4iJiouMjY6P\n" +
            "kJGSk5SVlpeYmZqbnJ2en6ChoqOkpaanqKmqq6ytrq+wsbKztLW2t7i5uru8vb6/\n" +
            "wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX2Nna29zd3t/g4eLj5OXm5+jp6uvs7e7v\n" +
            "8PHy8/T19vf4+fr7/P3+/w==\n" +
            "-----END PUBLIC KEY-----"

    @Test
    fun `sanitizePublicKey PEM accepts valid Ed25519 SPKI key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validPemEd25519Key, format = "pem")

        assertNotNull(result)
        assertTrue(result.startsWith("-----BEGIN PUBLIC KEY-----"))
        assertTrue(result.endsWith("-----END PUBLIC KEY-----"))
    }

    @Test
    fun `sanitizePublicKey PEM accepts valid ECDSA SPKI key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validPemEcdsaKey, format = "pem")

        assertNotNull(result)
        assertTrue(result.startsWith("-----BEGIN PUBLIC KEY-----"))
        assertTrue(result.endsWith("-----END PUBLIC KEY-----"))
    }

    @Test
    fun `sanitizePublicKey PEM accepts valid RSA SPKI key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizePublicKey(validPemRsaKey, format = "pem")

        assertNotNull(result)
        assertTrue(result.startsWith("-----BEGIN PUBLIC KEY-----"))
        assertTrue(result.endsWith("-----END PUBLIC KEY-----"))
    }

    @Test
    fun `sanitizePublicKey PEM rejects missing BEGIN header`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidPem = "MCowBQYDK2VwAyEAAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=\n-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(invalidPem, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM rejects missing END footer`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidPem = "-----BEGIN PUBLIC KEY-----\nMCowBQYDK2VwAyEAAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8="

        val result = service.sanitizePublicKey(invalidPem, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM rejects wrong header type`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val wrongHeader =
            "-----BEGIN PRIVATE KEY-----\n" +
                "MCowBQYDK2VwAyEAAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=\n" +
                "-----END PRIVATE KEY-----"

        val result = service.sanitizePublicKey(wrongHeader, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM rejects invalid base64 characters`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val invalidBase64 =
            "-----BEGIN PUBLIC KEY-----\n" +
                "MCow!@#\$%^&*invalid-base64-data\n" +
                "-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(invalidBase64, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM rejects body that is too short`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shortBody =
            "-----BEGIN PUBLIC KEY-----\n" +
                "AQID\n" +
                "-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(shortBody, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM rejects key that is too long`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val longBody = "A".repeat(6000) // Way over the 5000 char limit
        val tooLong =
            "-----BEGIN PUBLIC KEY-----\n" +
                longBody + "\n" +
                "-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(tooLong, format = "pem")

        assertNull(result)
    }

    @Test
    fun `sanitizePublicKey PEM normalizes line wrapping to 64 chars`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        // Send the ECDSA key with all base64 on a single line (not 64-char wrapped)
        val unwrapped =
            "-----BEGIN PUBLIC KEY-----\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEAAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1Njc4OTo7PD0+Pw==\n" +
                "-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(unwrapped, format = "pem")

        assertNotNull(result)
        // After normalization, lines between BEGIN and END should be max 64 chars
        val lines = result.lines()
        // Skip first (BEGIN) and last (END) lines
        for (i in 1 until lines.size - 1) {
            assertTrue(
                lines[i].length <= 64,
                "Line $i should be <= 64 chars, got ${lines[i].length}: '${lines[i]}'",
            )
        }
    }

    @Test
    fun `sanitizePublicKey PEM trims surrounding whitespace`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val withWhitespace = "  \n$validPemEd25519Key\n  "

        val result = service.sanitizePublicKey(withWhitespace, format = "pem")

        assertNotNull(result)
        assertTrue(result.startsWith("-----BEGIN"))
        assertTrue(result.endsWith("-----END PUBLIC KEY-----"))
    }

    @Test
    fun `sanitizePublicKey PEM rejects empty body between markers`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val emptyBody = "-----BEGIN PUBLIC KEY-----\n\n-----END PUBLIC KEY-----"

        val result = service.sanitizePublicKey(emptyBody, format = "pem")

        assertNull(result)
    }

    // --- Format validation in createPendingShare Tests ---

    @Test
    fun `createPendingShare accepts openssh format`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", null, format = "openssh")

        assertNotNull(result)
        assertEquals("openssh", fakeDao.createdShares[0].format)
    }

    @Test
    fun `createPendingShare accepts pem format`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", null, format = "pem")

        assertNotNull(result)
        assertEquals("pem", fakeDao.createdShares[0].format)
    }

    @Test
    fun `createPendingShare defaults to openssh format`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", null)

        assertNotNull(result)
        assertEquals("openssh", fakeDao.createdShares[0].format)
    }

    @Test
    fun `createPendingShare rejects invalid format`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.createPendingShare("ed25519", "ssh", null, format = "pkcs12")

        assertNull(result)
        assertEquals(0, fakeDao.createdShares.size)
    }

    // --- completeShare with PEM format Tests ---

    @Test
    fun `completeShare succeeds for PEM format share with valid PEM key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null, format = "pem")!!

        val result = service.completeShare(shareId, validPemEd25519Key, "ed25519")

        assertTrue(result)
        val completedShare = fakeDao.shares[shareId]
        assertNotNull(completedShare?.publicKey)
        assertTrue(completedShare!!.publicKey!!.startsWith("-----BEGIN PUBLIC KEY-----"))
    }

    @Test
    fun `completeShare fails for PEM format share with OpenSSH key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null, format = "pem")!!

        // Try to complete PEM share with OpenSSH-format key
        val result = service.completeShare(shareId, validEd25519Key, "ed25519")

        assertFalse(result, "Should reject OpenSSH key when share format is PEM")
    }

    @Test
    fun `completeShare fails for OpenSSH format share with PEM key`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val shareId = service.createPendingShare("ed25519", "ssh", null, format = "openssh")!!

        // Try to complete OpenSSH share with PEM-format key
        val result = service.completeShare(shareId, validPemEd25519Key, "ed25519")

        assertFalse(result, "Should reject PEM key when share format is OpenSSH")
    }

    // --- sanitizeLabel Tests ---

    @Test
    fun `sanitizeLabel returns null for blank label`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizeLabel("   ")

        assertNull(result)
    }

    @Test
    fun `sanitizeLabel returns null for null label`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizeLabel(null)

        assertNull(result)
    }

    @Test
    fun `sanitizeLabel removes HTML tags`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)

        val result = service.sanitizeLabel("<b>bold</b> label")

        assertNotNull(result)
        assertFalse(result.contains("<b>"))
        assertFalse(result.contains("</b>"))
    }

    @Test
    fun `sanitizeLabel truncates to 256 chars`() {
        val fakeDao = FakeKeyShareDao()
        val service = KeyService(fakeDao)
        val longLabel = "a".repeat(500)

        val result = service.sanitizeLabel(longLabel)

        assertNotNull(result)
        assertTrue(result.length <= 256)
    }
}

package de.mw.services

import de.mw.daos.IKeyShareDao
import de.mw.models.SharePublicKey
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

class KeyService(
    private val keyShareDao: IKeyShareDao,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Valid algorithms that we support
    private val validAlgorithms = setOf("ed25519", "ecdsa-p256", "ecdsa-p384", "rsa-2048", "rsa-4096", "rsa-8192")

    // Valid purposes
    private val validPurposes = setOf("ssh", "git")

    // Valid key formats
    private val validFormats = setOf("openssh", "pem")

    // Map user-selected algorithm to expected SSH key prefix
    private val algorithmToKeyPrefix =
        mapOf(
            "ed25519" to "ssh-ed25519",
            "ecdsa-p256" to "ecdsa-sha2-nistp256",
            "ecdsa-p384" to "ecdsa-sha2-nistp384",
            "rsa-2048" to "ssh-rsa",
            "rsa-4096" to "ssh-rsa",
            "rsa-8192" to "ssh-rsa",
        )

    /**
     * Validates and sanitizes a public key to prevent XSS attacks.
     * Dispatches to format-specific validation based on the [format] parameter.
     *
     * @param publicKey The raw public key string
     * @param expectedAlgorithm The algorithm the user selected (to cross-validate)
     * @param format The key format: "openssh" or "pem"
     * @return Sanitized public key or null if validation fails
     */
    fun sanitizePublicKey(
        publicKey: String,
        expectedAlgorithm: String? = null,
        format: String = "openssh",
    ): String? =
        when (format) {
            "pem" -> sanitizePemPublicKey(publicKey, expectedAlgorithm)
            else -> sanitizeOpenSshPublicKey(publicKey, expectedAlgorithm)
        }

    /**
     * Validates and sanitizes an OpenSSH-format public key.
     * OpenSSH public keys have a specific format: algorithm base64-data [comment]
     * We validate the structure and sanitize the comment portion.
     */
    private fun sanitizeOpenSshPublicKey(
        publicKey: String,
        expectedAlgorithm: String? = null,
    ): String? {
        val trimmed = publicKey.trim()

        // Public keys should be reasonably sized (max ~4KB for RSA-8192)
        if (trimmed.length > 5000) {
            logger.warn("Public key rejected: too long (${trimmed.length} chars)")
            return null
        }

        // Basic structure validation: should start with a known algorithm prefix
        val validPrefixes =
            listOf(
                "ssh-ed25519",
                "ecdsa-sha2-nistp256",
                "ecdsa-sha2-nistp384",
                "ssh-rsa",
            )

        val keyPrefix = validPrefixes.find { trimmed.startsWith(it) }
        if (keyPrefix == null) {
            logger.warn("Public key rejected: invalid algorithm prefix")
            return null
        }

        // Cross-validate that the key matches the expected algorithm
        if (expectedAlgorithm != null) {
            val expectedPrefix = algorithmToKeyPrefix[expectedAlgorithm]
            if (expectedPrefix != null && keyPrefix != expectedPrefix) {
                logger.warn("Public key rejected: algorithm mismatch - expected $expectedPrefix but got $keyPrefix")
                return null
            }
        }

        // Split into parts: algorithm, base64-data, [optional comment]
        val parts = trimmed.split(" ", limit = 3)
        if (parts.size < 2) {
            logger.warn("Public key rejected: missing base64 data")
            return null
        }

        val algorithm = parts[0]
        val base64Data = parts[1]

        // Validate base64 data contains only valid base64 characters
        if (!base64Data.matches(Regex("^[A-Za-z0-9+/]+=*$"))) {
            logger.warn("Public key rejected: invalid base64 data")
            return null
        }

        // If there's a comment, sanitize it (don't HTML-encode - kotlinx-html will handle that)
        val sanitizedComment =
            if (parts.size == 3) {
                sanitizeComment(parts[2])
            } else {
                null
            }

        // Reconstruct the key with sanitized comment
        return if (sanitizedComment != null) {
            "$algorithm $base64Data $sanitizedComment"
        } else {
            "$algorithm $base64Data"
        }
    }

    /**
     * Validates and sanitizes a PEM-format public key (SPKI).
     * PEM public keys have the structure:
     * -----BEGIN PUBLIC KEY-----
     * <base64 DER data, 64-char lines>
     * -----END PUBLIC KEY-----
     *
     * We validate the structure and normalize the content. Algorithm cross-validation
     * is not done at the PEM level since the algorithm is encoded inside the DER data.
     */
    private fun sanitizePemPublicKey(
        publicKey: String,
        expectedAlgorithm: String? = null,
    ): String? {
        val trimmed = publicKey.trim()

        // PEM keys should be reasonably sized (max ~4KB for RSA-8192 SPKI)
        if (trimmed.length > 5000) {
            logger.warn("PEM public key rejected: too long (${trimmed.length} chars)")
            return null
        }

        // Must start with BEGIN PUBLIC KEY and end with END PUBLIC KEY
        if (!trimmed.startsWith("-----BEGIN PUBLIC KEY-----")) {
            logger.warn("PEM public key rejected: missing BEGIN header")
            return null
        }
        if (!trimmed.endsWith("-----END PUBLIC KEY-----")) {
            logger.warn("PEM public key rejected: missing END footer")
            return null
        }

        // Extract the base64 body between the markers
        val bodyStart = trimmed.indexOf('\n')
        val bodyEnd = trimmed.lastIndexOf('\n')
        if (bodyStart == -1 || bodyEnd <= bodyStart) {
            logger.warn("PEM public key rejected: invalid structure")
            return null
        }

        val base64Body = trimmed.substring(bodyStart + 1, bodyEnd).trim()

        // Validate that the body contains only valid base64 characters and newlines
        val base64Continuous = base64Body.replace("\n", "").replace("\r", "")
        if (!base64Continuous.matches(Regex("^[A-Za-z0-9+/]+=*$"))) {
            logger.warn("PEM public key rejected: invalid base64 body")
            return null
        }

        // Must have a non-trivial body (at least 16 bytes encoded = ~22 chars)
        if (base64Continuous.length < 20) {
            logger.warn("PEM public key rejected: body too short")
            return null
        }

        // Normalize: re-wrap base64 at 64-char lines
        val normalizedBase64 = base64Continuous.chunked(64).joinToString("\n")

        return "-----BEGIN PUBLIC KEY-----\n$normalizedBase64\n-----END PUBLIC KEY-----"
    }

    /**
     * Sanitizes the comment portion of a public key.
     * Removes dangerous characters but does NOT HTML-encode
     * (kotlinx-html will escape when rendering).
     */
    private fun sanitizeComment(comment: String): String {
        // Remove HTML tags and dangerous characters, keep it raw for storage
        // kotlinx-html will escape on render, so we just remove dangerous patterns
        return comment
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[<>\"']"), "_") // Replace remaining dangerous chars with underscore
            .filter { it.code in 0x20..0x7E } // Only printable ASCII
            .take(256) // Limit comment length
    }

    /**
     * Sanitizes a label for display.
     */
    fun sanitizeLabel(label: String?): String? {
        if (label.isNullOrBlank()) return null
        return label
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[<>\"']"), "_") // Replace dangerous chars
            .filter { it.code in 0x20..0x7E } // Only printable ASCII
            .take(256)
            .trim()
            .ifBlank { null }
    }

    /**
     * Creates a new PENDING key share request.
     * The admin specifies algorithm, purpose, format, and optional label.
     * No public key yet - it will be generated by the recipient.
     *
     * @return The share ID
     */
    fun createPendingShare(
        algorithm: String,
        purpose: String,
        label: String?,
        format: String = "openssh",
    ): UUID? {
        // Validate algorithm
        if (algorithm !in validAlgorithms) {
            logger.warn("Invalid algorithm: $algorithm")
            return null
        }

        // Validate purpose
        if (purpose !in validPurposes) {
            logger.warn("Invalid purpose: $purpose")
            return null
        }

        // Validate format
        if (format !in validFormats) {
            logger.warn("Invalid format: $format")
            return null
        }

        val sanitizedLabel = sanitizeLabel(label)

        val id = UUID.randomUUID()
        val sharePublicKey =
            SharePublicKey(
                id = id,
                created = LocalDateTime.now(),
                publicKey = null, // Will be set when recipient generates the key
                algorithm = algorithm,
                purpose = purpose,
                label = sanitizedLabel,
                completedAt = null,
                format = format,
            )

        keyShareDao.createShare(sharePublicKey)
        logger.info("Created pending key share: $id (algorithm=$algorithm, purpose=$purpose, format=$format, label=$sanitizedLabel)")
        return id
    }

    /**
     * Completes a pending share by adding the public key.
     * Called when the recipient generates the key on their machine.
     *
     * @param id The share ID
     * @param publicKey The generated public key
     * @param algorithm The algorithm (must match the share's algorithm)
     * @return true if successful, false if validation fails or share is already completed
     */
    fun completeShare(
        id: UUID,
        publicKey: String,
        algorithm: String,
    ): Boolean {
        // First, verify the share exists and the algorithm matches
        val share = keyShareDao.getShare(id)
        if (share == null) {
            logger.warn("Share not found: $id")
            return false
        }

        if (share.isCompleted()) {
            logger.warn("Share already completed: $id")
            return false
        }

        if (share.algorithm != algorithm) {
            logger.warn("Algorithm mismatch for share $id: expected ${share.algorithm}, got $algorithm")
            return false
        }

        // Sanitize the public key with algorithm cross-validation, using the share's format
        val sanitizedKey = sanitizePublicKey(publicKey, algorithm, share.format)
        if (sanitizedKey == null) {
            logger.warn("Public key sanitization failed for share $id")
            return false
        }

        val success = keyShareDao.completeShare(id, sanitizedKey)
        if (success) {
            logger.info("Completed key share: $id")
        } else {
            logger.warn("Failed to complete key share $id (may already be completed or not found)")
        }
        return success
    }

    /**
     * Retrieves a key share by ID.
     */
    fun getShare(id: UUID): SharePublicKey? = keyShareDao.getShare(id)

    /**
     * Deletes a key share.
     */
    fun deleteShare(id: UUID) {
        keyShareDao.deleteShare(id)
        logger.info("Deleted key share: $id")
    }
}

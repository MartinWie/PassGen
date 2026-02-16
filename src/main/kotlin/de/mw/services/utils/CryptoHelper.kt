package de.mw.services.utils

import org.slf4j.LoggerFactory
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class CryptoHelper {
    companion object {
        private const val IV_LENGTH = 12
        private const val KEY_LENGTH = 256

        // 10,000 iterations is sufficient here because the input "password" is a UUID (122 bits of
        // entropy from SecureRandom), not a user-chosen password. OWASP's recommendation of 600,000+
        // iterations targets low-entropy human passwords; with a random UUID, even 1 iteration would
        // make brute-force infeasible (2^122 / attempts-per-sec = astronomical). The real security of
        // password shares depends on the share URL (containing both UUIDs) staying secret.
        private const val ITERATIONS = 10000
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private val logger = LoggerFactory.getLogger(this::class.java)

        fun encrypt(
            plainText: String,
            token: String,
            salt: String,
        ): String {
            val secretKey = deriveKey(token, salt)

            // Get a random initialisation vector
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)

            // Encryption magic
            val cypher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(128, iv)
            cypher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            val encryptedBytes = cypher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine IV + encrypted data(needed for decryption later)
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined)
        }

        fun decrypt(
            encryptedText: String,
            token: String,
            salt: String,
        ): String? {
            val combined = Base64.getUrlDecoder().decode(encryptedText)

            val iv = ByteArray(IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)

            val encryptedBytes = ByteArray(combined.size - iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)

            val secretKey = deriveKey(token, salt)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            try {
                val decryptedBytes = cipher.doFinal(encryptedBytes)
                return String(decryptedBytes, Charsets.UTF_8)
            } catch (e: Exception) {
                logger.error("Invalid input for decryption.", e)
                return null
            }
        }

        // Derive a strong encryption key from the UUID token and salt
        private fun deriveKey(
            token: String,
            salt: String,
        ): SecretKey {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val tokenChars = token.toCharArray()
            val saltBytes = salt.toByteArray()
            val spec = PBEKeySpec(tokenChars, saltBytes, ITERATIONS, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            spec.clearPassword() // Zero the internal char array
            tokenChars.fill('\u0000') // Zero our copy of the token
            saltBytes.fill(0) // Zero our copy of the salt
            val encoded = secretKey.encoded
            val result = SecretKeySpec(encoded, "AES")
            encoded.fill(0) // Zero the intermediate byte array
            return result
        }
    }
}

package de.mw.services.utils

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
        private val IV_LENGTH = 12
        private val KEY_LENGTH = 256
        private val ITERATIONS = 10000
        private val TRANSFORMATION = "AES/GCM/NoPadding"

        fun encrypt(plainText: String, token: String, salt: String): String {
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

        fun decrypt(encryptedText: String, token: String, salt: String): String {
            return "" // TODO: implement(extract IV and data, then derive key and decrypt)
        }

        // Derive a strong encryption key from the UUID token and salt
        private fun deriveKey(token: String, salt: String): SecretKey {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(token.toCharArray(),salt.toByteArray(), ITERATIONS, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            return SecretKeySpec(secretKey.encoded, "AES")
        }
    }
}
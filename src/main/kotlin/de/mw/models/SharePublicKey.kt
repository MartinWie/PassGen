package de.mw.models

import java.time.LocalDateTime
import java.util.*

data class SharePublicKey(
    val id: UUID,
    val created: LocalDateTime,
    val publicKey: String?, // NULL until recipient generates the key
    val algorithm: String,
    val purpose: String,
    val label: String?, // Optional label set by admin to identify this share
    val completedAt: LocalDateTime?, // Set when recipient generates and submits the public key
    val format: String = "openssh", // Key format: "openssh" or "pem"
) {
    /**
     * Returns true if this share is still pending (recipient hasn't generated key yet)
     */
    fun isPending(): Boolean = publicKey == null

    /**
     * Returns true if this share has been completed (public key has been submitted)
     */
    fun isCompleted(): Boolean = publicKey != null
}

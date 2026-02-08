package de.mw.daos

import de.mw.models.SharePublicKey
import java.util.*

interface IKeyShareDao {
    fun createShare(sharePublicKey: SharePublicKey): String

    fun getShare(id: UUID): SharePublicKey?

    fun deleteShare(id: UUID)

    /**
     * Completes a pending share by adding the public key.
     * Returns true if the share was updated, false if it was already completed or not found.
     */
    fun completeShare(id: UUID, publicKey: String): Boolean
}

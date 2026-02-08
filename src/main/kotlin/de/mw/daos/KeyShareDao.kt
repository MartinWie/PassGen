package de.mw.daos

import de.mw.generated.Tables
import de.mw.generated.tables.records.SharePublicKeyRecord
import de.mw.models.SharePublicKey
import org.jooq.DSLContext
import java.util.*

class KeyShareDao(
    private val dsl: DSLContext,
) : IKeyShareDao {
    override fun createShare(sharePublicKey: SharePublicKey): String {
        val record = sharePublicKey.toRecord()
        dsl.insertInto(Tables.SHARE_PUBLIC_KEY).set(record).execute()
        return sharePublicKey.id.toString()
    }

    override fun getShare(id: UUID): SharePublicKey? =
        dsl
            .selectFrom(Tables.SHARE_PUBLIC_KEY)
            .where(Tables.SHARE_PUBLIC_KEY.ID.eq(id))
            .fetchOne()
            ?.toModel()

    override fun deleteShare(id: UUID) {
        dsl
            .delete(Tables.SHARE_PUBLIC_KEY)
            .where(Tables.SHARE_PUBLIC_KEY.ID.eq(id))
            .execute()
    }

    /**
     * Completes a pending share by adding the public key.
     * Returns true if the share was updated, false if it was already completed or not found.
     */
    override fun completeShare(id: UUID, publicKey: String): Boolean {
        val updatedRows = dsl
            .update(Tables.SHARE_PUBLIC_KEY)
            .set(Tables.SHARE_PUBLIC_KEY.PUBLIC_KEY, publicKey)
            .set(Tables.SHARE_PUBLIC_KEY.COMPLETED_AT, java.time.LocalDateTime.now())
            .where(Tables.SHARE_PUBLIC_KEY.ID.eq(id))
            .and(Tables.SHARE_PUBLIC_KEY.PUBLIC_KEY.isNull) // Only update if still pending
            .execute()
        return updatedRows > 0
    }

    private fun SharePublicKey.toRecord() =
        SharePublicKeyRecord().apply {
            setId(this@toRecord.id)
            setCreated(this@toRecord.created)
            setPublicKey(this@toRecord.publicKey)
            setAlgorithm(this@toRecord.algorithm)
            setPurpose(this@toRecord.purpose)
            setLabel(this@toRecord.label)
            setCompletedAt(this@toRecord.completedAt)
        }

    private fun SharePublicKeyRecord.toModel() =
        SharePublicKey(
            id = this.id,
            created = this.created,
            publicKey = this.publicKey,
            algorithm = this.algorithm,
            purpose = this.purpose,
            label = this.label,
            completedAt = this.completedAt,
        )
}

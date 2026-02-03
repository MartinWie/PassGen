package de.mw.daos

import de.mw.generated.Tables
import de.mw.generated.Tables.PASSWORDWORD
import de.mw.generated.tables.records.SharePasswordRecord
import de.mw.models.SharePassword
import de.mw.models.WordLanguage
import org.jooq.DSLContext
import java.math.BigDecimal
import java.util.*

class PasswordDao(
    private val dsl: DSLContext,
) : IPasswordDao {
    override fun get(
        amount: Int,
        language: WordLanguage,
    ): List<String> {
        val count =
            dsl
                .selectCount()
                .from(PASSWORDWORD)
                .where(PASSWORDWORD.LANGUAGE.eq(language.name))
                .fetchOneInto(Int::class.java) ?: 0

        if (count == 0) return emptyList()

        val offsets = (0 until amount).map { (Math.random() * count).toInt() }

        return offsets.mapNotNull { offset ->
            dsl
                .select(PASSWORDWORD.VALUE)
                .from(PASSWORDWORD)
                .where(PASSWORDWORD.LANGUAGE.eq(language.name))
                .limit(1)
                .offset(offset)
                .fetchOne(PASSWORDWORD.VALUE)
        }
    }

    override fun insert(
        words: List<String>,
        language: WordLanguage,
    ) {
        dsl
            .batch(
                words.map { value ->
                    dsl
                        .insertInto(PASSWORDWORD)
                        .set(PASSWORDWORD.VALUE, value)
                        .set(PASSWORDWORD.LANGUAGE, language.name)
                },
            ).execute()
    }

    override fun createShare(sharePassword: SharePassword): String {
        val record = sharePassword.toRecord()
        dsl.insertInto(Tables.SHARE_PASSWORD).set(record).execute()
        return sharePassword.id.toString()
    }

    override fun getShare(id: UUID): SharePassword? =
        dsl
            .selectFrom(Tables.SHARE_PASSWORD)
            .where(Tables.SHARE_PASSWORD.ID.eq(id))
            .fetchOne()
            ?.toModel()

    override fun setRemainingViewsShare(
        id: UUID,
        amount: BigDecimal,
    ) {
        dsl
            .update(Tables.SHARE_PASSWORD)
            .set(Tables.SHARE_PASSWORD.REMAINING_VIEWS, amount)
            .where(Tables.SHARE_PASSWORD.ID.eq(id))
            .execute()
    }

    override fun deleteShare(id: UUID) {
        dsl
            .delete(Tables.SHARE_PASSWORD)
            .where(Tables.SHARE_PASSWORD.ID.eq(id))
            .execute()
    }

    private fun SharePassword.toRecord() =
        SharePasswordRecord().apply {
            setId(this@toRecord.id)
            setCreated(this@toRecord.created)
            setValue(this@toRecord.value)
            setRemainingViews(this@toRecord.remainingViews)
        }

    private fun SharePasswordRecord.toModel() =
        SharePassword(
            id = this.id,
            created = this.created,
            value = this.value,
            remainingViews = this.remainingViews,
        )
}

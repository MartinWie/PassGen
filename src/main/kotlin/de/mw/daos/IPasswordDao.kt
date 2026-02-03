package de.mw.daos

import de.mw.models.SharePassword
import de.mw.models.WordLanguage
import java.math.BigDecimal
import java.util.*

/**
 * Interface for password data access operations.
 * Allows for easy mocking/faking in tests.
 */
interface IPasswordDao {
    fun get(
        amount: Int = 1,
        language: WordLanguage,
    ): List<String>

    fun insert(
        words: List<String>,
        language: WordLanguage,
    )

    fun createShare(sharePassword: SharePassword): String

    fun getShare(id: UUID): SharePassword?

    fun setRemainingViewsShare(
        id: UUID,
        amount: BigDecimal,
    )

    fun deleteShare(id: UUID)
}

package de.mw.daos

import de.mw.models.SharePassword
import de.mw.models.WordLanguage
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

    /**
     * Atomically decrements remaining_views by 1 and returns the share (with the NEW, post-decrement
     * remaining_views) only if remaining_views > 0. Returns null if the share doesn't exist or has
     * no views left. This prevents a race condition where concurrent requests could both view a
     * one-time share.
     */
    fun decrementAndGetShare(id: UUID): SharePassword?

    fun deleteShare(id: UUID)
}

package de.mw.services

import de.mw.daos.IPasswordDao
import de.mw.models.SharePassword
import de.mw.models.WordLanguage
import de.mw.services.utils.SPECIAL_CHARS
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Fake implementation of IPasswordDao for testing PasswordService.
 * Allows controlled responses and tracking of method calls.
 */
class FakePasswordDao : IPasswordDao {
    var wordsToReturn: Map<WordLanguage, List<String>> =
        mapOf(
            WordLanguage.ENG to listOf("apple", "banana", "cherry", "date", "elderberry", "fig", "grape"),
            WordLanguage.GER to listOf("Apfel", "Birne", "Kirsche", "Dattel", "Holunder", "Feige", "Traube"),
        )

    var insertedWords: MutableList<Pair<List<String>, WordLanguage>> = mutableListOf()
    var createdShares: MutableList<SharePassword> = mutableListOf()
    var shares: MutableMap<UUID, SharePassword> = mutableMapOf()
    var deletedShareIds: MutableList<UUID> = mutableListOf()

    override fun get(
        amount: Int,
        language: WordLanguage,
    ): List<String> {
        val words = wordsToReturn[language] ?: emptyList()
        return words.take(amount.coerceAtMost(words.size))
    }

    override fun insert(
        words: List<String>,
        language: WordLanguage,
    ) {
        insertedWords.add(words to language)
    }

    override fun createShare(sharePassword: SharePassword): String {
        createdShares.add(sharePassword)
        shares[sharePassword.id] = sharePassword
        return sharePassword.id.toString()
    }

    // Note: This fake is not thread-safe. The real DAO uses atomic SQL operations.
    // For concurrency testing, use integration tests against a real database.
    override fun decrementAndGetShare(id: UUID): SharePassword? {
        val existing = shares[id] ?: return null
        if (existing.remainingViews <= BigDecimal.ZERO) return null
        val newViews = existing.remainingViews.minus(BigDecimal.ONE)
        val updated = existing.copy(remainingViews = newViews)
        shares[id] = updated
        // Return the post-decrement state (matching real DAO behavior)
        return updated
    }

    override fun deleteShare(id: UUID) {
        deletedShareIds.add(id)
        shares.remove(id)
    }
}

class PasswordServiceTest {
    @Test
    fun `getWords returns correct number of words`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.ENG, specialChars = false, numbers = false)

        assertEquals(3, words.size)
    }

    @Test
    fun `getWords with single word returns one word`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 1, language = WordLanguage.ENG, specialChars = false, numbers = false)

        assertEquals(1, words.size)
    }

    @Test
    fun `getWords respects language parameter for English`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.ENG, specialChars = false, numbers = false)

        // Words should come from English list
        val englishWords = fakeDao.wordsToReturn[WordLanguage.ENG]!!
        words.forEach { word ->
            assertTrue(englishWords.contains(word), "Word '$word' should be in English word list")
        }
    }

    @Test
    fun `getWords respects language parameter for German`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.GER, specialChars = false, numbers = false)

        // Words should come from German list
        val germanWords = fakeDao.wordsToReturn[WordLanguage.GER]!!
        words.forEach { word ->
            assertTrue(germanWords.contains(word), "Word '$word' should be in German word list")
        }
    }

    @Test
    fun `getWords with specialChars appends special character to each word`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.ENG, specialChars = true, numbers = false)

        words.forEach { word ->
            val lastChar = word.last()
            assertTrue(
                SPECIAL_CHARS.contains(lastChar),
                "Word '$word' should end with a special character from SPECIAL_CHARS",
            )
        }
    }

    @Test
    fun `getWords with numbers appends digit to each word`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.ENG, specialChars = false, numbers = true)

        words.forEach { word ->
            val lastChar = word.last()
            assertTrue(lastChar.isDigit(), "Word '$word' should end with a digit")
        }
    }

    @Test
    fun `getWords with both specialChars and numbers appends both`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val words = service.getWords(amount = 3, language = WordLanguage.ENG, specialChars = true, numbers = true)

        words.forEach { word ->
            val lastChar = word.last()
            val secondLastChar = word[word.length - 2]
            assertTrue(lastChar.isDigit(), "Word '$word' should end with a digit")
            assertTrue(
                SPECIAL_CHARS.contains(secondLastChar),
                "Word '$word' should have a special character before the digit",
            )
        }
    }

    @Test
    fun `insertWords delegates to dao`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val wordsToInsert = listOf("test1", "test2", "test3")

        service.insertWords(wordsToInsert, WordLanguage.ENG)

        assertEquals(1, fakeDao.insertedWords.size)
        assertEquals(wordsToInsert to WordLanguage.ENG, fakeDao.insertedWords[0])
    }

    @Test
    fun `createShare returns id and salt pair`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        val result = service.createShare("my-secret-password")

        assertNotNull(result)
        val (id, salt) = result
        assertNotNull(id)
        assertNotNull(salt)
    }

    @Test
    fun `createShare creates share in dao`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        service.createShare("my-secret-password")

        assertEquals(1, fakeDao.createdShares.size)
    }

    @Test
    fun `createShare with custom remainingViews`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)

        service.createShare("my-secret-password", remainingViews = BigDecimal(5))

        assertEquals(1, fakeDao.createdShares.size)
        assertEquals(BigDecimal(5), fakeDao.createdShares[0].remainingViews)
    }

    @Test
    fun `createShare returns null for value exceeding 5000 chars`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val longValue = "A".repeat(5001)

        val result = service.createShare(longValue)

        assertNull(result)
        assertEquals(0, fakeDao.createdShares.size)
    }

    @Test
    fun `createShare accepts value of exactly 5000 chars`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val maxValue = "A".repeat(5000)

        val result = service.createShare(maxValue)

        assertNotNull(result)
        assertEquals(1, fakeDao.createdShares.size)
    }

    @Test
    fun `getShare returns decrypted value`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val originalValue = "my-secret-password"

        val (id, salt) = service.createShare(originalValue)!!

        val retrievedValue = service.getShare(id, salt)

        assertEquals(originalValue, retrievedValue)
    }

    @Test
    fun `getShare returns null for non-existent share`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val randomId = UUID.randomUUID()
        val randomSalt = UUID.randomUUID()

        val result = service.getShare(randomId, randomSalt)

        assertNull(result)
    }

    @Test
    fun `getShare deletes share when remainingViews becomes zero`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, salt) = service.createShare("secret", remainingViews = BigDecimal.ONE)!!

        service.getShare(id, salt)

        assertTrue(fakeDao.deletedShareIds.contains(id), "Share should be deleted after last view")
    }

    @Test
    fun `getShare decrements remainingViews when more than one`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, salt) = service.createShare("secret", remainingViews = BigDecimal(3))!!

        service.getShare(id, salt)

        // After one view, remaining_views should be decremented from 3 to 2
        assertEquals(BigDecimal(2), fakeDao.shares[id]?.remainingViews)
    }

    @Test
    fun `getShare with wrong salt returns null due to decryption failure`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, _) = service.createShare("secret", remainingViews = BigDecimal(3))!!
        val wrongSalt = UUID.randomUUID()

        val result = service.getShare(id, wrongSalt)

        assertNull(result)
        // A wrong-salt attempt still consumes a view (security-positive: prevents brute-forcing salt)
        assertEquals(BigDecimal(2), fakeDao.shares[id]?.remainingViews)
    }

    @Test
    fun `multiple getShare calls decrement views correctly`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, salt) = service.createShare("secret", remainingViews = BigDecimal(3))!!

        // First view: 3 -> 2
        service.getShare(id, salt)
        assertEquals(BigDecimal(2), fakeDao.shares[id]?.remainingViews)

        // Second view: 2 -> 1
        service.getShare(id, salt)
        assertEquals(BigDecimal(1), fakeDao.shares[id]?.remainingViews)

        // Third view: 1 -> 0 (deleted)
        service.getShare(id, salt)
        assertTrue(fakeDao.deletedShareIds.contains(id))
    }

    @Test
    fun `getShare returns null when remainingViews is already zero`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, salt) = service.createShare("secret", remainingViews = BigDecimal.ONE)!!

        // First view consumes the last view and deletes the share
        val firstResult = service.getShare(id, salt)
        assertNotNull(firstResult)

        // Second view should return null (share has been deleted)
        val secondResult = service.getShare(id, salt)
        assertNull(secondResult)
    }

    @Test
    fun `getShare with wrong salt exhausts views and deletes share`() {
        val fakeDao = FakePasswordDao()
        val service = PasswordService(fakeDao)
        val (id, salt) = service.createShare("secret", remainingViews = BigDecimal.ONE)!!
        val wrongSalt = UUID.randomUUID()

        // Wrong salt consumes the only view and deletes the share
        val wrongResult = service.getShare(id, wrongSalt)
        assertNull(wrongResult)
        assertTrue(fakeDao.deletedShareIds.contains(id), "Share should be deleted after wrong-salt exhausted last view")

        // Correct salt now fails because share is gone
        val correctResult = service.getShare(id, salt)
        assertNull(correctResult)
    }
}

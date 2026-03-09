package de.mw.services

import de.mw.daos.IPasswordDao
import de.mw.models.SharePassword
import de.mw.models.WordLanguage
import de.mw.services.utils.CryptoHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PasswordService(
    private val passwordDao: IPasswordDao,
) : CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Use @Volatile to ensure visibility across threads when cachedWords is reassigned
    @Volatile
    private var cachedWords: List<List<String>> = emptyList()

    @Volatile
    private var lastRefreshTime: Long = 0
    private val refreshIntervalMs = 1 * 60 * 1000 // 1 min

    @Volatile
    private var refreshInProgress = false

    init {
        // Initialize cache after constructor
        loadWords()
        lastRefreshTime = System.currentTimeMillis()
    }

    /**
     * Returns up to [maxPerLanguage] words per language from the cached in-memory lists.
     * Intended for client-side password generation bootstrap data.
     */
    fun getWordLists(maxPerLanguage: Int = 500): Map<WordLanguage, List<String>> {
        launch {
            refreshCacheIfNeeded()
        }
        val limit = maxPerLanguage.coerceAtLeast(1)
        return WordLanguage.entries.associateWith { language ->
            cachedWords.getOrElse(language.ordinal) { emptyList() }.take(limit)
        }
    }

    fun insertWords(
        words: List<String>,
        language: WordLanguage,
    ) {
        logger.info("Inserting ${words.size} words in language ${language.name}")
        try {
            passwordDao.insert(words, language)
            logger.info("Successfully inserted ${words.size} words")
        } catch (e: Exception) {
            logger.error("Failed to insert words", e)
            throw e
        }
    }

    private fun refreshCacheIfNeeded() {
        synchronized(this@PasswordService) {
            if (!refreshInProgress && System.currentTimeMillis() - lastRefreshTime > refreshIntervalMs) {
                refreshInProgress = true
                try {
                    fetchedWords()
                } finally {
                    refreshInProgress = false
                }
            }
        }
    }

    private fun loadWords() {
        cachedWords =
            WordLanguage.entries.map {
                logger.info("Initial loading of 500 words in language ${it.name}")
                try {
                    passwordDao.get(500, it)
                } catch (e: Exception) {
                    logger.error("Failed to load words for ${it.name}", e)
                    emptyList()
                }
            }
    }

    private fun fetchedWords() {
        lastRefreshTime = System.currentTimeMillis()
        logger.info("Word cache refreshed after ${refreshIntervalMs / 60000} minutes!")
        cachedWords =
            WordLanguage.entries.map {
                try {
                    passwordDao.get(500, it)
                } catch (e: Exception) {
                    logger.error("Failed to refresh cache for ${it.name}", e)
                    cachedWords.getOrElse(it.ordinal) { emptyList() }
                }
            }
    }

    fun createShare(
        value: String,
        remainingViews: BigDecimal = BigDecimal.ONE,
    ): Pair<UUID, UUID>? {
        if (value.length > 5000) return null
        val id = UUID.randomUUID()
        val salt = UUID.randomUUID()
        val valueCrypted = CryptoHelper.encrypt(value, id.toString(), salt.toString())
        val sharePassword = SharePassword(id, LocalDateTime.now(), valueCrypted, remainingViews)
        passwordDao.createShare(sharePassword)
        return id to salt
    }

    fun getShare(
        id: UUID,
        salt: UUID,
    ): String? {
        val share = passwordDao.decrementAndGetShare(id) ?: return null

        val decryptedValue =
            CryptoHelper.decrypt(
                share.value,
                id.toString(),
                salt.toString(),
            )

        // The share's remainingViews is the post-decrement value.
        // If no views remain, clean up the row.
        if (share.remainingViews <= BigDecimal.ZERO) {
            passwordDao.deleteShare(id)
        }

        return decryptedValue
    }
}

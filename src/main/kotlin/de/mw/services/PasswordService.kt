package de.mw.services

import de.mw.daos.PasswordDao
import de.mw.models.WordLanguage
import de.mw.services.utils.SPECIAL_CHARS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.random.Random

class PasswordService(private val passwordDao: PasswordDao) : CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var cachedWords: List<List<String>> = emptyList()
    private var lastRefreshTime: Long = 0
    private val refreshIntervalMs = 1 * 60 * 1000 // 1 min
    private var refreshInProgress = false

    init {
        // Initialize cache after constructor
        loadWords()
        lastRefreshTime = System.currentTimeMillis()
    }

    fun getWords(
        amount: Int = 1,
        language: WordLanguage,
        specialChars: Boolean,
        numbers: Boolean
    ): List<String> {
        logger.info(
            "Retrieving $amount words in language ${language.name} include special chars: $specialChars, include numbers: $numbers"
        )
        launch {
            synchronized(this@PasswordService) {
                if (!refreshInProgress) {
                    refreshInProgress = true
                    launch {
                        try {
                            fetchedWords()
                        } finally {
                            synchronized(this@PasswordService) {
                                refreshInProgress = false
                            }
                        }
                    }
                }
            }
        }
        var words = cachedWords[language.ordinal].shuffled().take(amount)

        if(specialChars) {
            words = words.map {
                it+SPECIAL_CHARS[Random.nextInt(SPECIAL_CHARS.length)]
            }
        }

        if(numbers) {
            words = words.map {
                it+Random.nextInt(0,10)
            }
        }

        return words
    }

    fun insertWords(words: List<String>, language: WordLanguage) {
        logger.info("Inserting ${words.size} words in language ${language.name}")
        try {
            passwordDao.insert(words, language)
            logger.info("Successfully inserted ${words.size} words")
        } catch (e: Exception) {
            logger.error("Failed to insert words", e)
            throw e
        }
    }

    private fun loadWords() {
        cachedWords = WordLanguage.entries.map {
            logger.info("Initial loading of 50 words in language ${it.name}")
            try {
                passwordDao.get(50, it)
            } catch (e: Exception) {
                logger.error("Failed to load words for ${it.name}", e)
                emptyList()
            }
        }
    }

    private fun fetchedWords() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime > refreshIntervalMs) {
            lastRefreshTime = currentTime
            logger.info("Word cache refreshed after ${refreshIntervalMs / 60000} minutes!")
            cachedWords = WordLanguage.entries.map {
                try {
                    passwordDao.get(500, it)
                } catch (e: Exception) {
                    logger.error("Failed to refresh cache for ${it.name}", e)
                    cachedWords.getOrElse(it.ordinal) { emptyList() }
                }
            }
        }
    }
}
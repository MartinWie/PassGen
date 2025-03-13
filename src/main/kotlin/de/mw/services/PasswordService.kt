package de.mw.services

import de.mw.daos.PasswordDao
import de.mw.models.WordLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class PasswordService(private val passwordDao: PasswordDao) : CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var cachedWords: List<List<String>> = emptyList()
    private val counter = AtomicInteger(20)

    init {
        // Initialize cache after constructor
        loadWords()
    }

    fun getWords(amount: Int = 1, language: WordLanguage): List<String> {
        logger.info("Retrieving $amount words in language ${language.name}")
        launch {
            synchronized(this@PasswordService) {
                fetchedWords()
            }
        }
        return cachedWords[language.ordinal].shuffled().take(amount)
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
        if (counter.incrementAndGet() > 20) {
            counter.set(0)
            logger.info("Word cache refreshed!")
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
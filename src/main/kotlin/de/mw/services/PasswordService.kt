package de.mw.services

import de.mw.daos.PasswordDao
import de.mw.models.WordLanguage
import org.slf4j.LoggerFactory

class PasswordService(private val passwordDao: PasswordDao) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getWords(amount: Int = 1, language: WordLanguage): List<String> {
        logger.info("Retrieving $amount words in language ${language.name}")
        val words = passwordDao.get(amount, language)
        logger.debug("Retrieved ${words.size} words")
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
}
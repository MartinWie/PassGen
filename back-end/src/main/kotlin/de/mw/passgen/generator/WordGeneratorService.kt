package de.mw.passgen.generator

import de.mw.passgen.model.Languages
import de.mw.passgen.model.Word
import de.mw.passgen.repository.LanguagesRepository
import de.mw.passgen.repository.WordRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import javax.annotation.PostConstruct

@Service
class WordGeneratorService {

    companion object : KLogging()

    @Autowired
    lateinit var wordRepository: WordRepository

    @Autowired
    lateinit var languagesRepository: LanguagesRepository

    @PostConstruct
    fun setup() {

        val result = wordRepository.findByValue("Wort")

        if (result.isEmpty()) {
            logger.info("Starting initial setup")

            initialSetup()

            logger.info("Setup done!")
        } else {
            logger.info("DB already initialized nothing todo!")
        }
    }

    fun getRandomWord(lang: String): String {
        return wordRepository.findByLanguageAndWordNumberLanguageBase(lang, (0..100000).random()).first().value!!
    }

    private fun bufferedReaderFromFile(lang: String): BufferedReader? {
        val br: BufferedReader?

        br = try {
            File(lang).bufferedReader()
        } catch (e: IOException) {
            logger.error(e.toString())
            null
        }

        return br
    }

    private fun initialSetup() {
        var wordNumberLanguageBaseCounter: Int

        Languages.values().map { language ->
            val lineList = mutableListOf<Word>()
            wordNumberLanguageBaseCounter = 0

            bufferedReaderFromFile(language.path)?.useLines { lines ->
                lines.forEach { line ->
                    lineList.add(Word(language.value, line, wordNumberLanguageBaseCounter))
                    wordNumberLanguageBaseCounter++
                }
            }

            logger.info { "Starting reading from BufferedReader" }
            logger.info { "Words in list: ${lineList.size}" }

            wordRepository.saveAll(lineList)
            languagesRepository.save(Languages(language.value, wordNumberLanguageBaseCounter))
        }
    }

    enum class Languages(val value: String, val path: String) {
        GERMAN("german", "src/main/kotlin/de/mw/passgen/generator/resources/deutsch.txt"),
        ENGLISH("english", "src/main/kotlin/de/mw/passgen/generator/resources/english.txt")
    }
}

// disable h2 console!
// implement constants for languages + implement full random method ((0 .. 100000).random() includes 0 and 100000)
// figure where spring context fails to load

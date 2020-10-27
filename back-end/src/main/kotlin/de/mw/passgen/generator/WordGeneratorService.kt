package de.mw.passgen.generator

import de.mw.passgen.model.Word
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.IOException
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import javax.annotation.PostConstruct


@Service
class WordGeneratorService {

    companion object : KLogging()

    @Autowired
    lateinit var wordRepository: WordRepository

    val languages = mapOf<String,String>(
            "german" to "src/main/kotlin/de/mw/passgen/generator/resources/deutsch.txt",
            "english" to "src/main/kotlin/de/mw/passgen/generator/resources/english.txt"
    )

    val languagesWordAmounts = mutableMapOf<String,Int>(
            "german" to 0,
            "english" to 0
    )

    @PostConstruct
    fun setup(){

        val result = wordRepository.findByValue("Wort")

        if(result.isEmpty()){
            logger.info("Starting initial setup")

            initialSetup()

            logger.info("Setup done!")
        } else {
            logger.info("DB already initialized nothing todo!")
        }

    }

    fun getRandomWord(lang: String):String{
        return wordRepository.findByLanguageAndWordNumberLanguageBase(lang, (0 .. 100000).random()).first().value!!
    }

    private fun bufferedReaderFromFile(lang: String): BufferedReader? {
        val br: BufferedReader?

        br = try{
            File(lang).bufferedReader()
        }catch (e:IOException){
            logger.error(e.toString())
            null
        }

        return br
    }

    private fun initialSetup(){
        var wordNumberLanguageBaseCounter: Int

        languages.map { language ->
            val lineList = mutableListOf<Word>()
            wordNumberLanguageBaseCounter = 0

            bufferedReaderFromFile(language.value)?.useLines { lines ->
                lines.forEach { line ->
                    lineList.add(Word(language.key,line,wordNumberLanguageBaseCounter))
                    wordNumberLanguageBaseCounter++
                }
                languagesWordAmounts[language.key] = wordNumberLanguageBaseCounter
            }

            logger.info { "Starting reading from BufferedReader" }
            logger.info { "Words in list: ${lineList.size}" }

            wordRepository.saveAll(lineList)

        }

    }

}

 // disable h2 console!
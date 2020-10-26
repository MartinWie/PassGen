package de.mw.passgen.generator

import de.mw.passgen.model.Word
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.IOException
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import java.io.File


@Service
class WordGeneratorService {

    companion object : KLogging()

    @Autowired
    lateinit var wordRepository: WordRepository

    fun getRandomWord():String{
        return "random"
    }

    fun setup() : String{

        val result = wordRepository.findByValue("Wort")

        if(result.isEmpty()){
            logger.info("Starting initial setup")

            initialSetup()

            return "done"
        }

        return "nothing to do"

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
        val languages = mapOf<String,String>(
                "german" to "src/main/kotlin/de/mw/passgen/generator/resources/deutsch.txt",
                "english" to "src/main/kotlin/de/mw/passgen/generator/resources/english.txt"
        )

        languages.map { language ->
            val lineList = mutableListOf<Word>()

            bufferedReaderFromFile(language.value)?.useLines { lines ->
                lines.forEach { line ->
                    lineList.add(Word(language.key,line))
                }
            }

            logger.info { "Starting reading from BufferedReader" }
            logger.info { "Words in list: ${lineList.size}" }

            wordRepository.saveAll(lineList)

        }

    }

}

 // disable h2 console!
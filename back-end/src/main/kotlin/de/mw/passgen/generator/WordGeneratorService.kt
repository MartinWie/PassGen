package de.mw.passgen.generator

import de.mw.passgen.model.Word
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired


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
            logger.info("Initial setup")
            wordRepository.save(Word("german","Wort"))
            return "done"
        }

        return "nothing to do"

    }

    private fun loadLanguageFromFile(lang: wordlistLanguages): BufferedReader? {
        val br: BufferedReader?

        br = try{
            BufferedReader(FileReader(lang.toString()))
        }catch (e:IOException){
            logger.error(e.toString())
            null
        }

        return br
    }

    object wordlistLanguages {
        const val GERMAN = "..\\deutsch.txt"
        const val ENGLISH = "..\\english.txt"
    }

}

 // disable h2 console!
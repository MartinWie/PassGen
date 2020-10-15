package de.mw.passgen.generator

import mu.KLogging
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException

@Service
class WordGeneratorService {

    companion object : KLogging()

    fun getRandomWord():String{
        return "random"
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
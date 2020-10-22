package de.mw.passgen.generator

import de.mw.passgen.model.Word
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

@Controller
class WordGeneratorHandler(
        val wordGeneratorService: WordGeneratorService
) {

    @Autowired
    lateinit var wordRepository: WordRepository

    @RequestMapping("/word")
    fun getRandomWord():String{
        //return wordGeneratorService.getRandomWord()
        return "Word"
    }

    @RequestMapping("/setup")
    fun setup():String{
        wordRepository.save(Word("Wort1","german"))
        wordRepository.save(Word( "Wort2","german"))
        return "done"
    }
}
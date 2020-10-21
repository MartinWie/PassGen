package de.mw.passgen.generator

import de.mw.passgen.model.Word
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.util.*

@Controller
class WordGeneratorHandler(
        val wordGeneratorService: WordGeneratorService
) {

    @Autowired
    lateinit var wordRepository: WordRepository

    @GetMapping("/word")
    fun getRandomWord():String{
        return wordGeneratorService.getRandomWord()
    }

    @GetMapping("/setup")
    fun setup():String{
        wordRepository.save(Word(UUID.randomUUID() ,"Wort1","german"))
        wordRepository.save(Word( UUID.randomUUID(),"Wort2","german"))
        return "done"
    }
}
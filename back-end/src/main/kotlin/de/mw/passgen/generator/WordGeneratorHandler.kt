package de.mw.passgen.generator

import de.mw.passgen.model.Word
import de.mw.passgen.repository.WordRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WordGeneratorHandler(
        val wordGeneratorService: WordGeneratorService
) {

    @Autowired
    lateinit var wordRepository: WordRepository

    @GetMapping("/word")
    fun getRandomWord():String{
        //return wordGeneratorService.getRandomWord()
        return "Word"
    }

    @GetMapping("/setup")
    fun setup():String{
        wordRepository.save(Word("Wort1","german"))
        wordRepository.save(Word( "Wort2","german"))
        return "done"
    }
}
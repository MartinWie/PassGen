package de.mw.passgen.generator

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WordGeneratorHandler(
        val wordGeneratorService: WordGeneratorService
) {

    @GetMapping("/word")
    fun getRandomWord():String{
        return wordGeneratorService.getRandomWord()
    }
}
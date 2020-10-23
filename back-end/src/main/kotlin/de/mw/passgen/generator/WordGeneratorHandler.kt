package de.mw.passgen.generator

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WordGeneratorHandler(
        val wordGeneratorService: WordGeneratorService
) {

    @GetMapping("/word")
    fun getRandomWord():String{
        //return wordGeneratorService.getRandomWord()
        return "Word"
    }

    @GetMapping("/setup")
    fun setup():String{
        return wordGeneratorService.setup()
    }
}
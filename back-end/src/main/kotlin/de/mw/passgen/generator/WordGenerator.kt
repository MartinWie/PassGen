package de.mw.passgen.generator

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WordGenerator {

    @GetMapping("/word")
    fun getRandomWord():String{
        return "Word"
    }
}
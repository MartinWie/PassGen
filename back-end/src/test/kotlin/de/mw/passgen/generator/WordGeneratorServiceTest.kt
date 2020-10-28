package de.mw.passgen.generator

import de.mw.passgen.PassgenTestSpringContext
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class WordGeneratorServiceTest : PassgenTestSpringContext(){

    @Autowired
    lateinit var wordGeneratorService : WordGeneratorService

    @Test
    fun testWordgenertorReturnsString(){
        val randString = wordGeneratorService.getRandomWord("german")
        Assert.assertEquals(String().javaClass.kotlin.simpleName,randString.javaClass.kotlin.simpleName)
    }

    @Test
    fun checkIfWordsAreReallyRandom(){
        val w1 = wordGeneratorService.getRandomWord("german")
        val w2 = wordGeneratorService.getRandomWord("german")

        Assert.assertNotEquals(w1,w2)
    }
}
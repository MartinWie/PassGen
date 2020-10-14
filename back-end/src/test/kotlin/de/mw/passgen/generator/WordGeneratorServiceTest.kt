package de.mw.passgen.generator

import org.junit.Assert
import org.junit.Test

class WordGeneratorServiceTest {

    val wordGeneratorService : WordGeneratorService = WordGeneratorService()

    @Test
    fun testWordgenertorReturnsString(){
        val randString = wordGeneratorService.getRandomWord()
        Assert.assertEquals(String().javaClass.kotlin.simpleName,randString.javaClass.kotlin.simpleName)
    }
}
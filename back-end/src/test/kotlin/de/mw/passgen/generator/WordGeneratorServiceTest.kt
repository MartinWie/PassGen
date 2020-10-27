package de.mw.passgen.generator

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class WordGeneratorServiceTest {

    val wordGeneratorService : WordGeneratorService = WordGeneratorService()

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
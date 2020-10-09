package de.mw.passgen.generator

import org.junit.Assert
import org.junit.Test

class WordGeneratorTest {

    @Test
    fun testWordgenertorReturnsString(){
        val generator = WordGenerator()
        val ranString = generator.getRandomWord()
        Assert.assertEquals(String().javaClass.kotlin.simpleName,ranString.javaClass.kotlin.simpleName)
    }
}
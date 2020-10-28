package de.mw.passgen.generator

import de.mw.passgen.PassgenTestSpringContext
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired


class WordGeneratorServiceSpringTest : PassgenTestSpringContext() {

    @Autowired
    lateinit var wordGeneratorService : WordGeneratorService

    @Test
    fun testWordgenertorReturnsString(){
        val randString = wordGeneratorService.getRandomWord("german")
        Assert.assertEquals(String().javaClass.kotlin.simpleName,randString.javaClass.kotlin.simpleName)
    }
}
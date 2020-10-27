package de.mw.passgen.generator

import de.mw.passgen.PassgenApplication
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

//Testing in spring context
@Ignore
@SpringBootTest(classes = [PassgenApplication::class]) // Show Spring where spring configuration is
@RunWith(SpringRunner::class)
class WordGeneratorServiceSpringTest {

    @Autowired
    lateinit var wordGeneratorService : WordGeneratorService

    @Test
    fun testWordgenertorReturnsString(){
        val randString = wordGeneratorService.getRandomWord("german")
        Assert.assertEquals(String().javaClass.kotlin.simpleName,randString.javaClass.kotlin.simpleName)
    }
}
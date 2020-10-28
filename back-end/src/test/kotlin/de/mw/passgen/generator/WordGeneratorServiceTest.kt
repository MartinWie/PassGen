package de.mw.passgen.generator

import de.mw.passgen.PassgenApplication
import de.mw.passgen.repository.WordRepository
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
@SpringBootTest(classes = [PassgenApplication::class])
@RunWith(SpringRunner::class)
class WordGeneratorServiceTest {

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
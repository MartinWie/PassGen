package de.mw.passgen.repository

import de.mw.passgen.model.Word
import org.springframework.data.repository.CrudRepository

interface WordRepository: CrudRepository<Word,Long>{
    fun findByValue(value: String): List<Word>

    fun findByLanguage(language: String): List<Word>
}
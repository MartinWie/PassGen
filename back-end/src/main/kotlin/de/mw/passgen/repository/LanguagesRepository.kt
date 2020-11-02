package de.mw.passgen.repository

import de.mw.passgen.model.Languages
import org.springframework.data.repository.CrudRepository
import java.util.*

interface LanguagesRepository: CrudRepository<Languages,Long> {
    fun findByValue(value:String): List<Languages>
}
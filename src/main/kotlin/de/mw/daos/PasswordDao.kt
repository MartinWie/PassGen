package de.mw.daos

import de.mw.dsl
import de.mw.generated.Tables.PASSWORDWORD
import de.mw.models.WordLanguage
import org.jooq.DSLContext

class PasswordDao(dsl: DSLContext) {
    fun get(amount: Int = 1, language: WordLanguage): List<String> {
        val count = dsl.selectCount()
            .from(PASSWORDWORD)
            .where(PASSWORDWORD.LANGUAGE.eq(language.name))
            .fetchOneInto(Int::class.java) ?: 0

        if (count == 0) return emptyList()

        val offsets = (0 until amount).map { (Math.random() * count).toInt() }

        return offsets.mapNotNull { offset ->
            dsl.select(PASSWORDWORD.VALUE)
                .from(PASSWORDWORD)
                .where(PASSWORDWORD.LANGUAGE.eq(language.name))
                .limit(1)
                .offset(offset)
                .fetchOne(PASSWORDWORD.VALUE)
        }
    }
}
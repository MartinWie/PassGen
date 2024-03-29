package de.mw.passgen.model

import java.io.Serializable
import java.util.UUID
import javax.persistence.* // ktlint-disable no-wildcard-imports

@Entity
@Table(name = "Word")
class Word : Serializable {
    @Id
    val uuid: UUID = UUID.randomUUID()

    @Column(name = "language")
    var language: String? = null

    @Column(name = "value")
    var value: String? = null

    @Column(name = "wordNumberLanguageBase")
    var wordNumberLanguageBase: Int? = null

    override fun toString(): String {
        return "$uuid,$language,$value,$wordNumberLanguageBase"
    }

    constructor(language: String?, value: String?, wordNumberLanguageBase: Int?) {
        this.language = language
        this.value = value
        this.wordNumberLanguageBase = wordNumberLanguageBase
    }

    constructor() {}
}

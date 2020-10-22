package de.mw.passgen.model

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name="Word")
class Word: Serializable{
    @Id
    val uuid: UUID = UUID.randomUUID()

    @Column(name = "language")
    var language: String? = null

    @Column(name = "value")
    var value: String? = null

    override fun toString(): String {
        return "${uuid},${language},${value}"
    }

    constructor(language: String?, value: String?) {
        this.language = language
        this.value = value
    }

    constructor() {}

}
package de.mw.passgen.model

import java.io.Serializable
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "Languages")
class Languages: Serializable{
    @Id
    val id: UUID = UUID.randomUUID()

    @Column(name = "value")
    var language: String? = null

    @Column(name = "wordsInDB")
    var wordsInDB: Int? = null

    override fun toString(): String {
        return "${language},${wordsInDB}"
    }

    constructor(language:String, amount: Int){
        this.language = language
        this.wordsInDB = amount
    }

    constructor(){}
}
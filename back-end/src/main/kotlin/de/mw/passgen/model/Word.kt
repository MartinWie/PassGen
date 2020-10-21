package de.mw.passgen.model

import java.util.*
import javax.persistence.*

@Entity
@Table(name="Word")
class Word(
        @Id
        val uuid: UUID,

        @Column(name = "language")
        val language: String,

        @Column(name = "value")
        val value: String
){
    override fun toString(): String {
        return "${uuid},${language},${value}"
    }

} // hibernate uses reflection to initiate the bean (this means it uses the default/empty constructor and fills them with getters and setters) figure a suitable solution
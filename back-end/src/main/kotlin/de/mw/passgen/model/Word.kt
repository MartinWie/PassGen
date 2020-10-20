package de.mw.passgen.model

import javax.persistence.*

@Entity
@Table(name="Word")
@SequenceGenerator(name="wordSeq", initialValue=0, allocationSize=100)
class Word(
        @Id
        @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="wordSeq")
        val id: Long,

        @Column(name = "language")
        val language: String,

        @Column(name = "value")
        val value: String
){
    override fun toString(): String {
        return "${id},${language},${value}"
    }

}
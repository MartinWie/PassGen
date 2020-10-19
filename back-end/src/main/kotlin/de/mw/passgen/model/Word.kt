package de.mw.passgen.model

import javax.persistence.*

@Entity
@Table(name="Word")
public class Word(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
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
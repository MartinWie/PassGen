package de.mw.passgen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PassgenApplication

 // This is the "SpringConfiguration" class

fun main(args: Array<String>) {
	runApplication<PassgenApplication>(*args)
}

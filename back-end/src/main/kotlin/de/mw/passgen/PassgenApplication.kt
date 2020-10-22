package de.mw.passgen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("de.mw.passgen")
class PassgenApplication

 // This is the "SpringConfiguration" class

fun main(args: Array<String>) {
	runApplication<PassgenApplication>(*args)
}

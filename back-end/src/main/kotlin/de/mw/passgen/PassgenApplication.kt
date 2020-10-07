package de.mw.passgen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PassgenApplication

fun main(args: Array<String>) {
	runApplication<PassgenApplication>(*args)
}

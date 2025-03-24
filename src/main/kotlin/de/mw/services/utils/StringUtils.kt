package de.mw.services.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getCurrentTimeAsString(): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")
    return now.format(formatter)
}

fun generateEventCode(length: Int = 8): String {
    val allowedChars = "abcdefghijklmnopqrstuvwxyz123456789"

    return (1..length).map { allowedChars.random() }.joinToString("")
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

const val SPECIAL_CHARS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
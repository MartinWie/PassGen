package de.mw.frontend.utils

/**
 * Keeps a-z, A-Z, 0-9, -, space, ä, ö, ü, Ä, Ö, Ü, @, and ß.
 * Can be used to clean user input.
 */
fun String.sanitize(): String? {
    val allowedChars = "[^a-zA-Z0-9äöüÄÖÜß@\\- ]".toRegex()
    return this.replace(allowedChars, "").ifEmpty { null }
}

/**
 * Keeps a-z, A-Z, 0-9, -, ä, ö, ü, Ä, Ö, Ü, @, ., and ß.
 * Can be used to clean user input.
 */
fun String.sanitizeMail(): String? {
    val allowedChars = "[^a-zA-Z0-9äöüÄÖÜß@\\-.]".toRegex()
    return this.replace(allowedChars, "").ifEmpty { null }
}

/**
 * Keeps a-z, A-Z, 0-9, and -.
 * Can be used to clean user input.
 */
fun String.sanitizeStrict(): String? {
    val allowedChars = "[^a-zA-Z0-9\\-]".toRegex()
    return this.replace(allowedChars, "").ifEmpty { null }
}

/**
 * Escapes HTML special characters to prevent XSS when interpolating
 * into raw HTML fragments.
 */
fun String.escapeHtml(): String =
    this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")

package de.mw.plugins.routes

import java.util.*

fun getUUIDorNull(idString: String?): UUID? {
    val id: UUID
    try {
        id = UUID.fromString(idString)
    } catch (exception: Exception) {
        return null
    }

    return id
}
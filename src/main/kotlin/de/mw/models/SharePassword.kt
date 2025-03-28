package de.mw.models

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class SharePassword(
    val id: UUID,
    val created: LocalDateTime,
    val value: String,
    val remainingViews: BigDecimal
)

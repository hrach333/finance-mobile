package com.hrach.financeapp.ui.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val uiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun formatIsoDateForUi(date: String): String {
    return try {
        LocalDate.parse(date).format(uiDateFormatter)
    } catch (_: Exception) {
        date
    }
}

fun parseUiOrIsoDateToIso(date: String): String? {
    val normalized = date.trim()
    if (normalized.isEmpty()) return null

    return try {
        LocalDate.parse(normalized).toString()
    } catch (_: Exception) {
        try {
            LocalDate.parse(normalized, uiDateFormatter).toString()
        } catch (_: Exception) {
            null
        }
    }
}

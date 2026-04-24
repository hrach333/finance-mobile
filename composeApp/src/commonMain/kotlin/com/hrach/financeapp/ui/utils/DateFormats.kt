package com.hrach.financeapp.ui.utils

fun formatIsoDateForUi(date: String): String {
    val normalized = parseUiOrIsoDateToIso(date) ?: return date
    return "${normalized.substring(8, 10)}.${normalized.substring(5, 7)}.${normalized.substring(0, 4)}"
}

fun parseUiOrIsoDateToIso(date: String): String? {
    val normalized = date.trim()
    if (normalized.isEmpty()) return null

    if (normalized.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        return normalized
    }

    val dotOrDashDate = Regex("""(\d{2})[.-](\d{2})[.-](\d{4})""").matchEntire(normalized)
    if (dotOrDashDate != null) {
        val (day, month, year) = dotOrDashDate.destructured
        return "$year-$month-$day"
    }

    return null
}

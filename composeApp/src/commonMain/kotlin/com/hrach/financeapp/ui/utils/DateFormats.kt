package com.hrach.financeapp.ui.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class CalendarMonth(
    val year: Int,
    val month: Int
)

fun formatIsoDateForUi(date: String): String {
    val normalized = parseUiOrIsoDateToIso(date) ?: return date
    return "${normalized.substring(8, 10)}.${normalized.substring(5, 7)}.${normalized.substring(0, 4)}"
}

fun parseUiOrIsoDateToIso(date: String): String? {
    val normalized = date.trim()
    if (normalized.isEmpty()) return null

    val isoDate = Regex("""(\d{4})-(\d{2})-(\d{2})""").matchEntire(normalized)
    if (isoDate != null) {
        val (year, month, day) = isoDate.destructured
        return normalized.takeIf { isValidDate(year.toInt(), month.toInt(), day.toInt()) }
    }

    val dotOrDashDate = Regex("""(\d{2})[.-](\d{2})[.-](\d{4})""").matchEntire(normalized)
    if (dotOrDashDate != null) {
        val (day, month, year) = dotOrDashDate.destructured
        return "$year-$month-$day".takeIf { isValidDate(year.toInt(), month.toInt(), day.toInt()) }
    }

    return null
}

@OptIn(ExperimentalTime::class)
fun currentIsoDate(): String {
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
}

fun currentUiDate(): String {
    return formatIsoDateForUi(currentIsoDate())
}

fun calendarMonthFromDate(date: String): CalendarMonth {
    val normalized = parseUiOrIsoDateToIso(date) ?: currentIsoDate()
    return CalendarMonth(
        year = normalized.substring(0, 4).toInt(),
        month = normalized.substring(5, 7).toInt()
    )
}

fun previousMonth(month: CalendarMonth): CalendarMonth {
    return if (month.month == 1) {
        CalendarMonth(year = month.year - 1, month = 12)
    } else {
        month.copy(month = month.month - 1)
    }
}

fun nextMonth(month: CalendarMonth): CalendarMonth {
    return if (month.month == 12) {
        CalendarMonth(year = month.year + 1, month = 1)
    } else {
        month.copy(month = month.month + 1)
    }
}

fun monthTitle(month: CalendarMonth): String {
    val name = when (month.month) {
        1 -> "Январь"
        2 -> "Февраль"
        3 -> "Март"
        4 -> "Апрель"
        5 -> "Май"
        6 -> "Июнь"
        7 -> "Июль"
        8 -> "Август"
        9 -> "Сентябрь"
        10 -> "Октябрь"
        11 -> "Ноябрь"
        else -> "Декабрь"
    }
    return "$name ${month.year}"
}

fun daysInMonth(month: CalendarMonth): Int {
    return when (month.month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        else -> if (isLeapYear(month.year)) 29 else 28
    }
}

fun firstDayOffsetFromMonday(month: CalendarMonth): Int {
    return LocalDate(month.year, month.month, 1).dayOfWeek.isoDayNumber - 1
}

fun isoDateForDay(month: CalendarMonth, day: Int): String {
    return "${month.year}-${month.month.padded2()}-${day.padded2()}"
}

private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
    return runCatching { LocalDate(year, month, day) }.isSuccess
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

private fun Int.padded2(): String = toString().padStart(2, '0')

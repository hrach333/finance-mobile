package com.hrach.financeapp.data.repository

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CurrentMonthFinancePeriodProvider : FinancePeriodProvider {
    @OptIn(ExperimentalTime::class)
    override fun currentPeriod(): FinancePeriod {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val month = today.month.ordinal + 1
        val startDate = LocalDate(today.year, month, 1)
        val nextMonthStart = if (month == 12) {
            LocalDate(today.year + 1, 1, 1)
        } else {
            LocalDate(today.year, month + 1, 1)
        }
        val endDate = nextMonthStart.minus(DatePeriod(days = 1))
        return FinancePeriod(
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
    }
}

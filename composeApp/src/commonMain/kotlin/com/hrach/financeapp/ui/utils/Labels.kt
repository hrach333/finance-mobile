package com.hrach.financeapp.ui.utils

import kotlin.math.abs
import kotlin.math.roundToLong

fun Double.toMoneyText(currency: String = "RUB"): String {
    val suffix = if (currency.uppercase() == "RUB") "₽" else currency
    val normalized = if (this % 1.0 == 0.0) {
        this.toLong().toString()
    } else {
        val roundedCents = (abs(this) * 100).roundToLong()
        val whole = roundedCents / 100
        val cents = (roundedCents % 100).toString().padStart(2, '0')
        val sign = if (this < 0) "-" else ""
        "$sign$whole.$cents"
    }
    return "$normalized $suffix"
}

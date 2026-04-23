package com.hrach.financeapp.ui.utils

fun Double.toMoneyText(currency: String = "RUB"): String {
    val suffix = if (currency.uppercase() == "RUB") "₽" else currency
    val normalized = if (this % 1.0 == 0.0) this.toInt().toString() else String.format("%.2f", this)
    return "$normalized $suffix"
}

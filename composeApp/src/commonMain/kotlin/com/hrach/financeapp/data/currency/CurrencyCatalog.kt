package com.hrach.financeapp.data.currency

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val title: String
)

object CurrencyCatalog {
    const val DEFAULT_CODE = "RUB"

    val supported: List<CurrencyInfo> = listOf(
        CurrencyInfo(code = "RUB", symbol = "₽", title = "Российский рубль"),
        CurrencyInfo(code = "USD", symbol = "$", title = "Доллар США"),
        CurrencyInfo(code = "EUR", symbol = "€", title = "Евро")
    )

    fun normalize(code: String?): String {
        val normalized = code?.trim()?.uppercase().orEmpty()
        return normalized.ifBlank { DEFAULT_CODE }
    }

    fun symbolFor(code: String?): String {
        val normalized = normalize(code)
        return supported.firstOrNull { it.code == normalized }?.symbol ?: normalized
    }

    fun titleFor(code: String?): String {
        val normalized = normalize(code)
        return supported.firstOrNull { it.code == normalized }?.title ?: normalized
    }
}

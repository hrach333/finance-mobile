package com.hrach.financeapp.ui.utils

import androidx.compose.ui.graphics.Color

fun getIconBackgroundColor(key: String?): Color {
    return when {
        key.isNullOrEmpty() -> Color(0xFFE8A9FF)
        else -> {
            val colors = listOf(
                Color(0xFFFF6B9D),  // Ярко розовый
                Color(0xFFC44569),  // Бордовый
                Color(0xFFF15241),  // Оранжево-красный
                Color(0xFFFFA630),  // Оранжевый
                Color(0xFFFFD93D),  // Жёлтый
                Color(0xFF6BCB77),  // Зелёный
                Color(0xFF4D96FF),  // Синий
                Color(0xFF9D84B7),  // Фиолетовый
                Color(0xFFFF7EB3),  // Розовый
                Color(0xFF52B788),  // Мятный
                Color(0xFF00D9FF),  // Голубой
                Color(0xFFB565D8),  // Орхидея
            )
            colors[key.hashCode().mod(colors.size)]
        }
    }
}

fun getAccountTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "CASH" -> Color(0xFF52B788)        // Зелёный для наличных
        "CARD" -> Color(0xFF4D96FF)        // Синий для карты
        "BANK" -> Color(0xFF9D84B7)        // Фиолетовый для банка
        "SAVINGS" -> Color(0xFFFFD93D)     // Жёлтый для сбережений
        else -> Color(0xFFC44569)           // По умолчанию бордовый
    }
}

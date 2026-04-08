package com.hrach.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

fun String.toTransactionLabel(): String = when (uppercase()) {
    "INCOME" -> "Доход"
    "EXPENSE" -> "Расход"
    "TRANSFER" -> "Перевод"
    else -> this
}

fun String.toAccountTypeLabel(): String = when (uppercase()) {
    "CASH" -> "Наличные"
    "CARD" -> "Карта"
    "BANK" -> "Банковский счет"
    "SAVINGS" -> "Накопления"
    else -> this
}

fun String.toCategoryTypeLabel(): String = when (uppercase()) {
    "INCOME" -> "Доход"
    "EXPENSE" -> "Расход"
    else -> this
}

fun transactionGradient(type: String): Brush = when (type.uppercase()) {
    "INCOME" -> Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFF4ADE80)))
    "EXPENSE" -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFEC4899)))
    else -> Brush.horizontalGradient(listOf(Color(0xFF475569), Color(0xFF94A3B8)))
}

fun categoryIcon(name: String, type: String): ImageVector {
    val n = name.lowercase()
    return when {
        "ед" in n || "продукт" in n || "кафе" in n || "ресторан" in n -> Icons.Filled.Fastfood
        "транспорт" in n || "такси" in n || "автобус" in n -> Icons.Filled.DirectionsBus
        "дом" in n || "квартир" in n || "жкх" in n -> Icons.Filled.Home
        "здоров" in n || "мед" in n || "аптек" in n -> Icons.Filled.LocalHospital
        "покуп" in n || "одеж" in n || "маркет" in n -> Icons.Filled.ShoppingBag
        "зарплат" in n || "работ" in n -> Icons.Filled.Work
        "подар" in n -> Icons.Filled.Favorite
        "развлеч" in n || "игр" in n -> Icons.Filled.SportsEsports
        "накоп" in n || "сбереж" in n -> Icons.Filled.Savings
        "продаж" in n -> Icons.Filled.Sell
        type.uppercase() == "INCOME" -> Icons.Filled.AttachMoney
        else -> Icons.Filled.Payments
    }
}

fun String.toMoneySignPrefix(): String = when (uppercase()) {
    "INCOME" -> "+"
    "EXPENSE" -> "-"
    else -> ""
}

package com.hrach.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val title: String,
    val icon: ImageVector
)

val categoryIconOptions = listOf(
    CategoryIconOption("work", "Зарплата", Icons.Filled.Work),
    CategoryIconOption("food", "Еда", Icons.Filled.Fastfood),
    CategoryIconOption("transport", "Транспорт", Icons.Filled.DirectionsBus),
    CategoryIconOption("shopping", "Покупки", Icons.Filled.ShoppingBag),
    CategoryIconOption("gift", "Подарок", Icons.Filled.CardGiftcard),
    CategoryIconOption("home", "Дом", Icons.Filled.Home),
    CategoryIconOption("health", "Здоровье", Icons.Filled.LocalHospital),
    CategoryIconOption("kids", "Дети", Icons.Filled.ChildCare),
    CategoryIconOption("fun", "Развлечения", Icons.Filled.Movie),
    CategoryIconOption("savings", "Накопления", Icons.Filled.Savings),
    CategoryIconOption("money", "Деньги", Icons.Filled.Payments)
)

fun iconByKey(key: String?): CategoryIconOption =
    categoryIconOptions.firstOrNull { it.key == key } ?: categoryIconOptions.last()

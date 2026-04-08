package com.hrach.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(iconKey: String?): ImageVector {
    return when (iconKey) {
        "food" -> Icons.Filled.Fastfood
        "transport" -> Icons.Filled.DirectionsCar
        "home" -> Icons.Filled.Home
        "health" -> Icons.Filled.Favorite
        "shopping" -> Icons.Filled.ShoppingBag
        "salary" -> Icons.Filled.Payments
        "wallet" -> Icons.Filled.AccountBalanceWallet
        else -> Icons.Filled.MoreHoriz
    }
}
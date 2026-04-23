package com.hrach.financeapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

fun accountTypeIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "CASH" -> Icons.Filled.AccountBalanceWallet
        "CARD" -> Icons.Filled.CreditCard
        "BANK" -> Icons.Filled.AccountBalance
        "SAVINGS" -> Icons.Filled.Savings
        else -> Icons.Filled.AccountBalanceWallet
    }
}
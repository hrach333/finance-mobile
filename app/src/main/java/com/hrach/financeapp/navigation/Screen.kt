package com.hrach.financeapp.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "Главная")
    data object Transactions : Screen("transactions", "Операции")
    data object Accounts : Screen("accounts", "Счета")
    data object Categories : Screen("categories", "Категории")
    data object AddTransaction : Screen("add_transaction", "Добавить")
}

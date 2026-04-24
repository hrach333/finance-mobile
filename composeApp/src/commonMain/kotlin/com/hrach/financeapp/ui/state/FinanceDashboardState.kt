package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.model.FinanceOverview

enum class DashboardTab(val title: String, val glyph: String) {
    Home("Главная", "Г"),
    Transactions("Операции", "О"),
    Accounts("Счета", "С"),
    Categories("Категории", "К"),
    Members("Участники", "У"),
    Analytics("Аналитика", "А")
}

data class FinanceDashboardState(
    val selectedTab: DashboardTab = DashboardTab.Home,
    val overview: FinanceOverview? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface FinanceDashboardEvent {
    data object None : FinanceDashboardEvent
    data object AuthExpired : FinanceDashboardEvent
}

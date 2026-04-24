package com.hrach.financeapp.mvp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hrach.financeapp.data.auth.DesktopSessionStore
import com.hrach.financeapp.data.network.KtorAuthRepository
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SmartBudget MVP"
    ) {
        App(
            authRepository = KtorAuthRepository(),
            sessionStore = DesktopSessionStore(),
            repositoryFactory = { tokenProvider ->
                RemoteFinanceOverviewRepository(
                    dataSource = KtorFinanceDataSource(tokenProvider = tokenProvider),
                    periodProvider = CurrentMonthFinancePeriodProvider()
                )
            }
        )
    }
}

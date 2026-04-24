package com.hrach.financeapp.mvp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hrach.financeapp.data.auth.DesktopSessionStore
import com.hrach.financeapp.data.network.KtorAuthRepository
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository

fun main() = application {
    val closeHandlers = mutableListOf<() -> Unit>()
    val authRepository = KtorAuthRepository().also { repository ->
        closeHandlers += { repository.close() }
    }

    Window(
        onCloseRequest = {
            closeHandlers.asReversed().forEach { close ->
                runCatching { close() }
            }
            exitApplication()
        },
        title = "SmartBudget MVP"
    ) {
        App(
            authRepository = authRepository,
            sessionStore = DesktopSessionStore(),
            repositoryFactory = { tokenProvider ->
                val dataSource = KtorFinanceDataSource(tokenProvider = tokenProvider)
                closeHandlers += { dataSource.close() }
                RemoteFinanceOverviewRepository(
                    dataSource = dataSource,
                    periodProvider = CurrentMonthFinancePeriodProvider()
                )
            }
        )
    }
}

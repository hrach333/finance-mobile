package com.hrach.financeapp.mvp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SmartBudget MVP"
    ) {
        App(
            repository = RemoteFinanceOverviewRepository(
                dataSource = KtorFinanceDataSource(
                    tokenProvider = {
                        System.getenv("FINANCE_API_TOKEN")
                            ?: System.getProperty("finance.api.token")
                    }
                ),
                periodProvider = CurrentMonthFinancePeriodProvider()
            )
        )
    }
}

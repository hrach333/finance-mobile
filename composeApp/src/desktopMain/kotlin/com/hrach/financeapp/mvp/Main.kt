package com.hrach.financeapp.mvp

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.hrach.financeapp.data.auth.DesktopSessionStore
import com.hrach.financeapp.data.network.KtorAuthRepository
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository
import org.jetbrains.compose.resources.painterResource
import smartbudget.composeapp.generated.resources.Res
import smartbudget.composeapp.generated.resources.app_icon

fun main() = application {
    val closeHandlers = mutableListOf<() -> Unit>()
    val authRepository = KtorAuthRepository().also { repository ->
        closeHandlers += { repository.close() }
    }
    val windowState = rememberWindowState(size = DpSize(width = 1280.dp, height = 860.dp))

    Window(
        onCloseRequest = {
            closeHandlers.asReversed().forEach { close ->
                runCatching { close() }
            }
            exitApplication()
        },
        state = windowState,
        title = "Умный бюджет",
        icon = painterResource(Res.drawable.app_icon)
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

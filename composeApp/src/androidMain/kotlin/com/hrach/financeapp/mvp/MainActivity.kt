package com.hrach.financeapp.mvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Context
import com.hrach.financeapp.data.auth.AndroidSessionStore
import com.hrach.financeapp.data.network.KtorAuthRepository
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext

        setContent {
            App(
                authRepository = KtorAuthRepository(),
                sessionStore = AndroidSessionStore(this),
                repositoryFactory = { tokenProvider ->
                    RemoteFinanceOverviewRepository(
                        dataSource = KtorFinanceDataSource(tokenProvider = tokenProvider),
                        periodProvider = CurrentMonthFinancePeriodProvider()
                    )
                }
            )
        }
    }

    companion object {
        private lateinit var appContext: Context

        fun applicationContextProvider(): Context = appContext
    }
}

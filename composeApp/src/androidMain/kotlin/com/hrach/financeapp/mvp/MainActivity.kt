package com.hrach.financeapp.mvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hrach.financeapp.BuildConfig
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(
                repository = RemoteFinanceOverviewRepository(
                    dataSource = KtorFinanceDataSource(
                        tokenProvider = { BuildConfig.FINANCE_API_TOKEN.takeIf { it.isNotBlank() } }
                    ),
                    periodProvider = CurrentMonthFinancePeriodProvider()
                )
            )
        }
    }
}

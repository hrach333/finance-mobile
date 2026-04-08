package com.hrach.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hrach.financeapp.data.api.ApiClient
import com.hrach.financeapp.data.repository.FinanceRepository
import com.hrach.financeapp.ui.screens.AppRoot
import com.hrach.financeapp.ui.theme.FinanceAppTheme
import com.hrach.financeapp.viewmodel.HomeViewModel
import com.hrach.financeapp.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        val repository = FinanceRepository(ApiClient.financeApi)
        val sessionViewModel = SessionViewModel(repository, ApiClient.sessionManager)
        val homeViewModel = HomeViewModel(repository)

        setContent {
            FinanceAppTheme {
                AppRoot(
                    sessionViewModel = sessionViewModel,
                    homeViewModel = homeViewModel
                )
            }
        }
    }
}

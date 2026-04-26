package com.hrach.financeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.Gson
import com.hrach.financeapp.data.api.ApiClient
import com.hrach.financeapp.data.db.FinanceDatabase
import com.hrach.financeapp.data.network.NetworkMonitor
import com.hrach.financeapp.data.offline.OfflineManager
import com.hrach.financeapp.data.repository.FinanceRepository
import com.hrach.financeapp.ui.screens.AppRoot
import com.hrach.financeapp.ui.theme.FinanceAppTheme
import com.hrach.financeapp.viewmodel.HomeViewModel
import com.hrach.financeapp.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        
        // Инициализируем компоненты для офлайн функциональности
        val database = FinanceDatabase.getInstance(applicationContext)
        val pendingOperationDao = database.pendingOperationDao()
        val localFinanceDao = database.localFinanceDao()
        val networkMonitor = NetworkMonitor(applicationContext)
        val gson = Gson()
        
        // Создаем OfflineManager
        val offlineManager = OfflineManager(
            dao = pendingOperationDao,
            api = ApiClient.financeApi,
            networkMonitor = networkMonitor,
            gson = gson
        )
        
        // Передаем offlineManager и networkMonitor в Repository
        val repository = FinanceRepository(
            api = ApiClient.financeApi,
            offlineManager = offlineManager,
            networkMonitor = networkMonitor,
            gson = gson,
            localFinanceDao = localFinanceDao
        )
        
        val sessionViewModel = SessionViewModel(repository, ApiClient.sessionManager)
        val homeViewModel = HomeViewModel(
            repository = repository,
            offlineManager = offlineManager,
            networkMonitor = networkMonitor
        )

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

package com.hrach.financeapp

import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hrach.financeapp.data.auth.AndroidSessionStore
import com.hrach.financeapp.data.network.KtorAuthRepository
import com.hrach.financeapp.data.network.KtorFinanceDataSource
import com.hrach.financeapp.data.repository.CurrentMonthFinancePeriodProvider
import com.hrach.financeapp.data.repository.RemoteFinanceOverviewRepository
import com.hrach.financeapp.mvp.App
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class MainActivity : ComponentActivity() {
    private lateinit var yandexAuthLauncher: ActivityResultLauncher<YandexAuthLoginOptions>
    private var pendingYandexAuth: CompletableDeferred<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        val yandexAuthSdk = YandexAuthSdk.create(YandexAuthOptions(this))
        yandexAuthLauncher = registerForActivityResult(yandexAuthSdk.contract) { result ->
            val pending = pendingYandexAuth ?: return@registerForActivityResult
            pendingYandexAuth = null
            when (result) {
                is YandexAuthResult.Success -> pending.complete(result.token.value)
                is YandexAuthResult.Failure -> pending.completeExceptionally(
                    IllegalStateException(result.exception.message ?: "Ошибка авторизации Яндекс")
                )
                YandexAuthResult.Cancelled -> pending.completeExceptionally(
                    IllegalStateException("Вход через Яндекс отменен")
                )
            }
        }

        setContent {
            App(
                authRepository = KtorAuthRepository(),
                sessionStore = AndroidSessionStore(this),
                yandexOAuthTokenProvider = {
                    requestYandexOAuthToken().await()
                },
                repositoryFactory = { tokenProvider ->
                    RemoteFinanceOverviewRepository(
                        dataSource = KtorFinanceDataSource(tokenProvider = tokenProvider),
                        periodProvider = CurrentMonthFinancePeriodProvider()
                    )
                }
            )
        }
    }

    private fun requestYandexOAuthToken(): Deferred<String> {
        pendingYandexAuth?.takeIf { it.isActive }?.let {
            throw IllegalStateException("Авторизация через Яндекс уже запущена")
        }
        return CompletableDeferred<String>().also { deferred ->
            pendingYandexAuth = deferred
            yandexAuthLauncher.launch(YandexAuthLoginOptions())
        }
    }

    companion object {
        private lateinit var appContext: Context

        fun applicationContextProvider(): Context = appContext
    }
}

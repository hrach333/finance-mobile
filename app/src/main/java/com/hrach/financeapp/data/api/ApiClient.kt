package com.hrach.financeapp.data.api

import android.content.Context
import com.hrach.financeapp.data.auth.AuthInterceptor
import com.hrach.financeapp.data.auth.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://finance.hrach.ru/api/"

    lateinit var sessionManager: SessionManager
        private set

    fun init(context: Context) {
        if (::sessionManager.isInitialized) {
            return
        }
        sessionManager = SessionManager(context.applicationContext)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val financeApi: FinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinanceApi::class.java)
    }
}

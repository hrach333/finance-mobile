package com.hrach.financeapp.data.api

import android.content.Context
import com.hrach.financeapp.data.auth.AuthInterceptor
import com.hrach.financeapp.data.auth.SessionManager
import com.hrach.financeapp.data.network.AIService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://finance.hrach.ru/api/"
    private const val AI_BASE_URL = "https://aifinance.hrach.ru/"

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
    private val aiHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .writeTimeout(10, TimeUnit.MINUTES)
        .callTimeout(10, TimeUnit.MINUTES)
        .build()

    val financeApi: FinanceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FinanceApi::class.java)
    }

    val aiService: AIService by lazy {
        Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(aiHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIService::class.java)
    }
}

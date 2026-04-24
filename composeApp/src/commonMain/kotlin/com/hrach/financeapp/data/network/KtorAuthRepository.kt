package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.AuthResponseDto
import com.hrach.financeapp.data.dto.LoginRequest
import com.hrach.financeapp.data.dto.RegisterRequest
import com.hrach.financeapp.data.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class KtorAuthRepository(
    baseUrl: String = KtorFinanceDataSource.DEFAULT_BASE_URL,
    private val httpClient: HttpClient = createFinanceHttpClient(
        tokenProvider = { null },
        baseUrl = baseUrl
    )
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthResponseDto =
        httpClient.post("login") {
            setBody(LoginRequest(email = email, password = password))
        }.body()

    override suspend fun register(name: String, email: String, password: String): AuthResponseDto =
        httpClient.post("register") {
            setBody(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    passwordConfirmation = password
                )
            )
        }.body()

    override suspend fun logout(token: String?) {
        if (token.isNullOrBlank()) return
        runCatching {
            httpClient.post("logout") {
                bearerAuth(token)
            }
        }
    }
}

package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.AuthResponseDto
import com.hrach.financeapp.data.dto.ForgotPasswordRequest
import com.hrach.financeapp.data.dto.ForgotPasswordResponse
import com.hrach.financeapp.data.dto.LoginRequest
import com.hrach.financeapp.data.dto.MessageResponse
import com.hrach.financeapp.data.dto.RegisterRequest
import com.hrach.financeapp.data.dto.ResetPasswordRequest
import com.hrach.financeapp.data.dto.YandexMobileLoginRequest
import com.hrach.financeapp.data.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject

class KtorAuthRepository(
    baseUrl: String = KtorFinanceDataSource.DEFAULT_BASE_URL,
    private val httpClient: HttpClient = createFinanceHttpClient(
        tokenProvider = { null },
        baseUrl = baseUrl
    )
) : AuthRepository {
    fun close() {
        httpClient.close()
    }

    override suspend fun login(email: String, password: String): AuthResponseDto =
        httpClient.post("login") {
            setBody(LoginRequest(email = email, password = password))
        }.body()

    override suspend fun loginWithYandex(oauthToken: String): AuthResponseDto =
        httpClient.post("auth/yandex/mobile") {
            setBody(YandexMobileLoginRequest(oauthToken = oauthToken))
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

    override suspend fun forgotPassword(email: String): ForgotPasswordResponse =
        httpClient.post("forgot-password") {
            setBody(ForgotPasswordRequest(email = email.trim()))
        }.body()

    override suspend fun resetPassword(email: String, code: String, password: String): MessageResponse {
        val payload = httpClient.post("reset-password") {
            setBody(
                ResetPasswordRequest(
                    email = email.trim(),
                    code = code.trim(),
                    password = password,
                    passwordConfirmation = password
                )
            )
        }.bodyAsText()
        return decodeResetPasswordResponse(payload)
    }

    override suspend fun logout(token: String?) {
        if (token.isNullOrBlank()) return
        runCatching {
            httpClient.post("logout") {
                bearerAuth(token)
            }
        }
    }
}

private fun decodeResetPasswordResponse(payload: String): MessageResponse {
    if (payload.isBlank()) {
        throw FinanceNetworkException("Сервер не подтвердил смену пароля: пустой ответ")
    }

    val root = runCatching { financeJson.parseToJsonElement(payload).jsonObject }
        .getOrElse { throw FinanceNetworkException("Не удалось прочитать ответ сервера") }
    val message = root.textValue("message") ?: root.textValue("error")
    val errors = root["errors"]?.flattenText()
    val token = root.textValue("token")
    val statusText = root.textValue("status")?.lowercase()
    val success = root.booleanValue("success")
        ?: root.booleanValue("status")
        ?: statusText?.let { status ->
            when (status) {
                "success", "ok", "true" -> true
                "error", "failed", "fail", "false" -> false
                else -> null
            }
        }

    if (success == false) {
        throw FinanceNetworkException(listOfNotNull(message, errors).joinToString("\n").ifBlank { "Пароль не был изменен" })
    }

    if (success == true) {
        return MessageResponse(message = message ?: "Пароль успешно изменен")
    }

    if (!token.isNullOrBlank()) {
        return MessageResponse(message = message ?: "Пароль успешно изменен")
    }

    if (message != null && message.looksLikePasswordResetSuccess()) {
        return MessageResponse(message = message)
    }

    throw FinanceNetworkException(
        listOfNotNull(message, errors).joinToString("\n").ifBlank { "Сервер не подтвердил смену пароля" }
    )
}

private fun JsonObject.textValue(key: String): String? {
    return (this[key] as? JsonPrimitive)?.content?.takeIf { it.isNotBlank() }
}

private fun JsonObject.booleanValue(key: String): Boolean? {
    return (this[key] as? JsonPrimitive)?.booleanOrNull
}

private fun JsonElement.flattenText(): String? {
    return when (this) {
        is JsonPrimitive -> content.takeIf { it.isNotBlank() }
        is JsonArray -> mapNotNull { it.flattenText() }.joinToString("\n").takeIf { it.isNotBlank() }
        is JsonObject -> values.mapNotNull { it.flattenText() }.joinToString("\n").takeIf { it.isNotBlank() }
    }
}

private fun String.looksLikePasswordResetSuccess(): Boolean {
    val normalized = lowercase()
    return listOf("успеш", "измен", "success", "reset", "changed", "updated").any { it in normalized }
}

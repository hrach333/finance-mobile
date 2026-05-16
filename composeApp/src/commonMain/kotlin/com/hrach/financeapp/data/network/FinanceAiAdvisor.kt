package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.AIChatRequest
import com.hrach.financeapp.data.dto.ChatMessage
import com.hrach.financeapp.data.dto.AIChatResponse
import com.hrach.financeapp.data.model.FinanceOverview
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString

class FinanceAiAdvisor(
    private val model: String = DEFAULT_MODEL,
    baseUrl: String = DEFAULT_BASE_URL,
    private val httpClient: HttpClient = createOllamaHttpClient(baseUrl)
) {
    suspend fun getAdvice(overview: FinanceOverview): String {
        val prompt = AIPromptBuilder.buildFinanceAdvicePrompt(overview)
        val rawResponse = httpClient.post("v1/chat/completions") {
            setBody(
                AIChatRequest(
                    model = model,
                    messages = listOf(ChatMessage(role = "user", content = prompt)),
                    stream = false,
                    temperature = 0.7,
                    max_tokens = 1000
                )
            )
        }.bodyAsText()
        println("FinanceAiAdvisor raw response: $rawResponse")

        val response = runCatching {
            financeJson.decodeFromString<AIChatResponse>(rawResponse)
        }.getOrElse { error ->
            throw FinanceNetworkException(
                "Не удалось разобрать ответ ИИ: ${error.message}\nОтвет сервера: ${rawResponse.take(MAX_DEBUG_RESPONSE_LENGTH)}",
                error
            )
        }

        return response.contentOrNull()?.takeIf { it.isNotBlank() }
            ?: throw FinanceNetworkException(
                "ИИ не вернул текст ответа.\nОтвет сервера: ${response.debugSummary().take(MAX_DEBUG_RESPONSE_LENGTH)}"
            )
    }

    fun close() {
        httpClient.close()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://aifinance.hrach.ru/"
        const val DEFAULT_MODEL = "llama3.1"
        private const val MAX_DEBUG_RESPONSE_LENGTH = 2_000
    }
}

fun createOllamaHttpClient(baseUrl: String = FinanceAiAdvisor.DEFAULT_BASE_URL): HttpClient {
    val normalizedBaseUrl = baseUrl.trimEnd('/') + "/"
    return HttpClient {
        expectSuccess = true

        defaultRequest {
            url(normalizedBaseUrl)
            contentType(ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(financeJson)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 60_000
            requestTimeoutMillis = 600_000
            socketTimeoutMillis = 600_000
        }
    }
}

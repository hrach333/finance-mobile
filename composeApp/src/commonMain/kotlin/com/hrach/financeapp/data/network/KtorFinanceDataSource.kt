package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.ApiListResponse
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UserDto
import com.hrach.financeapp.data.repository.FinanceDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorFinanceDataSource(
    private val tokenProvider: () -> String?,
    baseUrl: String = DEFAULT_BASE_URL,
    private val httpClient: HttpClient = createFinanceHttpClient(
        tokenProvider = tokenProvider,
        baseUrl = baseUrl
    )
) : FinanceDataSource {
    override suspend fun me(): UserDto = httpClient.get("me").body()

    override suspend fun getGroups(): List<GroupDto> =
        httpClient.get("groups").body<ApiListResponse<GroupDto>>().data

    override suspend fun getAccounts(groupId: Int): List<AccountDto> =
        httpClient.get("accounts") {
            parameter("groupId", groupId)
        }.body<ApiListResponse<AccountDto>>().data

    override suspend fun getCategories(groupId: Int): List<CategoryDto> =
        httpClient.get("categories") {
            parameter("groupId", groupId)
        }.body<ApiListResponse<CategoryDto>>().data

    override suspend fun getTransactions(groupId: Int): List<TransactionDto> =
        httpClient.get("transactions") {
            parameter("groupId", groupId)
        }.body<ApiListResponse<TransactionDto>>().data

    override suspend fun getSummary(groupId: Int, startDate: String, endDate: String): SummaryDto =
        httpClient.get("analytics/summary") {
            parameter("groupId", groupId)
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }.body()

    override suspend fun getGroupMembers(groupId: Int): List<GroupMemberDto> =
        httpClient.get("groups/$groupId/members").body<ApiListResponse<GroupMemberDto>>().data

    companion object {
        const val DEFAULT_BASE_URL = "https://finance.hrach.ru/api/"
    }
}

fun createFinanceHttpClient(
    tokenProvider: () -> String?,
    baseUrl: String = KtorFinanceDataSource.DEFAULT_BASE_URL
): HttpClient {
    val normalizedBaseUrl = baseUrl.trimEnd('/') + "/"

    return HttpClient {
        expectSuccess = true

        defaultRequest {
            url(normalizedBaseUrl)
            contentType(ContentType.Application.Json)
            tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
                bearerAuth(token)
            }
            headers.remove(HttpHeaders.Accept)
            headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(Logging) {
            level = LogLevel.INFO
        }

        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, _ ->
                throw FinanceNetworkException(cause.message ?: "Ошибка запроса к API", cause)
            }
        }
    }
}

class FinanceNetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)

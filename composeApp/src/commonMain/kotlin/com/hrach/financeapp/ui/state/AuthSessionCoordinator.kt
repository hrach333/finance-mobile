package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.auth.SessionStore
import com.hrach.financeapp.data.dto.AuthResponseDto
import com.hrach.financeapp.data.repository.AuthRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository

class AuthSessionCoordinator(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    private val repositoryFactory: (() -> String?) -> FinanceOverviewRepository
) {
    suspend fun restoreToken(): String? {
        return sessionStore.getToken()?.takeIf { it.isNotBlank() }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return authenticate {
            authRepository.login(email = email.trim(), password = password)
        }
    }

    suspend fun loginWithYandex(oauthToken: String): AuthResult {
        return authenticate {
            authRepository.loginWithYandex(oauthToken = oauthToken)
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        return authenticate {
            authRepository.register(name = name.trim(), email = email.trim(), password = password)
        }
    }

    suspend fun forgotPassword(email: String): AuthActionResult {
        return runCatching { authRepository.forgotPassword(email = email.trim()) }
            .fold(
                onSuccess = { response ->
                    AuthActionResult.Success(response.message.ifBlank { "Код отправлен на email" })
                },
                onFailure = { throwable ->
                    AuthActionResult.Failure(throwable.message ?: "Не удалось отправить код")
                }
            )
    }

    suspend fun resetPassword(email: String, code: String, password: String): AuthActionResult {
        return runCatching {
            val normalizedEmail = email.trim()
            val response = authRepository.resetPassword(email = normalizedEmail, code = code.trim(), password = password)
            runCatching { authRepository.login(email = normalizedEmail, password = password) }
                .getOrElse { throwable ->
                    throw IllegalStateException(
                        "Сервер принял запрос восстановления, но вход новым паролем не прошел: ${throwable.message ?: "причина не указана"}"
                    )
                }
            response
        }.fold(
            onSuccess = { response ->
                AuthActionResult.Success(response.message.ifBlank { "Пароль успешно изменен" })
            },
            onFailure = { throwable ->
                AuthActionResult.Failure(throwable.message ?: "Не удалось изменить пароль")
            }
        )
    }

    fun createOverviewRepository(tokenProvider: () -> String?): FinanceOverviewRepository {
        return repositoryFactory(tokenProvider)
    }

    suspend fun logout(token: String?) {
        authRepository.logout(token)
        sessionStore.clearToken()
    }

    suspend fun clearToken() {
        sessionStore.clearToken()
    }

    private suspend fun authenticate(block: suspend () -> AuthResponseDto): AuthResult {
        return runCatching { block() }
            .fold(
                onSuccess = { response ->
                    sessionStore.saveToken(response.token)
                    AuthResult.Success(response.token)
                },
                onFailure = { throwable ->
                    AuthResult.Failure(throwable.message ?: "Не удалось выполнить вход")
                }
            )
    }
}

sealed interface AuthResult {
    data class Success(val token: String) : AuthResult
    data class Failure(val message: String) : AuthResult
}

sealed interface AuthActionResult {
    data class Success(val message: String) : AuthActionResult
    data class Failure(val message: String) : AuthActionResult
}

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

    suspend fun register(name: String, email: String, password: String): AuthResult {
        return authenticate {
            authRepository.register(name = name.trim(), email = email.trim(), password = password)
        }
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

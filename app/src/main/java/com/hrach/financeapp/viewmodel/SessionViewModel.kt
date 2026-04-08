package com.hrach.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hrach.financeapp.data.auth.SessionManager
import com.hrach.financeapp.data.dto.ApiErrorResponse
import com.hrach.financeapp.data.dto.UserDto
import com.hrach.financeapp.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface AuthState {
    data object Checking : AuthState
    data object Unauthenticated : AuthState
    data object Authenticated : AuthState
}

class SessionViewModel(
    private val repository: FinanceRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val gson = Gson()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            _loading.value = true
            _error.value = null
            try {
                _currentUser.value = repository.me()
                _authState.value = AuthState.Authenticated
            } catch (_: Exception) {
                sessionManager.clearToken()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
            } finally {
                _loading.value = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = repository.login(email, password)
                sessionManager.saveToken(response.token)
                _currentUser.value = response.user
                _authState.value = AuthState.Authenticated
                onSuccess()
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = repository.register(name, email, password)
                sessionManager.saveToken(response.token)
                _currentUser.value = response.user
                _authState.value = AuthState.Authenticated
                onSuccess()
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.logout()
            } catch (_: Exception) {
            } finally {
                sessionManager.clearToken()
                _currentUser.value = null
                _authState.value = AuthState.Unauthenticated
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun parseException(e: Exception): String {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string()
            val parsed = runCatching { gson.fromJson(body, ApiErrorResponse::class.java) }.getOrNull()
            if (!parsed?.errors.isNullOrEmpty()) {
                return parsed?.errors?.values?.flatten()?.joinToString("\n") ?: "Ошибка запроса"
            }
            return when (e.code()) {
                401 -> "Неверный email или пароль"
                403 -> "Нет доступа"
                422 -> parsed?.message ?: "Ошибка валидации"
                else -> parsed?.message ?: "Ошибка запроса: ${e.code()}"
            }
        }
        return e.message ?: "Неизвестная ошибка"
    }
}

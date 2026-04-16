package com.hrach.financeapp.viewmodel

import android.util.Log
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

            val cachedUser = sessionManager.getUser()
            if (cachedUser != null) {
                _currentUser.value = cachedUser
                _authState.value = AuthState.Authenticated
                return@launch
            }

            _loading.value = true
            _error.value = null
            try {
                val user = repository.me()
                sessionManager.saveUser(user)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 401) {
                    sessionManager.clearSession()
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                } else {
                    _authState.value = AuthState.Unauthenticated
                    _error.value = parseException(e)
                }
            } finally {
                _loading.value = false
            }
        }
    }

    private fun refreshCurrentUserSilently() {
        viewModelScope.launch {
            try {
                val user = repository.me()
                sessionManager.saveUser(user)
                _currentUser.value = user
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 401) {
                    sessionManager.clearSession()
                    _currentUser.value = null
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun refreshCurrentUser() {
        refreshCurrentUserSilently()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = repository.login(email, password)
                sessionManager.saveSession(response.token, response.user)
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
                sessionManager.saveSession(response.token, response.user)
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

    fun forgotPassword(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.forgotPassword(email)
                onSuccess()
            } catch (e: Exception) {
                _error.value = parseException(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetPassword(email: String, code: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repository.resetPassword(email, code, password)
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
                sessionManager.clearSession()
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
            Log.e("SessionViewModel", "HTTP Error ${e.code()}: $body")
            
            val errorText = parseError(body)
            if (errorText.isNotBlank()) {
                Log.d("SessionViewModel", "Errors from backend:\n$errorText")
                return errorText
            }
            
            return when (e.code()) {
                401 -> "Неверный email или пароль"
                403 -> "Нет доступа"
                422 -> "Ошибка валидации: проверьте введенные данные"
                else -> "Ошибка запроса: ${e.code()}"
            }
        }
        return e.message ?: "Неизвестная ошибка"
    }
    
    private fun parseError(json: String?): String {
        return try {
            val parsed = gson.fromJson(json, ApiErrorResponse::class.java)
            Log.d("SessionViewModel", "Parsed response: errors=${parsed.errors}, message=${parsed.message}")
            
            parsed.errors
                ?.let { errors ->
                    when (errors) {
                        is Map<*, *> -> {
                            @Suppress("UNCHECKED_CAST")
                            val errorsMap = errors as? Map<String, List<String>> ?: return ""
                            errorsMap.entries
                                .map { (field, messages) ->
                                    val translatedMessages = messages
                                        .map { mapError(it) }
                                    val fieldName = when (field) {
                                        "name" -> "Имя"
                                        "email" -> "Email"
                                        "password" -> "Пароль"
                                        "password_confirmation" -> "Подтверждение пароля"
                                        else -> field.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    }
                                    "$fieldName: ${translatedMessages.joinToString(", ")}"
                                }
                                .joinToString("\n")
                        }
                        is List<*> -> {
                            errors.filterIsInstance<String>()
                                .map { mapError(it) }
                                .joinToString("\n")
                        }
                        else -> ""
                    }
                }
                ?: parsed.message
                ?: "Ошибка запроса"
        } catch (e: Exception) {
            Log.e("SessionViewModel", "Failed to parse error: ${e.message}, json=$json")
            "Ошибка запроса"
        }
    }
    
    private fun mapError(message: String): String {
        return when {
            message.contains("email has already been taken", ignoreCase = true) ->
                "Пользователь с таким email уже существует"

            message.contains("password must be at least", ignoreCase = true) ->
                "Пароль должен быть не менее 8 символов"

            message.contains("password confirmation does not match", ignoreCase = true) ->
                "Пароли не совпадают"

            message.contains("email must be a valid email", ignoreCase = true) ->
                "Некорректный email"
            
            message.contains("validation.unique", ignoreCase = true) ->
                "Это значение уже занято"
            
            message.contains("validation.required", ignoreCase = true) ->
                "Это поле обязательно"
            
            message.contains("validation.min.string", ignoreCase = true) ->
                "Значение слишком короткое"
            
            message.contains("validation.min.numeric", ignoreCase = true) ->
                "Значение должно быть больше"
            
            message.contains("validation.confirmed", ignoreCase = true) ->
                "Поле подтверждения не совпадает"
            
            message.contains("validation.email", ignoreCase = true) ->
                "Email должен быть корректным"

            else -> message
        }
    }
}

package com.hrach.financeapp.data.auth

import android.content.Context
import com.google.gson.Gson
import com.hrach.financeapp.data.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("finance_session", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _token = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    val token: StateFlow<String?> = _token.asStateFlow()

    fun getToken(): String? = _token.value

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).commit()
        _token.value = token
    }

    fun saveUser(user: UserDto) {
        prefs.edit().putString(KEY_USER, gson.toJson(user)).commit()
    }

    fun getUser(): UserDto? {
        val raw = prefs.getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(raw, UserDto::class.java) }.getOrNull()
    }

    fun saveSession(token: String, user: UserDto) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, gson.toJson(user))
            .commit()
        _token.value = token
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).commit()
        _token.value = null
    }

    fun clearSession() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).commit()
        _token.value = null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "auth_user"
    }
}

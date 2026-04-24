package com.hrach.financeapp.data.auth

import android.content.Context

class AndroidSessionStore(context: Context) : SessionStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        "smartbudget_kmp_session",
        Context.MODE_PRIVATE
    )

    override suspend fun getToken(): String? =
        preferences.getString(KEY_TOKEN, null)?.takeIf { it.isNotBlank() }

    override suspend fun saveToken(token: String) {
        preferences.edit().putString(KEY_TOKEN, token).apply()
    }

    override suspend fun clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply()
    }

    private companion object {
        const val KEY_TOKEN = "auth_token"
    }
}

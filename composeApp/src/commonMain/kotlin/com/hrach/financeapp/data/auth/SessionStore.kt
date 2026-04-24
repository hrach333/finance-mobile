package com.hrach.financeapp.data.auth

interface SessionStore {
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}

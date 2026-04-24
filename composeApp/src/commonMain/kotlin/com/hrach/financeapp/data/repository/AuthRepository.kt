package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.AuthResponseDto

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(name: String, email: String, password: String): AuthResponseDto
    suspend fun logout(token: String?)
}

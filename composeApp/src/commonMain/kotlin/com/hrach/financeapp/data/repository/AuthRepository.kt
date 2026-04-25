package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.AuthResponseDto
import com.hrach.financeapp.data.dto.ForgotPasswordResponse
import com.hrach.financeapp.data.dto.MessageResponse

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResponseDto
    suspend fun register(name: String, email: String, password: String): AuthResponseDto
    suspend fun forgotPassword(email: String): ForgotPasswordResponse
    suspend fun resetPassword(email: String, code: String, password: String): MessageResponse
    suspend fun logout(token: String?)
}

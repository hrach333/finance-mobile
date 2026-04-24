package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val password: String,
    val passwordConfirmation: String
)

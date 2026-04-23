package com.hrach.financeapp.data.dto

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val password: String,
    val passwordConfirmation: String
)


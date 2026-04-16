package com.hrach.financeapp.data.dto

data class ForgotPasswordResponse(
    val message: String,
    val email: String,
    val code: String
)

package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordResponse(
    val message: String = "",
    val email: String? = null,
    val code: String? = null
)

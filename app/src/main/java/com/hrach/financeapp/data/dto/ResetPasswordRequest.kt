package com.hrach.financeapp.data.dto

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)

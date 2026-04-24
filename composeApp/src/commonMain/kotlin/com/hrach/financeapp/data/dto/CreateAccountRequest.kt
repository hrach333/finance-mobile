package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountRequest(
    val groupId: Int,
    val userId: Int?,
    val name: String,
    val type: String,
    val currency: String,
    val initialBalance: Double,
    val shared: Boolean
)

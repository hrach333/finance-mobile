package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: Int,
    val groupId: Int,
    val userId: Int?,
    val name: String,
    val type: String,
    val currency: String,
    val initialBalance: Double,
    val currentBalance: Double,
    val shared: Boolean,
    val isActive: Boolean
)

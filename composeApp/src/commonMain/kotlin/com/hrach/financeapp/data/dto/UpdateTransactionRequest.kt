package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTransactionRequest(
    val groupId: Int,
    val accountId: Int,
    val createdBy: Int?,
    val type: String,
    val amount: Double,
    val currency: String,
    val categoryId: Int?,
    val transactionDate: String,
    val comment: String?
)

package com.hrach.financeapp.data.dto

data class TransactionDto(
    val id: Int,
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

package com.hrach.financeapp.data.repository

interface TransactionMutationsRepository {
    suspend fun createTransaction(
        groupId: Int,
        accountId: Int,
        createdBy: Int?,
        type: String,
        amount: Double,
        currency: String,
        categoryId: Int?,
        transactionDate: String,
        comment: String?
    )

    suspend fun updateTransaction(
        transactionId: Int,
        groupId: Int,
        accountId: Int,
        createdBy: Int?,
        type: String,
        amount: Double,
        currency: String,
        categoryId: Int?,
        transactionDate: String,
        comment: String?
    )

    suspend fun deleteTransaction(transactionId: Int)
}

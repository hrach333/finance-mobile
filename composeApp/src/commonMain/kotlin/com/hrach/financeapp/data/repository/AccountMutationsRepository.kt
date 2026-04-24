package com.hrach.financeapp.data.repository

interface AccountMutationsRepository {
    suspend fun createAccount(
        groupId: Int,
        name: String,
        type: String,
        initialBalance: Double,
        shared: Boolean = true
    )

    suspend fun updateAccount(
        accountId: Int,
        groupId: Int,
        userId: Int?,
        name: String,
        type: String,
        currency: String,
        initialBalance: Double,
        shared: Boolean = true,
        isActive: Boolean = true
    )

    suspend fun deleteAccount(accountId: Int)
}

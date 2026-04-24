package com.hrach.financeapp.data.repository

interface GroupMutationsRepository {
    suspend fun createGroup(name: String, baseCurrency: String)
    suspend fun updateGroup(groupId: Int, name: String, baseCurrency: String)
    suspend fun selectGroup(groupId: Int)
}

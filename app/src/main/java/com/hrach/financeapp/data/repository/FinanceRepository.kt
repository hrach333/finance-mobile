package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.api.FinanceApi
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest

class FinanceRepository(private val api: FinanceApi) {
    suspend fun getGroups() = api.getGroups().data
    suspend fun getAccounts(groupId: Int) = api.getAccounts(groupId).data
    suspend fun createAccount(request: CreateAccountRequest) = api.createAccount(request)
    suspend fun updateAccount(id: Int, request: UpdateAccountRequest) = api.updateAccount(id, request)
    suspend fun deleteAccount(id: Int) = api.deleteAccount(id)
    suspend fun getCategories(groupId: Int) = api.getCategories(groupId).data
    suspend fun createCategory(request: CreateCategoryRequest) = api.createCategory(request)
    suspend fun updateCategory(id: Int, name: String, type: String, iconKey: String? = null) =
        api.updateCategory(id, UpdateCategoryRequest(name = name, type = type, iconKey = iconKey))
    suspend fun deleteCategory(id: Int) = api.deleteCategory(id)
    suspend fun getTransactions(groupId: Int) = api.getTransactions(groupId).data
    suspend fun getSummary(groupId: Int, startDate: String, endDate: String) = api.getSummary(groupId, startDate, endDate)
    suspend fun createTransaction(request: CreateTransactionRequest) = api.createTransaction(request)
    suspend fun updateTransaction(id: Int, request: UpdateTransactionRequest) = api.updateTransaction(id, request)
    suspend fun deleteTransaction(id: Int) = api.deleteTransaction(id)
}

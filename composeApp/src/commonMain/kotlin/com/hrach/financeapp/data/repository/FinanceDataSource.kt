package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UserDto

interface FinanceDataSource {
    suspend fun me(): UserDto
    suspend fun getGroups(): List<GroupDto>
    suspend fun getAccounts(groupId: Int): List<AccountDto>
    suspend fun getCategories(groupId: Int): List<CategoryDto>
    suspend fun getTransactions(groupId: Int): List<TransactionDto>
    suspend fun getSummary(groupId: Int, startDate: String, endDate: String): SummaryDto
    suspend fun getGroupMembers(groupId: Int): List<GroupMemberDto>
}

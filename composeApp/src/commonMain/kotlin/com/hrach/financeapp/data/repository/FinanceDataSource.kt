package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.AddGroupMemberRequest
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateGroupRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateGroupRequest
import com.hrach.financeapp.data.dto.UpdateGroupMemberRoleRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.dto.UserDto

interface FinanceDataSource {
    suspend fun me(): UserDto
    suspend fun getGroups(): List<GroupDto>
    suspend fun createGroup(request: CreateGroupRequest): GroupDto?
    suspend fun updateGroup(groupId: Int, request: UpdateGroupRequest): GroupDto?
    suspend fun getAccounts(groupId: Int): List<AccountDto>
    suspend fun createAccount(request: CreateAccountRequest)
    suspend fun updateAccount(id: Int, request: UpdateAccountRequest)
    suspend fun deleteAccount(id: Int)
    suspend fun getCategories(groupId: Int): List<CategoryDto>
    suspend fun createCategory(request: CreateCategoryRequest)
    suspend fun updateCategory(id: Int, request: UpdateCategoryRequest)
    suspend fun deleteCategory(id: Int)
    suspend fun getTransactions(groupId: Int): List<TransactionDto>
    suspend fun createTransaction(request: CreateTransactionRequest)
    suspend fun updateTransaction(id: Int, request: UpdateTransactionRequest)
    suspend fun deleteTransaction(id: Int)
    suspend fun getSummary(groupId: Int, startDate: String, endDate: String): SummaryDto
    suspend fun getGroupMembers(groupId: Int): List<GroupMemberDto>
    suspend fun addGroupMember(groupId: Int, request: AddGroupMemberRequest)
    suspend fun updateGroupMemberRole(groupId: Int, memberId: Int, request: UpdateGroupMemberRoleRequest)
    suspend fun deleteGroupMember(groupId: Int, memberId: Int)
}

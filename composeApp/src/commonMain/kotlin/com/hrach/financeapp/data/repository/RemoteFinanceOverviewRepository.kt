package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.AddGroupMemberRequest
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateGroupMemberRoleRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.toFinanceOverview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RemoteFinanceOverviewRepository(
    private val dataSource: FinanceDataSource,
    private val periodProvider: FinancePeriodProvider,
    private val preferredGroupId: Int? = null
) : FinanceOverviewRepository,
    AccountMutationsRepository,
    CategoryMutationsRepository,
    GroupMemberMutationsRepository,
    TransactionMutationsRepository {
    override suspend fun getOverview(): FinanceOverview {
        return coroutineScope {
            val userDeferred = async { dataSource.me() }
            val groupsDeferred = async { dataSource.getGroups() }
            val user = userDeferred.await()
            val groups = groupsDeferred.await()
            val activeGroup = groups.firstOrNull { it.id == preferredGroupId } ?: groups.firstOrNull()
            val groupId = activeGroup?.id

            if (groupId == null) {
                toFinanceOverview(
                    userEmail = user.email,
                    groups = groups,
                    activeGroupId = null,
                    accounts = emptyList(),
                    categories = emptyList(),
                    transactions = emptyList(),
                    summary = null,
                    members = emptyList()
                )
            } else {
                val period = periodProvider.currentPeriod()
                val accountsDeferred = async { dataSource.getAccounts(groupId) }
                val categoriesDeferred = async { dataSource.getCategories(groupId) }
                val transactionsDeferred = async { dataSource.getTransactions(groupId) }
                val summaryDeferred = async { dataSource.getSummary(groupId, period.startDate, period.endDate) }
                val membersDeferred = async { dataSource.getGroupMembers(groupId) }

                toFinanceOverview(
                    userEmail = user.email,
                    groups = groups,
                    activeGroupId = groupId,
                    accounts = accountsDeferred.await(),
                    categories = categoriesDeferred.await(),
                    transactions = transactionsDeferred.await(),
                    summary = summaryDeferred.await(),
                    members = membersDeferred.await()
                )
            }
        }
    }

    override suspend fun createAccount(
        groupId: Int,
        name: String,
        type: String,
        initialBalance: Double,
        shared: Boolean
    ) {
        dataSource.createAccount(
            CreateAccountRequest(
                groupId = groupId,
                userId = null,
                name = name.trim(),
                type = type,
                currency = "RUB",
                initialBalance = initialBalance,
                shared = shared
            )
        )
    }

    override suspend fun updateAccount(
        accountId: Int,
        groupId: Int,
        userId: Int?,
        name: String,
        type: String,
        currency: String,
        initialBalance: Double,
        shared: Boolean,
        isActive: Boolean
    ) {
        dataSource.updateAccount(
            accountId,
            UpdateAccountRequest(
                groupId = groupId,
                userId = userId,
                name = name.trim(),
                type = type,
                currency = currency,
                initialBalance = initialBalance,
                currentBalance = initialBalance,
                shared = shared,
                isActive = isActive
            )
        )
    }

    override suspend fun deleteAccount(accountId: Int) {
        dataSource.deleteAccount(accountId)
    }

    override suspend fun createCategory(
        groupId: Int,
        name: String,
        type: String,
        iconKey: String?
    ) {
        dataSource.createCategory(
            CreateCategoryRequest(
                groupId = groupId,
                type = type,
                name = name.trim(),
                iconKey = iconKey
            )
        )
    }

    override suspend fun updateCategory(
        categoryId: Int,
        name: String,
        type: String,
        iconKey: String?
    ) {
        dataSource.updateCategory(
            categoryId,
            UpdateCategoryRequest(
                name = name.trim(),
                type = type,
                iconKey = iconKey
            )
        )
    }

    override suspend fun deleteCategory(categoryId: Int) {
        dataSource.deleteCategory(categoryId)
    }

    override suspend fun addGroupMember(groupId: Int, email: String, role: String) {
        dataSource.addGroupMember(
            groupId = groupId,
            request = AddGroupMemberRequest(email = email.trim(), role = role)
        )
    }

    override suspend fun updateGroupMemberRole(groupId: Int, memberId: Int, role: String) {
        dataSource.updateGroupMemberRole(
            groupId = groupId,
            memberId = memberId,
            request = UpdateGroupMemberRoleRequest(role = role)
        )
    }

    override suspend fun deleteGroupMember(groupId: Int, memberId: Int) {
        dataSource.deleteGroupMember(groupId = groupId, memberId = memberId)
    }

    override suspend fun createTransaction(
        groupId: Int,
        accountId: Int,
        createdBy: Int?,
        type: String,
        amount: Double,
        currency: String,
        categoryId: Int?,
        transactionDate: String,
        comment: String?
    ) {
        dataSource.createTransaction(
            CreateTransactionRequest(
                groupId = groupId,
                accountId = accountId,
                createdBy = createdBy,
                type = type,
                amount = amount,
                currency = currency,
                categoryId = categoryId,
                transactionDate = transactionDate,
                comment = comment
            )
        )
    }

    override suspend fun updateTransaction(
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
    ) {
        dataSource.updateTransaction(
            transactionId,
            UpdateTransactionRequest(
                groupId = groupId,
                accountId = accountId,
                createdBy = createdBy,
                type = type,
                amount = amount,
                currency = currency,
                categoryId = categoryId,
                transactionDate = transactionDate,
                comment = comment
            )
        )
    }

    override suspend fun deleteTransaction(transactionId: Int) {
        dataSource.deleteTransaction(transactionId)
    }
}

data class FinancePeriod(
    val startDate: String,
    val endDate: String
)

fun interface FinancePeriodProvider {
    fun currentPeriod(): FinancePeriod
}

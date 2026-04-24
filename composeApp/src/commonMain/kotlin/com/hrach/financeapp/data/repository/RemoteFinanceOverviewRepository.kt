package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.CreateAccountRequest
import com.hrach.financeapp.data.dto.CreateCategoryRequest
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.UpdateAccountRequest
import com.hrach.financeapp.data.dto.UpdateCategoryRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.toFinanceOverview

class RemoteFinanceOverviewRepository(
    private val dataSource: FinanceDataSource,
    private val periodProvider: FinancePeriodProvider,
    private val preferredGroupId: Int? = null
) : FinanceOverviewRepository, AccountMutationsRepository, CategoryMutationsRepository, TransactionMutationsRepository {
    override suspend fun getOverview(): FinanceOverview {
        val user = dataSource.me()
        val groups = dataSource.getGroups()
        val activeGroup = groups.firstOrNull { it.id == preferredGroupId } ?: groups.firstOrNull()
        val groupId = activeGroup?.id

        if (groupId == null) {
            return toFinanceOverview(
                userEmail = user.email,
                groups = groups,
                activeGroupId = null,
                accounts = emptyList(),
                categories = emptyList(),
                transactions = emptyList(),
                summary = null,
                members = emptyList()
            )
        }

        val period = periodProvider.currentPeriod()
        return toFinanceOverview(
            userEmail = user.email,
            groups = groups,
            activeGroupId = groupId,
            accounts = dataSource.getAccounts(groupId),
            categories = dataSource.getCategories(groupId),
            transactions = dataSource.getTransactions(groupId),
            summary = dataSource.getSummary(groupId, period.startDate, period.endDate),
            members = dataSource.getGroupMembers(groupId)
        )
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

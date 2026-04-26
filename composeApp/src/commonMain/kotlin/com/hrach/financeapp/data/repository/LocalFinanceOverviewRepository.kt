package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.toFinanceOverview
import com.hrach.financeapp.data.offline.OfflineAccountEntity
import com.hrach.financeapp.data.offline.OfflineCategoryEntity
import com.hrach.financeapp.data.offline.OfflineFinanceDatabase
import com.hrach.financeapp.data.offline.OfflineTransactionEntity
import com.hrach.financeapp.data.offline.PlatformOfflineStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalFinanceOverviewRepository(
    private val store: PlatformOfflineStore = PlatformOfflineStore()
) : FinanceOverviewRepository,
    AccountMutationsRepository,
    CategoryMutationsRepository,
    GroupMutationsRepository,
    GroupMemberMutationsRepository,
    TransactionMutationsRepository {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override suspend fun getOverview(): FinanceOverview {
        val database = load()
        val groupId = OFFLINE_GROUP_ID
        val transactions = database.transactions.filter { it.groupId == groupId }
        val summary = SummaryDto(
            income = transactions.filter { it.type.equals("INCOME", ignoreCase = true) }.sumOf { it.amount },
            expense = transactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }.sumOf { it.amount },
            balance = database.accounts.filter { it.groupId == groupId }.sumOf { it.currentBalance }
        )
        return toFinanceOverview(
            userEmail = "без регистрации",
            groups = database.groups.map { GroupDto(it.id, it.name, it.baseCurrency) },
            activeGroupId = groupId,
            accounts = database.accounts.map { it.toDto() },
            categories = database.categories.map { it.toDto() },
            transactions = transactions.map { it.toDto() },
            summary = summary,
            members = emptyList(),
            isOfflineMode = true
        )
    }

    override suspend fun createAccount(groupId: Int, name: String, type: String, initialBalance: Double, currency: String, shared: Boolean) {
        update { database ->
            val account = OfflineAccountEntity(
                id = database.nextAccountId,
                name = name.trim(),
                type = type,
                currency = currency,
                initialBalance = initialBalance,
                currentBalance = initialBalance,
                shared = shared
            )
            database.copy(accounts = database.accounts + account, nextAccountId = database.nextAccountId + 1)
        }
    }

    override suspend fun updateAccount(accountId: Int, groupId: Int, userId: Int?, name: String, type: String, currency: String, initialBalance: Double, shared: Boolean, isActive: Boolean) {
        update { database ->
            database.copy(
                accounts = database.accounts.map {
                    if (it.id == accountId) {
                        it.copy(name = name.trim(), type = type, currency = currency, initialBalance = initialBalance, currentBalance = initialBalance, shared = shared, isActive = isActive)
                    } else {
                        it
                    }
                }
            )
        }
    }

    override suspend fun deleteAccount(accountId: Int) {
        update { database ->
            database.copy(
                accounts = database.accounts.filterNot { it.id == accountId },
                transactions = database.transactions.filterNot { it.accountId == accountId }
            )
        }
    }

    override suspend fun createCategory(groupId: Int, name: String, type: String, iconKey: String?) {
        update { database ->
            val category = OfflineCategoryEntity(
                id = database.nextCategoryId,
                type = type,
                name = name.trim(),
                iconKey = iconKey,
                isSystem = false
            )
            database.copy(categories = database.categories + category, nextCategoryId = database.nextCategoryId + 1)
        }
    }

    override suspend fun updateCategory(categoryId: Int, name: String, type: String, iconKey: String?) {
        update { database ->
            val category = database.categories.firstOrNull { it.id == categoryId }
            require(category?.isSystem != true) { "Готовые категории нельзя редактировать. Создай свою категорию рядом." }
            database.copy(
                categories = database.categories.map {
                    if (it.id == categoryId) it.copy(name = name.trim(), type = type, iconKey = iconKey) else it
                }
            )
        }
    }

    override suspend fun deleteCategory(categoryId: Int) {
        update { database ->
            val category = database.categories.firstOrNull { it.id == categoryId }
            require(category?.isSystem != true) { "Готовые категории нельзя удалить, чтобы операции всегда было куда отнести." }
            database.copy(categories = database.categories.filterNot { it.id == categoryId })
        }
    }

    override suspend fun createGroup(name: String, baseCurrency: String) {
        error("В офлайн режиме уже есть группа «Мой бюджет». Другие группы доступны после регистрации.")
    }

    override suspend fun updateGroup(groupId: Int, name: String, baseCurrency: String) {
        error("В офлайн режиме группа закреплена как «Мой бюджет».")
    }

    override suspend fun selectGroup(groupId: Int) = Unit

    override suspend fun addGroupMember(groupId: Int, email: String, role: String) {
        error(OFFLINE_MEMBERS_MESSAGE)
    }

    override suspend fun updateGroupMemberRole(groupId: Int, memberId: Int, role: String) {
        error(OFFLINE_MEMBERS_MESSAGE)
    }

    override suspend fun deleteGroupMember(groupId: Int, memberId: Int) {
        error(OFFLINE_MEMBERS_MESSAGE)
    }

    override suspend fun createTransaction(groupId: Int, accountId: Int, createdBy: Int?, type: String, amount: Double, currency: String, categoryId: Int?, transactionDate: String, comment: String?) {
        update { database ->
            val transaction = OfflineTransactionEntity(
                id = database.nextTransactionId,
                accountId = accountId,
                type = type,
                amount = amount,
                currency = currency,
                categoryId = categoryId,
                transactionDate = transactionDate,
                comment = comment
            )
            database.copy(
                accounts = applyBalance(database.accounts, accountId, type, amount),
                transactions = database.transactions + transaction,
                nextTransactionId = database.nextTransactionId + 1
            )
        }
    }

    override suspend fun updateTransaction(transactionId: Int, groupId: Int, accountId: Int, createdBy: Int?, type: String, amount: Double, currency: String, categoryId: Int?, transactionDate: String, comment: String?) {
        update { database ->
            val old = database.transactions.firstOrNull { it.id == transactionId } ?: return@update database
            val reverted = applyBalance(database.accounts, old.accountId, old.type, -old.amount)
            val applied = applyBalance(reverted, accountId, type, amount)
            database.copy(
                accounts = applied,
                transactions = database.transactions.map {
                    if (it.id == transactionId) {
                        it.copy(accountId = accountId, type = type, amount = amount, currency = currency, categoryId = categoryId, transactionDate = transactionDate, comment = comment)
                    } else {
                        it
                    }
                }
            )
        }
    }

    override suspend fun deleteTransaction(transactionId: Int) {
        update { database ->
            val old = database.transactions.firstOrNull { it.id == transactionId } ?: return@update database
            database.copy(
                accounts = applyBalance(database.accounts, old.accountId, old.type, -old.amount),
                transactions = database.transactions.filterNot { it.id == transactionId }
            )
        }
    }

    private fun load(): OfflineFinanceDatabase {
        val raw = store.read()
        return if (raw.isNullOrBlank()) {
            OfflineFinanceDatabase().also { save(it) }
        } else {
            runCatching { json.decodeFromString<OfflineFinanceDatabase>(raw) }
                .getOrElse { OfflineFinanceDatabase().also { database -> save(database) } }
        }
    }

    private fun update(transform: (OfflineFinanceDatabase) -> OfflineFinanceDatabase) {
        save(transform(load()))
    }

    private fun save(database: OfflineFinanceDatabase) {
        store.write(json.encodeToString(database))
    }

    private fun applyBalance(accounts: List<OfflineAccountEntity>, accountId: Int, type: String, amount: Double): List<OfflineAccountEntity> {
        val signedAmount = if (type.equals("INCOME", ignoreCase = true)) amount else -amount
        return accounts.map { account ->
            if (account.id == accountId) account.copy(currentBalance = account.currentBalance + signedAmount) else account
        }
    }

    private fun OfflineAccountEntity.toDto() = AccountDto(id, groupId, userId, name, type, currency, initialBalance, currentBalance, shared, isActive)
    private fun OfflineCategoryEntity.toDto() = CategoryDto(id, groupId, type, name, iconKey, isSystem)
    private fun OfflineTransactionEntity.toDto() = TransactionDto(id, groupId, accountId, createdBy, type, amount, currency, categoryId, transactionDate, comment)

    private companion object {
        const val OFFLINE_GROUP_ID = 1
        const val OFFLINE_MEMBERS_MESSAGE =
            "Совместный бюджет, участники и защита от потери данных доступны после регистрации."
    }
}

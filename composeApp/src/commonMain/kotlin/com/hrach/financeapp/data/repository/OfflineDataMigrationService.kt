package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.CategoryOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.offline.OfflineFinanceDatabase

class OfflineDataMigrationService(
    private val offlineRepository: LocalFinanceOverviewRepository,
    private val onlineRepository: FinanceOverviewRepository
) {
    suspend fun migrateToGroup(targetGroupId: Int, targetCurrency: String) {
        val accountMutations = onlineRepository as? AccountMutationsRepository
            ?: error("Перенос счетов недоступен")
        val categoryMutations = onlineRepository as? CategoryMutationsRepository
            ?: error("Перенос категорий недоступен")
        val transactionMutations = onlineRepository as? TransactionMutationsRepository
            ?: error("Перенос операций недоступен")
        val offline = offlineRepository.migrationSnapshot()

        var online = onlineRepository.getOverview().forGroup(targetGroupId)

        offline.categories.forEach { offlineCategory ->
            if (online.categories.none { it.sameCategory(offlineCategory.name, offlineCategory.type) }) {
                categoryMutations.createCategory(
                    groupId = targetGroupId,
                    name = offlineCategory.name,
                    type = offlineCategory.type,
                    iconKey = offlineCategory.iconKey
                )
            }
        }
        online = onlineRepository.getOverview().forGroup(targetGroupId)
        val categoryMap = offline.categories.associate { offlineCategory ->
            offlineCategory.id to online.categories.firstOrNull {
                it.sameCategory(offlineCategory.name, offlineCategory.type)
            }?.id
        }

        offline.accounts.forEach { offlineAccount ->
            if (online.accounts.none { it.sameAccount(offlineAccount.name, offlineAccount.type) }) {
                accountMutations.createAccount(
                    groupId = targetGroupId,
                    name = offlineAccount.name,
                    type = offlineAccount.type,
                    initialBalance = offlineAccount.initialBalance,
                    currency = targetCurrency,
                    shared = offlineAccount.shared
                )
            }
        }
        online = onlineRepository.getOverview().forGroup(targetGroupId)
        val accountMap = offline.accounts.associate { offlineAccount ->
            offlineAccount.id to online.accounts.firstOrNull {
                it.sameAccount(offlineAccount.name, offlineAccount.type)
            }?.id
        }

        offline.transactions.sortedBy { it.transactionDate }.forEach { transaction ->
            val accountId = accountMap[transaction.accountId] ?: return@forEach
            transactionMutations.createTransaction(
                groupId = targetGroupId,
                accountId = accountId,
                createdBy = null,
                type = transaction.type,
                amount = transaction.amount,
                currency = targetCurrency,
                categoryId = transaction.categoryId?.let { categoryMap[it] },
                transactionDate = transaction.transactionDate,
                comment = transaction.comment
            )
        }

        offlineRepository.clearOfflineData()
    }

    private suspend fun FinanceOverview.forGroup(groupId: Int): FinanceOverview {
        val groupMutations = onlineRepository as? GroupMutationsRepository
        if (activeGroupId != groupId) {
            groupMutations?.selectGroup(groupId)
            return onlineRepository.getOverview()
        }
        return this
    }

    private fun CategoryOverview.sameCategory(name: String, type: String): Boolean {
        return this.name.equals(name, ignoreCase = true) && this.type.equals(type, ignoreCase = true)
    }

    private fun AccountOverview.sameAccount(name: String, type: String): Boolean {
        return title.equals(name, ignoreCase = true) && this.type.equals(type, ignoreCase = true)
    }
}

fun OfflineFinanceDatabase.offlineBaseCurrency(): String {
    return groups.firstOrNull()?.baseCurrency ?: "RUB"
}

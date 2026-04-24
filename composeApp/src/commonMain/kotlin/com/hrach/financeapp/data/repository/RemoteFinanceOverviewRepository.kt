package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.toFinanceOverview

class RemoteFinanceOverviewRepository(
    private val dataSource: FinanceDataSource,
    private val periodProvider: FinancePeriodProvider,
    private val preferredGroupId: Int? = null
) : FinanceOverviewRepository {
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
}

data class FinancePeriod(
    val startDate: String,
    val endDate: String
)

fun interface FinancePeriodProvider {
    fun currentPeriod(): FinancePeriod
}

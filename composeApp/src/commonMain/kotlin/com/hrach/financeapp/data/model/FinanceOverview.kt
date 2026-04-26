package com.hrach.financeapp.data.model

import com.hrach.financeapp.data.currency.CurrencyCatalog

data class FinanceOverview(
    val userEmail: String,
    val isOfflineMode: Boolean = false,
    val activeGroupId: Int? = null,
    val activeGroupName: String,
    val groups: List<GroupOverview> = emptyList(),
    val summary: FinanceSummary,
    val accounts: List<AccountOverview>,
    val categories: List<CategoryOverview> = emptyList(),
    val members: List<GroupMemberOverview> = emptyList(),
    val transactions: List<TransactionOverview>,
    val insights: List<String>
)

data class FinanceSummary(
    val balanceLabel: String,
    val incomeLabel: String,
    val expenseLabel: String,
    val subtitle: String
)

data class GroupOverview(
    val id: Int,
    val name: String,
    val baseCurrency: String
)

data class AccountOverview(
    val id: Int? = null,
    val groupId: Int? = null,
    val userId: Int? = null,
    val title: String,
    val type: String = "CASH",
    val currency: String = CurrencyCatalog.DEFAULT_CODE,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val shared: Boolean = true,
    val isActive: Boolean = true,
    val balanceLabel: String,
    val subtitle: String,
    val colorToken: OverviewColorToken
)

data class TransactionOverview(
    val id: Int? = null,
    val groupId: Int? = null,
    val accountId: Int? = null,
    val createdBy: Int? = null,
    val categoryId: Int? = null,
    val category: String,
    val categoryIconKey: String? = null,
    val comment: String,
    val amount: Double = 0.0,
    val currency: String = CurrencyCatalog.DEFAULT_CODE,
    val transactionDate: String = "",
    val amountLabel: String,
    val dateLabel: String,
    val kind: TransactionKind,
    val colorToken: OverviewColorToken
)

enum class TransactionKind {
    Income,
    Expense
}

data class CategoryOverview(
    val id: Int,
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String? = null,
    val isSystem: Boolean = false
)

data class GroupMemberOverview(
    val id: Int,
    val userId: Int? = null,
    val role: String,
    val userName: String?,
    val userEmail: String?
)

enum class OverviewColorToken {
    Income,
    Expense,
    Primary,
    Secondary,
    Muted
}

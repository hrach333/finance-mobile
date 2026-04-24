package com.hrach.financeapp.data.model

data class FinanceOverview(
    val userEmail: String,
    val activeGroupId: Int? = null,
    val activeGroupName: String,
    val summary: FinanceSummary,
    val accounts: List<AccountOverview>,
    val transactions: List<TransactionOverview>,
    val insights: List<String>
)

data class FinanceSummary(
    val balanceLabel: String,
    val incomeLabel: String,
    val expenseLabel: String,
    val subtitle: String
)

data class AccountOverview(
    val id: Int? = null,
    val groupId: Int? = null,
    val userId: Int? = null,
    val title: String,
    val type: String = "CASH",
    val currency: String = "RUB",
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val shared: Boolean = true,
    val isActive: Boolean = true,
    val balanceLabel: String,
    val subtitle: String,
    val colorToken: OverviewColorToken
)

data class TransactionOverview(
    val category: String,
    val comment: String,
    val amountLabel: String,
    val dateLabel: String,
    val kind: TransactionKind,
    val colorToken: OverviewColorToken
)

enum class TransactionKind {
    Income,
    Expense
}

enum class OverviewColorToken {
    Income,
    Expense,
    Primary,
    Secondary,
    Muted
}

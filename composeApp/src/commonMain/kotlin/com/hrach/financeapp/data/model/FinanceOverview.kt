package com.hrach.financeapp.data.model

data class FinanceOverview(
    val userEmail: String,
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
    val title: String,
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

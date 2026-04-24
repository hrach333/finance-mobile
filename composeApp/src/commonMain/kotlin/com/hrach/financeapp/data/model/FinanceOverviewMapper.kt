package com.hrach.financeapp.data.model

import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.data.dto.GroupMemberDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import kotlin.math.abs
import kotlin.math.roundToLong

fun toFinanceOverview(
    userEmail: String,
    groups: List<GroupDto>,
    activeGroupId: Int?,
    accounts: List<AccountDto>,
    categories: List<CategoryDto>,
    transactions: List<TransactionDto>,
    summary: SummaryDto?,
    members: List<GroupMemberDto>
): FinanceOverview {
    val activeGroupName = groups.firstOrNull { it.id == activeGroupId }?.name
        ?: groups.firstOrNull()?.name
        ?: "Нет группы"
    val categoryNames = categories.associate { it.id to it.name }
    val accountNames = accounts.associate { it.id to it.name }

    return FinanceOverview(
        userEmail = userEmail,
        activeGroupId = activeGroupId,
        activeGroupName = activeGroupName,
        summary = FinanceSummary(
            balanceLabel = (summary?.balance ?: accounts.sumOf { it.currentBalance }).moneyLabel(),
            incomeLabel = (summary?.income ?: 0.0).moneyLabel(),
            expenseLabel = (summary?.expense ?: 0.0).moneyLabel(),
            subtitle = "${accounts.size} ${accounts.size.plural("счет", "счета", "счетов")}, ${members.size} ${members.size.plural("участник", "участника", "участников")}"
        ),
        accounts = accounts.map { account ->
            AccountOverview(
                id = account.id,
                groupId = account.groupId,
                userId = account.userId,
                title = account.name,
                type = account.type,
                currency = account.currency,
                initialBalance = account.initialBalance,
                currentBalance = account.currentBalance,
                shared = account.shared,
                isActive = account.isActive,
                balanceLabel = account.currentBalance.moneyLabel(account.currency),
                subtitle = "${account.type.toAccountTypeLabel()} · ${if (account.shared) "Общий" else "Личный"}",
                colorToken = account.type.toAccountColorToken()
            )
        },
        transactions = transactions
            .sortedByDescending { it.transactionDate }
            .map { transaction ->
                val kind = transaction.type.toTransactionKind()
                TransactionOverview(
                    id = transaction.id,
                    groupId = transaction.groupId,
                    accountId = transaction.accountId,
                    createdBy = transaction.createdBy,
                    categoryId = transaction.categoryId,
                    category = categoryNames[transaction.categoryId] ?: transaction.type.toTransactionTypeLabel(),
                    comment = transaction.comment?.takeIf { it.isNotBlank() }
                        ?: accountNames[transaction.accountId]
                        ?: "Счет #${transaction.accountId}",
                    amount = transaction.amount,
                    currency = transaction.currency,
                    transactionDate = transaction.transactionDate,
                    amountLabel = transaction.amount.moneyLabel(transaction.currency, kind),
                    dateLabel = transaction.transactionDate.toShortDateLabel(),
                    kind = kind,
                    colorToken = when (kind) {
                        TransactionKind.Income -> OverviewColorToken.Income
                        TransactionKind.Expense -> OverviewColorToken.Expense
                    }
                )
            },
        insights = buildInsights(summary, categories, transactions)
    )
}

private fun buildInsights(
    summary: SummaryDto?,
    categories: List<CategoryDto>,
    transactions: List<TransactionDto>
): List<String> {
    val insights = mutableListOf<String>()
    if (summary != null) {
        val direction = if (summary.balance >= 0.0) "положительный" else "отрицательный"
        insights += "Баланс периода $direction: ${summary.balance.moneyLabel()}."
    }

    val categoryNames = categories.associate { it.id to it.name }
    val topExpense = transactions
        .filter { it.type.equals("EXPENSE", ignoreCase = true) }
        .groupBy { it.categoryId }
        .mapValues { item -> item.value.sumOf { it.amount } }
        .maxByOrNull { it.value }

    if (topExpense != null) {
        insights += "Самая крупная категория расходов: ${categoryNames[topExpense.key] ?: "Без категории"}."
    }

    if (insights.isEmpty()) {
        insights += "Данные загружены, аналитика появится после первых операций."
    }
    return insights
}

private fun Double.moneyLabel(currency: String = "RUB", kind: TransactionKind? = null): String {
    val sign = when (kind) {
        TransactionKind.Income -> "+"
        TransactionKind.Expense -> "-"
        null -> if (this < 0.0) "-" else ""
    }
    val rounded = abs(this).roundToLong().toString()
    val grouped = rounded.reversed().chunked(3).joinToString(" ").reversed()
    return "$sign$grouped ${currency.currencySymbol()}"
}

private fun String.currencySymbol(): String = when (uppercase()) {
    "RUB" -> "₽"
    "USD" -> "$"
    "EUR" -> "€"
    else -> this
}

private fun String.toAccountTypeLabel(): String = when (uppercase()) {
    "CASH" -> "Наличные"
    "CARD" -> "Карта"
    "BANK" -> "Банковский счет"
    "SAVINGS" -> "Накопления"
    else -> this
}

private fun String.toTransactionTypeLabel(): String = when (uppercase()) {
    "INCOME" -> "Доход"
    "EXPENSE" -> "Расход"
    else -> this
}

private fun String.toTransactionKind(): TransactionKind = when (uppercase()) {
    "INCOME" -> TransactionKind.Income
    else -> TransactionKind.Expense
}

private fun String.toAccountColorToken(): OverviewColorToken = when (uppercase()) {
    "SAVINGS" -> OverviewColorToken.Primary
    "CARD", "BANK" -> OverviewColorToken.Secondary
    "CASH" -> OverviewColorToken.Muted
    else -> OverviewColorToken.Primary
}

private fun String.toShortDateLabel(): String {
    val datePart = take(10)
    return if (datePart.length == 10 && datePart[4] == '-' && datePart[7] == '-') {
        "${datePart.substring(8, 10)}.${datePart.substring(5, 7)}"
    } else {
        datePart
    }
}

private fun Int.plural(one: String, few: String, many: String): String {
    val mod100 = this % 100
    val mod10 = this % 10
    return when {
        mod100 in 11..14 -> many
        mod10 == 1 -> one
        mod10 in 2..4 -> few
        else -> many
    }
}

package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.FinanceSummary
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview

class DemoFinanceOverviewRepository : FinanceOverviewRepository, AccountMutationsRepository {
    private var nextAccountId = 4
    private val accounts = mutableListOf(
        AccountOverview(
            id = 1,
            groupId = 1,
            title = "Основная карта",
            type = "CARD",
            currency = "RUB",
            initialBalance = 93_520.0,
            currentBalance = 93_520.0,
            balanceLabel = "93 520 ₽",
            subtitle = "Карта для ежедневных расходов",
            colorToken = OverviewColorToken.Secondary
        ),
        AccountOverview(
            id = 2,
            groupId = 1,
            title = "Наличные",
            type = "CASH",
            currency = "RUB",
            initialBalance = 18_300.0,
            currentBalance = 18_300.0,
            balanceLabel = "18 300 ₽",
            subtitle = "Домашний резерв",
            colorToken = OverviewColorToken.Muted
        ),
        AccountOverview(
            id = 3,
            groupId = 1,
            title = "Накопления",
            type = "SAVINGS",
            currency = "RUB",
            initialBalance = 265_000.0,
            currentBalance = 265_000.0,
            balanceLabel = "265 000 ₽",
            subtitle = "Цель: отпуск и подушка",
            colorToken = OverviewColorToken.Primary
        )
    )

    override suspend fun getOverview(): FinanceOverview {
        return FinanceOverview(
            userEmail = "demo@smartbudget.app",
            activeGroupId = 1,
            activeGroupName = "Семейный бюджет",
            summary = FinanceSummary(
                balanceLabel = "376 820 ₽",
                incomeLabel = "168 000 ₽",
                expenseLabel = "42 850 ₽",
                subtitle = "4 счета, 2 участника группы"
            ),
            accounts = accounts.toList(),
            transactions = listOf(
                TransactionOverview("Зарплата", "Основная карта", "+150 000 ₽", "23.04", TransactionKind.Income, OverviewColorToken.Income),
                TransactionOverview("Продукты", "Семья", "-8 420 ₽", "22.04", TransactionKind.Expense, OverviewColorToken.Expense),
                TransactionOverview("Такси", "Транспорт", "-1 150 ₽", "21.04", TransactionKind.Expense, OverviewColorToken.Secondary),
                TransactionOverview("Подработка", "Наличные", "+18 000 ₽", "20.04", TransactionKind.Income, OverviewColorToken.Primary),
                TransactionOverview("Аптека", "Здоровье", "-2 340 ₽", "19.04", TransactionKind.Expense, OverviewColorToken.Muted)
            ),
            insights = listOf(
                "Баланс месяца положительный: доходы выше расходов на 125 150 ₽.",
                "Самая крупная категория расходов сейчас: семья и продукты.",
                "Следующий технический шаг: общий API-клиент и авторизация в Multiplatform."
            )
        )
    }

    override suspend fun createAccount(
        groupId: Int,
        name: String,
        type: String,
        initialBalance: Double,
        shared: Boolean
    ) {
        accounts += AccountOverview(
            id = nextAccountId++,
            groupId = groupId,
            title = name,
            type = type,
            currency = "RUB",
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            shared = shared,
            balanceLabel = initialBalance.toDemoMoneyLabel(),
            subtitle = "${type.toDemoAccountTypeLabel()} · ${if (shared) "Общий" else "Личный"}",
            colorToken = type.toDemoAccountColorToken()
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
        val index = accounts.indexOfFirst { it.id == accountId }
        if (index < 0) return

        accounts[index] = accounts[index].copy(
            groupId = groupId,
            userId = userId,
            title = name,
            type = type,
            currency = currency,
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            shared = shared,
            isActive = isActive,
            balanceLabel = initialBalance.toDemoMoneyLabel(currency),
            subtitle = "${type.toDemoAccountTypeLabel()} · ${if (shared) "Общий" else "Личный"}",
            colorToken = type.toDemoAccountColorToken()
        )
    }

    override suspend fun deleteAccount(accountId: Int) {
        accounts.removeAll { it.id == accountId }
    }
}

private fun Double.toDemoMoneyLabel(currency: String = "RUB"): String {
    val rounded = toLong().toString().reversed().chunked(3).joinToString(" ").reversed()
    val symbol = if (currency == "RUB") "₽" else currency
    return "$rounded $symbol"
}

private fun String.toDemoAccountTypeLabel(): String = when (uppercase()) {
    "CASH" -> "Наличные"
    "CARD" -> "Карта"
    "BANK" -> "Банковский счет"
    "SAVINGS" -> "Накопления"
    else -> this
}

private fun String.toDemoAccountColorToken(): OverviewColorToken = when (uppercase()) {
    "SAVINGS" -> OverviewColorToken.Primary
    "CARD", "BANK" -> OverviewColorToken.Secondary
    "CASH" -> OverviewColorToken.Muted
    else -> OverviewColorToken.Primary
}

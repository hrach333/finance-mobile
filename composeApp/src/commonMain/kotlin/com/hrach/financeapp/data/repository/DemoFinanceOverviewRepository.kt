package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.FinanceSummary
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview

class DemoFinanceOverviewRepository : FinanceOverviewRepository {
    override suspend fun getOverview(): FinanceOverview {
        return FinanceOverview(
            userEmail = "demo@smartbudget.app",
            activeGroupName = "Семейный бюджет",
            summary = FinanceSummary(
                balanceLabel = "376 820 ₽",
                incomeLabel = "168 000 ₽",
                expenseLabel = "42 850 ₽",
                subtitle = "4 счета, 2 участника группы"
            ),
            accounts = listOf(
                AccountOverview(
                    title = "Основная карта",
                    balanceLabel = "93 520 ₽",
                    subtitle = "Карта для ежедневных расходов",
                    colorToken = OverviewColorToken.Secondary
                ),
                AccountOverview(
                    title = "Наличные",
                    balanceLabel = "18 300 ₽",
                    subtitle = "Домашний резерв",
                    colorToken = OverviewColorToken.Muted
                ),
                AccountOverview(
                    title = "Накопления",
                    balanceLabel = "265 000 ₽",
                    subtitle = "Цель: отпуск и подушка",
                    colorToken = OverviewColorToken.Primary
                )
            ),
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
}

package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import kotlin.math.roundToInt

object AIPromptBuilder {
    fun buildFinanceAdvicePrompt(
        transactions: List<TransactionDto>,
        categories: List<CategoryDto>,
        summary: SummaryDto?
    ): String {
        val totalIncome = summary?.income ?: 0.0
        val totalExpense = summary?.expense ?: 0.0
        val balance = summary?.balance ?: 0.0

        val expenseTransactions = transactions.filter { it.type.uppercase() == "EXPENSE" }
        val categoryTotals = expenseTransactions.groupBy { it.categoryId }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        val categoryBreakdown = categoryTotals.entries
            .sortedByDescending { it.value }
            .map { entry ->
                val categoryName = entry.key?.let { id ->
                    categories.firstOrNull { category -> category.id == id }?.name
                } ?: "Без категории"
                val percentage = if (totalExpense > 0) {
                    (entry.value / totalExpense * 100).roundToInt()
                } else {
                    0
                }
                "$categoryName: ${entry.value.roundToInt()} ₽ ($percentage%)"
            }
            .joinToString("\n")

        val topExpenses = expenseTransactions
            .sortedByDescending { it.amount }
            .take(5)
            .map { "${it.comment ?: "Операция"}: ${it.amount.roundToInt()} ₽" }
            .joinToString("\n")

        return """
Ты финансовый советник. Проанализируй моё финансовое состояние и дай практические советы по оптимизации расходов.

ФИНАНСОВОЕ РЕЗЮМЕ:
- Общий доход: $totalIncome ₽
- Общие расходы: $totalExpense ₽
- Баланс: $balance ₽
- Средний расход: ${if (expenseTransactions.isNotEmpty()) (totalExpense / expenseTransactions.size).roundToInt() else 0} ₽ за операцию

СТРУКТУРА РАСХОДОВ ПО КАТЕГОРИЯМ:
$categoryBreakdown

ТОП 5 БОЛЬШИХ РАСХОДОВ:
$topExpenses

На основе этих данных дай мне:
1. Главный финансовый совет (как экономить деньги)
2. 2-3 конкретных идеи по сокращению расходов на основе моих категорий
3. Наблюдение о моих привычках траты
4. Рекомендацию по целевому сбережению

Ответ пиши на русском языке, кратко и по делу. Форматируй ответ с использованием переносов строк для лучшей читаемости.
        """.trimIndent()
    }
}

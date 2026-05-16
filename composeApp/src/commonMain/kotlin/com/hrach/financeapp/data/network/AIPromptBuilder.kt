package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.SummaryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.TransactionKind
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

        return buildFinanceAdvicePrompt(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            incomeCategoryLines = categoryBreakdown(
                total = totalIncome,
                items = transactions
                    .filter { it.type.uppercase() == "INCOME" }
                    .map { PromptTransaction(it.categoryId, it.amount, it.comment) },
                categoryName = { id -> categories.firstOrNull { category -> category.id == id }?.name }
            ),
            expenseCategoryLines = categoryBreakdown(
                total = totalExpense,
                items = transactions
                    .filter { it.type.uppercase() == "EXPENSE" }
                    .map { PromptTransaction(it.categoryId, it.amount, it.comment) },
                categoryName = { id -> categories.firstOrNull { category -> category.id == id }?.name }
            ),
            averageExpense = transactions
                .filter { it.type.uppercase() == "EXPENSE" }
                .takeIf { it.isNotEmpty() }
                ?.let { totalExpense / it.size }
                ?: 0.0,
            topExpenses = transactions
                .filter { it.type.uppercase() == "EXPENSE" }
                .map { PromptTransaction(it.categoryId, it.amount, it.comment) }
        )
    }

    fun buildFinanceAdvicePrompt(overview: FinanceOverview): String {
        val totalIncome = overview.transactions
            .filter { it.kind == TransactionKind.Income }
            .sumOf { it.amount }
        val totalExpense = overview.transactions
            .filter { it.kind == TransactionKind.Expense }
            .sumOf { it.amount }
        val categoryNames = overview.categories.associate { it.id to it.name }

        return buildFinanceAdvicePrompt(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = totalIncome - totalExpense,
            incomeCategoryLines = categoryBreakdown(
                total = totalIncome,
                items = overview.transactions
                    .filter { it.kind == TransactionKind.Income }
                    .map { PromptTransaction(it.categoryId, it.amount, it.comment) },
                categoryName = { id -> categoryNames[id] }
            ),
            expenseCategoryLines = categoryBreakdown(
                total = totalExpense,
                items = overview.transactions
                    .filter { it.kind == TransactionKind.Expense }
                    .map { PromptTransaction(it.categoryId, it.amount, it.comment) },
                categoryName = { id -> categoryNames[id] }
            ),
            averageExpense = overview.transactions
                .filter { it.kind == TransactionKind.Expense }
                .takeIf { it.isNotEmpty() }
                ?.let { totalExpense / it.size }
                ?: 0.0,
            topExpenses = overview.transactions
                .filter { it.kind == TransactionKind.Expense }
                .map { PromptTransaction(it.categoryId, it.amount, it.comment) }
        )
    }

    private fun buildFinanceAdvicePrompt(
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        incomeCategoryLines: String,
        expenseCategoryLines: String,
        averageExpense: Double,
        topExpenses: List<PromptTransaction>
    ): String {
        val topExpenseLines = topExpenses
            .sortedByDescending { it.amount }
            .take(5)
            .joinToString("\n") {
                "${it.comment?.takeIf { comment -> comment.isNotBlank() } ?: "Операция"}: ${it.amount.roundToInt()} ₽"
            }

        val savingsRate = if (totalIncome > 0.0) {
            (balance / totalIncome * 100).roundToInt()
        } else {
            0
        }

        return """
Ты финансовый советник. Проанализируй мои финансы и дай практические рекомендации по оптимизации бюджета.

ФИНАНСОВОЕ РЕЗЮМЕ:
- Доходы: ${totalIncome.roundToInt()} ₽
- Расходы: ${totalExpense.roundToInt()} ₽
- Баланс: ${balance.roundToInt()} ₽
- Доля сбережений от дохода: $savingsRate%
- Средний расход: ${averageExpense.roundToInt()} ₽ за операцию

ДОХОДЫ ПО КАТЕГОРИЯМ:
$incomeCategoryLines

РАСХОДЫ ПО КАТЕГОРИЯМ:
$expenseCategoryLines

ТОП 5 КРУПНЫХ РАСХОДОВ:
${topExpenseLines.ifBlank { "Нет расходов" }}

На основе этих данных дай:
1. Короткую оценку бюджета.
2. 2-3 конкретных идеи, где можно снизить расходы по категориям.
3. Наблюдение о структуре доходов и расходов.
4. Реалистичную рекомендацию по целевому сбережению.

Ответ пиши на русском языке, кратко и по делу. Не выдумывай данные, которых нет в списке.
        """.trimIndent()
    }

    private fun categoryBreakdown(
        total: Double,
        items: List<PromptTransaction>,
        categoryName: (Int) -> String?
    ): String {
        return items.groupBy { it.categoryId }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }.entries
            .sortedByDescending { it.value }
            .map { entry ->
                val name = entry.key?.let(categoryName) ?: "Без категории"
                val percentage = if (total > 0) {
                    (entry.value / total * 100).roundToInt()
                } else {
                    0
                }
                "$name: ${entry.value.roundToInt()} ₽ ($percentage%)"
            }.joinToString("\n")
            .ifBlank { "Нет данных" }
    }

    private data class PromptTransaction(
        val categoryId: Int?,
        val amount: Double,
        val comment: String?
    )
}

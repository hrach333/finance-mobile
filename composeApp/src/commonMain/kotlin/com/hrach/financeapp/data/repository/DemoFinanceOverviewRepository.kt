package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.currency.CurrencyCatalog
import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.CategoryOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.FinanceSummary
import com.hrach.financeapp.data.model.GroupOverview
import com.hrach.financeapp.data.model.GroupMemberOverview
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview

class DemoFinanceOverviewRepository :
    FinanceOverviewRepository,
    AccountMutationsRepository,
    CategoryMutationsRepository,
    GroupMutationsRepository,
    GroupMemberMutationsRepository,
    TransactionMutationsRepository {
    private var selectedGroupId = 1
    private var nextGroupId = 2
    private var nextAccountId = 4
    private var nextCategoryId = 6
    private var nextMemberId = 3
    private var nextTransactionId = 6
    private val groups = mutableListOf(
        GroupOverview(id = 1, name = "Семейный бюджет", baseCurrency = CurrencyCatalog.DEFAULT_CODE)
    )
    private val categories = mutableListOf(
        CategoryOverview(id = 1, groupId = 1, type = "EXPENSE", name = "Продукты", iconKey = "shopping"),
        CategoryOverview(id = 2, groupId = 1, type = "EXPENSE", name = "Транспорт", iconKey = "transport"),
        CategoryOverview(id = 3, groupId = 1, type = "EXPENSE", name = "Здоровье", iconKey = "health"),
        CategoryOverview(id = 4, groupId = 1, type = "INCOME", name = "Зарплата", iconKey = "salary"),
        CategoryOverview(id = 5, groupId = 1, type = "INCOME", name = "Подработка", iconKey = "work")
    )
    private val accounts = mutableListOf(
        AccountOverview(
            id = 1,
            groupId = 1,
            title = "Основная карта",
            type = "CARD",
            currency = CurrencyCatalog.DEFAULT_CODE,
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
            currency = CurrencyCatalog.DEFAULT_CODE,
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
            currency = CurrencyCatalog.DEFAULT_CODE,
            initialBalance = 265_000.0,
            currentBalance = 265_000.0,
            balanceLabel = "265 000 ₽",
            subtitle = "Цель: отпуск и подушка",
            colorToken = OverviewColorToken.Primary
        )
    )
    private val members = mutableListOf(
        GroupMemberOverview(id = 1, userId = 1, role = "admin", userName = "Демо Админ", userEmail = "demo@smartbudget.app"),
        GroupMemberOverview(id = 2, userId = 2, role = "member", userName = "Семья", userEmail = "family@smartbudget.app")
    )
    private val transactions = mutableListOf(
        TransactionOverview(
            id = 1,
            groupId = 1,
            accountId = 1,
            categoryId = 4,
            category = "Зарплата",
            comment = "Основная карта",
            amount = 150_000.0,
            transactionDate = "2026-04-23",
            amountLabel = "+150 000 ₽",
            dateLabel = "23.04",
            kind = TransactionKind.Income,
            colorToken = OverviewColorToken.Income
        ),
        TransactionOverview(
            id = 2,
            groupId = 1,
            accountId = 1,
            categoryId = 1,
            category = "Продукты",
            comment = "Семья",
            amount = 8_420.0,
            transactionDate = "2026-04-22",
            amountLabel = "-8 420 ₽",
            dateLabel = "22.04",
            kind = TransactionKind.Expense,
            colorToken = OverviewColorToken.Expense
        ),
        TransactionOverview(
            id = 3,
            groupId = 1,
            accountId = 1,
            categoryId = 2,
            category = "Такси",
            comment = "Транспорт",
            amount = 1_150.0,
            transactionDate = "2026-04-21",
            amountLabel = "-1 150 ₽",
            dateLabel = "21.04",
            kind = TransactionKind.Expense,
            colorToken = OverviewColorToken.Secondary
        ),
        TransactionOverview(
            id = 4,
            groupId = 1,
            accountId = 2,
            categoryId = 5,
            category = "Подработка",
            comment = "Наличные",
            amount = 18_000.0,
            transactionDate = "2026-04-20",
            amountLabel = "+18 000 ₽",
            dateLabel = "20.04",
            kind = TransactionKind.Income,
            colorToken = OverviewColorToken.Primary
        ),
        TransactionOverview(
            id = 5,
            groupId = 1,
            accountId = 1,
            categoryId = 3,
            category = "Аптека",
            comment = "Здоровье",
            amount = 2_340.0,
            transactionDate = "2026-04-19",
            amountLabel = "-2 340 ₽",
            dateLabel = "19.04",
            kind = TransactionKind.Expense,
            colorToken = OverviewColorToken.Muted
        )
    )

    override suspend fun getOverview(): FinanceOverview {
        val activeGroup = groups.firstOrNull { it.id == selectedGroupId } ?: groups.firstOrNull()
        val groupId = activeGroup?.id
        val groupAccounts = accounts.filter { it.groupId == groupId }
        val groupCategories = categories.filter { it.groupId == groupId }
        val groupTransactions = transactions.filter { it.groupId == groupId }
        return FinanceOverview(
            userEmail = "demo@smartbudget.app",
            activeGroupId = groupId,
            activeGroupName = activeGroup?.name ?: "Нет группы",
            groups = groups.toList(),
            summary = FinanceSummary(
                balanceLabel = groupAccounts.sumOf { it.currentBalance }.toDemoMoneyLabel(activeGroup?.baseCurrency),
                incomeLabel = 168_000.0.toDemoMoneyLabel(activeGroup?.baseCurrency),
                expenseLabel = 42_850.0.toDemoMoneyLabel(activeGroup?.baseCurrency),
                subtitle = "${groupAccounts.size} счетов, ${members.size} участника группы"
            ),
            accounts = groupAccounts,
            categories = groupCategories,
            members = members.toList(),
            transactions = groupTransactions,
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
        currency: String,
        shared: Boolean
    ) {
        accounts += AccountOverview(
            id = nextAccountId++,
            groupId = groupId,
            title = name,
            type = type,
            currency = currency,
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            shared = shared,
            balanceLabel = initialBalance.toDemoMoneyLabel(currency),
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

    override suspend fun createCategory(
        groupId: Int,
        name: String,
        type: String,
        iconKey: String?
    ) {
        categories += CategoryOverview(
            id = nextCategoryId++,
            groupId = groupId,
            type = type,
            name = name,
            iconKey = iconKey
        )
    }

    override suspend fun updateCategory(
        categoryId: Int,
        name: String,
        type: String,
        iconKey: String?
    ) {
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index < 0) return

        categories[index] = categories[index].copy(
            name = name,
            type = type,
            iconKey = iconKey
        )
    }

    override suspend fun deleteCategory(categoryId: Int) {
        categories.removeAll { it.id == categoryId }
    }

    override suspend fun createGroup(name: String, baseCurrency: String) {
        val group = GroupOverview(
            id = nextGroupId++,
            name = name.trim(),
            baseCurrency = CurrencyCatalog.normalize(baseCurrency)
        )
        groups += group
        selectedGroupId = group.id
    }

    override suspend fun updateGroup(groupId: Int, name: String, baseCurrency: String) {
        val index = groups.indexOfFirst { it.id == groupId }
        if (index < 0) return

        groups[index] = groups[index].copy(
            name = name.trim(),
            baseCurrency = CurrencyCatalog.normalize(baseCurrency)
        )
    }

    override suspend fun selectGroup(groupId: Int) {
        if (groups.any { it.id == groupId }) {
            selectedGroupId = groupId
        }
    }

    override suspend fun addGroupMember(groupId: Int, email: String, role: String) {
        members += GroupMemberOverview(
            id = nextMemberId++,
            role = role,
            userName = email.substringBefore('@').takeIf { it.isNotBlank() },
            userEmail = email
        )
    }

    override suspend fun updateGroupMemberRole(groupId: Int, memberId: Int, role: String) {
        val index = members.indexOfFirst { it.id == memberId }
        if (index < 0) return

        members[index] = members[index].copy(role = role)
    }

    override suspend fun deleteGroupMember(groupId: Int, memberId: Int) {
        members.removeAll { it.id == memberId }
    }

    override suspend fun createTransaction(
        groupId: Int,
        accountId: Int,
        createdBy: Int?,
        type: String,
        amount: Double,
        currency: String,
        categoryId: Int?,
        transactionDate: String,
        comment: String?
    ) {
        val kind = type.toDemoTransactionKind()
        transactions.add(
            0,
            TransactionOverview(
                id = nextTransactionId++,
                groupId = groupId,
                accountId = accountId,
                createdBy = createdBy,
                categoryId = categoryId,
                category = categories.firstOrNull { it.id == categoryId }?.name ?: type.toDemoTransactionTypeLabel(),
                comment = comment?.takeIf { it.isNotBlank() }
                    ?: accounts.firstOrNull { it.id == accountId }?.title
                    ?: "Счет #$accountId",
                amount = amount,
                currency = currency,
                transactionDate = transactionDate,
                amountLabel = amount.toDemoTransactionMoneyLabel(currency, kind),
                dateLabel = transactionDate.toDemoShortDateLabel(),
                kind = kind,
                colorToken = if (kind == TransactionKind.Income) OverviewColorToken.Income else OverviewColorToken.Expense
            )
        )
    }

    override suspend fun updateTransaction(
        transactionId: Int,
        groupId: Int,
        accountId: Int,
        createdBy: Int?,
        type: String,
        amount: Double,
        currency: String,
        categoryId: Int?,
        transactionDate: String,
        comment: String?
    ) {
        val index = transactions.indexOfFirst { it.id == transactionId }
        if (index < 0) return

        val kind = type.toDemoTransactionKind()
        transactions[index] = transactions[index].copy(
            groupId = groupId,
            accountId = accountId,
            createdBy = createdBy,
            categoryId = categoryId,
            category = categories.firstOrNull { it.id == categoryId }?.name ?: type.toDemoTransactionTypeLabel(),
            comment = comment?.takeIf { it.isNotBlank() }
                ?: accounts.firstOrNull { it.id == accountId }?.title
                ?: "Счет #$accountId",
            amount = amount,
            currency = currency,
            transactionDate = transactionDate,
            amountLabel = amount.toDemoTransactionMoneyLabel(currency, kind),
            dateLabel = transactionDate.toDemoShortDateLabel(),
            kind = kind,
            colorToken = if (kind == TransactionKind.Income) OverviewColorToken.Income else OverviewColorToken.Expense
        )
    }

    override suspend fun deleteTransaction(transactionId: Int) {
        transactions.removeAll { it.id == transactionId }
    }
}

private fun Double.toDemoMoneyLabel(currency: String? = CurrencyCatalog.DEFAULT_CODE): String {
    val rounded = toLong().toString().reversed().chunked(3).joinToString(" ").reversed()
    return "$rounded ${CurrencyCatalog.symbolFor(currency)}"
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

private fun String.toDemoTransactionKind(): TransactionKind = when (uppercase()) {
    "INCOME" -> TransactionKind.Income
    else -> TransactionKind.Expense
}

private fun String.toDemoTransactionTypeLabel(): String = when (uppercase()) {
    "INCOME" -> "Доход"
    "EXPENSE" -> "Расход"
    else -> this
}

private fun Double.toDemoTransactionMoneyLabel(currency: String, kind: TransactionKind): String {
    val sign = if (kind == TransactionKind.Income) "+" else "-"
    return "$sign${toDemoMoneyLabel(currency)}"
}

private fun String.toDemoShortDateLabel(): String {
    return if (length >= 10 && this[4] == '-' && this[7] == '-') {
        "${substring(8, 10)}.${substring(5, 7)}"
    } else {
        this
    }
}

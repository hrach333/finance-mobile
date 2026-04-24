package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.CategoryOverview
import com.hrach.financeapp.data.model.GroupMemberOverview
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview
import com.hrach.financeapp.data.repository.AccountMutationsRepository
import com.hrach.financeapp.data.repository.CategoryMutationsRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository
import com.hrach.financeapp.data.repository.GroupMemberMutationsRepository
import com.hrach.financeapp.data.repository.TransactionMutationsRepository

class FinanceDashboardController(
    private val repository: FinanceOverviewRepository
) {
    private val loader = FinanceOverviewLoader(repository)
    private val accountMutations = repository as? AccountMutationsRepository
    private val categoryMutations = repository as? CategoryMutationsRepository
    private val memberMutations = repository as? GroupMemberMutationsRepository
    private val transactionMutations = repository as? TransactionMutationsRepository
    private var nextOptimisticIdValue = -1

    var state = FinanceDashboardState()
        private set

    fun selectTab(tab: DashboardTab): FinanceDashboardState {
        state = state.copy(selectedTab = tab)
        return state
    }

    fun markLoading(): FinanceDashboardState {
        state = state.copy(isLoading = true, errorMessage = null)
        return state
    }

    fun previewCreateAccount(name: String, type: String, initialBalance: Double): FinanceDashboardState {
        val overview = state.overview ?: return state
        val groupId = overview.activeGroupId ?: return state
        val account = AccountOverview(
            id = nextOptimisticId(),
            groupId = groupId,
            title = name.trim(),
            type = type,
            currency = "RUB",
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            balanceLabel = initialBalance.moneyLabel("RUB"),
            subtitle = "${type.toAccountTypeLabel()} · Общий",
            colorToken = type.toAccountColorToken()
        )
        state = state.copy(
            overview = overview.copy(accounts = overview.accounts + account),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewUpdateAccount(account: AccountOverview, name: String, type: String, initialBalance: Double): FinanceDashboardState {
        val overview = state.overview ?: return state
        val updated = account.copy(
            title = name.trim(),
            type = type,
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            balanceLabel = initialBalance.moneyLabel(account.currency),
            subtitle = "${type.toAccountTypeLabel()} · ${if (account.shared) "Общий" else "Личный"}",
            colorToken = type.toAccountColorToken()
        )
        state = state.copy(
            overview = overview.copy(accounts = overview.accounts.map { if (it.id == account.id) updated else it }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewDeleteAccount(account: AccountOverview): FinanceDashboardState {
        val overview = state.overview ?: return state
        state = state.copy(
            overview = overview.copy(accounts = overview.accounts.filterNot { it.id == account.id }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewCreateCategory(name: String, type: String, iconKey: String?): FinanceDashboardState {
        val overview = state.overview ?: return state
        val groupId = overview.activeGroupId ?: return state
        val category = CategoryOverview(
            id = nextOptimisticId(),
            groupId = groupId,
            type = type,
            name = name.trim(),
            iconKey = iconKey
        )
        state = state.copy(
            overview = overview.copy(categories = overview.categories + category),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewUpdateCategory(category: CategoryOverview, name: String, type: String, iconKey: String?): FinanceDashboardState {
        val overview = state.overview ?: return state
        val updated = category.copy(name = name.trim(), type = type, iconKey = iconKey)
        state = state.copy(
            overview = overview.copy(categories = overview.categories.map { if (it.id == category.id) updated else it }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewDeleteCategory(category: CategoryOverview): FinanceDashboardState {
        val overview = state.overview ?: return state
        state = state.copy(
            overview = overview.copy(categories = overview.categories.filterNot { it.id == category.id }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewAddGroupMember(email: String, role: String): FinanceDashboardState {
        val overview = state.overview ?: return state
        val member = GroupMemberOverview(
            id = nextOptimisticId(),
            role = role,
            userName = email.substringBefore('@').takeIf { it.isNotBlank() },
            userEmail = email.trim()
        )
        state = state.copy(
            overview = overview.copy(members = overview.members + member),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewUpdateGroupMemberRole(member: GroupMemberOverview, role: String): FinanceDashboardState {
        val overview = state.overview ?: return state
        state = state.copy(
            overview = overview.copy(members = overview.members.map { if (it.id == member.id) it.copy(role = role) else it }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewDeleteGroupMember(member: GroupMemberOverview): FinanceDashboardState {
        val overview = state.overview ?: return state
        state = state.copy(
            overview = overview.copy(members = overview.members.filterNot { it.id == member.id }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewCreateTransaction(
        type: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        transactionDate: String,
        comment: String
    ): FinanceDashboardState {
        val overview = state.overview ?: return state
        val groupId = overview.activeGroupId ?: return state
        val account = overview.accounts.firstOrNull { it.id == accountId } ?: return state
        val category = overview.categories.firstOrNull { it.id == categoryId }
        val kind = type.toTransactionKind()
        val transaction = TransactionOverview(
            id = nextOptimisticId(),
            groupId = groupId,
            accountId = accountId,
            categoryId = categoryId,
            category = category?.name ?: type.toTransactionTypeLabel(),
            comment = comment.takeIf { it.isNotBlank() } ?: account.title,
            amount = amount,
            currency = account.currency,
            transactionDate = transactionDate,
            amountLabel = amount.moneyLabel(account.currency, kind),
            dateLabel = transactionDate.toShortDateLabel(),
            kind = kind,
            colorToken = if (kind == TransactionKind.Income) OverviewColorToken.Income else OverviewColorToken.Expense
        )
        state = state.copy(
            overview = overview.copy(transactions = listOf(transaction) + overview.transactions),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewUpdateTransaction(
        transaction: TransactionOverview,
        type: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        transactionDate: String,
        comment: String
    ): FinanceDashboardState {
        val overview = state.overview ?: return state
        val account = overview.accounts.firstOrNull { it.id == accountId } ?: return state
        val category = overview.categories.firstOrNull { it.id == categoryId }
        val kind = type.toTransactionKind()
        val updated = transaction.copy(
            accountId = accountId,
            categoryId = categoryId,
            category = category?.name ?: type.toTransactionTypeLabel(),
            comment = comment.takeIf { it.isNotBlank() } ?: account.title,
            amount = amount,
            currency = account.currency,
            transactionDate = transactionDate,
            amountLabel = amount.moneyLabel(account.currency, kind),
            dateLabel = transactionDate.toShortDateLabel(),
            kind = kind,
            colorToken = if (kind == TransactionKind.Income) OverviewColorToken.Income else OverviewColorToken.Expense
        )
        state = state.copy(
            overview = overview.copy(transactions = overview.transactions.map { if (it.id == transaction.id) updated else it }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    fun previewDeleteTransaction(transaction: TransactionOverview): FinanceDashboardState {
        val overview = state.overview ?: return state
        state = state.copy(
            overview = overview.copy(transactions = overview.transactions.filterNot { it.id == transaction.id }),
            isLoading = false,
            errorMessage = null
        )
        return state
    }

    suspend fun refresh(): FinanceDashboardEvent {
        state = state.copy(isLoading = true, errorMessage = null)

        return when (val result = loader.load()) {
            FinanceOverviewLoadResult.AuthExpired -> {
                state = state.copy(overview = null, isLoading = false)
                FinanceDashboardEvent.AuthExpired
            }
            is FinanceOverviewLoadResult.Failure -> {
                state = state.copy(overview = null, isLoading = false, errorMessage = result.message)
                FinanceDashboardEvent.None
            }
            is FinanceOverviewLoadResult.Success -> {
                state = state.copy(overview = result.overview, isLoading = false, errorMessage = null)
                FinanceDashboardEvent.None
            }
        }
    }

    suspend fun createAccount(name: String, type: String, initialBalance: Double, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет", rollbackState) {
            mutations.createAccount(
                groupId = groupId,
                name = name,
                type = type,
                initialBalance = initialBalance
            )
        }
    }

    suspend fun updateAccount(account: AccountOverview, name: String, type: String, initialBalance: Double, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val accountId = account.id ?: return failAction("Не удалось определить счет")
        val groupId = account.groupId ?: state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет", rollbackState) {
            mutations.updateAccount(
                accountId = accountId,
                groupId = groupId,
                userId = account.userId,
                name = name,
                type = type,
                currency = account.currency,
                initialBalance = initialBalance,
                shared = account.shared,
                isActive = account.isActive
            )
        }
    }

    suspend fun deleteAccount(account: AccountOverview, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val accountId = account.id ?: return failAction("Не удалось определить счет")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет", rollbackState) {
            mutations.deleteAccount(accountId)
        }
    }

    suspend fun createCategory(name: String, type: String, iconKey: String?, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось изменить категорию", rollbackState) {
            mutations.createCategory(
                groupId = groupId,
                name = name,
                type = type,
                iconKey = iconKey
            )
        }
    }

    suspend fun updateCategory(category: CategoryOverview, name: String, type: String, iconKey: String?, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось изменить категорию", rollbackState) {
            mutations.updateCategory(
                categoryId = category.id,
                name = name,
                type = type,
                iconKey = iconKey
            )
        }
    }

    suspend fun deleteCategory(category: CategoryOverview, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось удалить категорию", rollbackState) {
            mutations.deleteCategory(category.id)
        }
    }

    suspend fun addGroupMember(email: String, role: String, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = memberMutations ?: return failAction("Редактирование участников пока недоступно")

        return runMutationAction("Не удалось добавить участника", rollbackState) {
            mutations.addGroupMember(groupId = groupId, email = email, role = role)
        }
    }

    suspend fun updateGroupMemberRole(member: GroupMemberOverview, role: String, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = memberMutations ?: return failAction("Редактирование участников пока недоступно")

        return runMutationAction("Не удалось изменить роль участника", rollbackState) {
            mutations.updateGroupMemberRole(groupId = groupId, memberId = member.id, role = role)
        }
    }

    suspend fun deleteGroupMember(member: GroupMemberOverview, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = memberMutations ?: return failAction("Редактирование участников пока недоступно")

        return runMutationAction("Не удалось удалить участника", rollbackState) {
            mutations.deleteGroupMember(groupId = groupId, memberId = member.id)
        }
    }

    suspend fun createTransaction(
        type: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        transactionDate: String,
        comment: String,
        rollbackState: FinanceDashboardState? = null
    ): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")
        val account = state.overview?.accounts?.firstOrNull { it.id == accountId }
            ?: return failAction("Выберите счет")

        return runMutationAction("Не удалось изменить операцию", rollbackState) {
            mutations.createTransaction(
                groupId = groupId,
                accountId = accountId,
                createdBy = null,
                type = type,
                amount = amount,
                currency = account.currency,
                categoryId = categoryId,
                transactionDate = transactionDate,
                comment = comment.takeIf { it.isNotBlank() }
            )
        }
    }

    suspend fun updateTransaction(
        transaction: TransactionOverview,
        type: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        transactionDate: String,
        comment: String,
        rollbackState: FinanceDashboardState? = null
    ): FinanceDashboardEvent {
        val transactionId = transaction.id ?: return failAction("Не удалось определить операцию")
        val groupId = transaction.groupId ?: state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")
        val account = state.overview?.accounts?.firstOrNull { it.id == accountId }
            ?: return failAction("Выберите счет")

        return runMutationAction("Не удалось изменить операцию", rollbackState) {
            mutations.updateTransaction(
                transactionId = transactionId,
                groupId = groupId,
                accountId = accountId,
                createdBy = transaction.createdBy,
                type = type,
                amount = amount,
                currency = account.currency,
                categoryId = categoryId,
                transactionDate = transactionDate,
                comment = comment.takeIf { it.isNotBlank() }
            )
        }
    }

    suspend fun deleteTransaction(transaction: TransactionOverview, rollbackState: FinanceDashboardState? = null): FinanceDashboardEvent {
        val transactionId = transaction.id ?: return failAction("Не удалось определить операцию")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")

        return runMutationAction("Не удалось удалить операцию", rollbackState) {
            mutations.deleteTransaction(transactionId)
        }
    }

    private suspend fun runMutationAction(
        errorMessage: String,
        rollbackState: FinanceDashboardState? = null,
        action: suspend () -> Unit
    ): FinanceDashboardEvent {
        state = state.copy(errorMessage = null)
        return runCatching { action() }
            .fold(
                onSuccess = { refresh() },
                onFailure = { throwable ->
                    state = (rollbackState ?: state).copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: errorMessage
                    )
                    FinanceDashboardEvent.None
                }
            )
    }

    private fun failAction(message: String): FinanceDashboardEvent {
        state = state.copy(isLoading = false, errorMessage = message)
        return FinanceDashboardEvent.None
    }

    private fun nextOptimisticId(): Int = nextOptimisticIdValue--
}

private fun Double.moneyLabel(currency: String, kind: TransactionKind? = null): String {
    val sign = when (kind) {
        TransactionKind.Income -> "+"
        TransactionKind.Expense -> "-"
        null -> if (this < 0.0) "-" else ""
    }
    val rounded = kotlin.math.abs(this).toLong().toString()
    val grouped = rounded.reversed().chunked(3).joinToString(" ").reversed()
    val symbol = if (currency == "RUB") "₽" else currency
    return "$sign$grouped $symbol"
}

private fun String.toAccountTypeLabel(): String = when (uppercase()) {
    "CASH" -> "Наличные"
    "CARD" -> "Карта"
    "BANK" -> "Банковский счет"
    "SAVINGS" -> "Накопления"
    else -> this
}

private fun String.toAccountColorToken(): OverviewColorToken = when (uppercase()) {
    "SAVINGS" -> OverviewColorToken.Primary
    "CARD", "BANK" -> OverviewColorToken.Secondary
    "CASH" -> OverviewColorToken.Muted
    else -> OverviewColorToken.Primary
}

private fun String.toTransactionKind(): TransactionKind = when (uppercase()) {
    "INCOME" -> TransactionKind.Income
    else -> TransactionKind.Expense
}

private fun String.toTransactionTypeLabel(): String = when (uppercase()) {
    "INCOME" -> "Доход"
    "EXPENSE" -> "Расход"
    else -> this
}

private fun String.toShortDateLabel(): String {
    return if (length >= 10 && this[4] == '-' && this[7] == '-') {
        "${substring(8, 10)}.${substring(5, 7)}"
    } else {
        this
    }
}

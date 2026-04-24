package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.CategoryOverview
import com.hrach.financeapp.data.model.TransactionOverview
import com.hrach.financeapp.data.repository.AccountMutationsRepository
import com.hrach.financeapp.data.repository.CategoryMutationsRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository
import com.hrach.financeapp.data.repository.TransactionMutationsRepository

class FinanceDashboardController(
    private val repository: FinanceOverviewRepository
) {
    private val loader = FinanceOverviewLoader(repository)
    private val accountMutations = repository as? AccountMutationsRepository
    private val categoryMutations = repository as? CategoryMutationsRepository
    private val transactionMutations = repository as? TransactionMutationsRepository

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

    suspend fun createAccount(name: String, type: String, initialBalance: Double): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет") {
            mutations.createAccount(
                groupId = groupId,
                name = name,
                type = type,
                initialBalance = initialBalance
            )
        }
    }

    suspend fun updateAccount(account: AccountOverview, name: String, type: String, initialBalance: Double): FinanceDashboardEvent {
        val accountId = account.id ?: return failAction("Не удалось определить счет")
        val groupId = account.groupId ?: state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет") {
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

    suspend fun deleteAccount(account: AccountOverview): FinanceDashboardEvent {
        val accountId = account.id ?: return failAction("Не удалось определить счет")
        val mutations = accountMutations ?: return failAction("Редактирование счетов пока недоступно")

        return runMutationAction("Не удалось изменить счет") {
            mutations.deleteAccount(accountId)
        }
    }

    suspend fun createCategory(name: String, type: String, iconKey: String?): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось изменить категорию") {
            mutations.createCategory(
                groupId = groupId,
                name = name,
                type = type,
                iconKey = iconKey
            )
        }
    }

    suspend fun updateCategory(category: CategoryOverview, name: String, type: String, iconKey: String?): FinanceDashboardEvent {
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось изменить категорию") {
            mutations.updateCategory(
                categoryId = category.id,
                name = name,
                type = type,
                iconKey = iconKey
            )
        }
    }

    suspend fun deleteCategory(category: CategoryOverview): FinanceDashboardEvent {
        val mutations = categoryMutations ?: return failAction("Редактирование категорий пока недоступно")

        return runMutationAction("Не удалось удалить категорию") {
            mutations.deleteCategory(category.id)
        }
    }

    suspend fun createTransaction(
        type: String,
        amount: Double,
        accountId: Int,
        categoryId: Int,
        transactionDate: String,
        comment: String
    ): FinanceDashboardEvent {
        val groupId = state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")
        val account = state.overview?.accounts?.firstOrNull { it.id == accountId }
            ?: return failAction("Выберите счет")

        return runMutationAction("Не удалось изменить операцию") {
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
        comment: String
    ): FinanceDashboardEvent {
        val transactionId = transaction.id ?: return failAction("Не удалось определить операцию")
        val groupId = transaction.groupId ?: state.overview?.activeGroupId ?: return failAction("Нет активной группы")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")
        val account = state.overview?.accounts?.firstOrNull { it.id == accountId }
            ?: return failAction("Выберите счет")

        return runMutationAction("Не удалось изменить операцию") {
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

    suspend fun deleteTransaction(transaction: TransactionOverview): FinanceDashboardEvent {
        val transactionId = transaction.id ?: return failAction("Не удалось определить операцию")
        val mutations = transactionMutations ?: return failAction("Редактирование операций пока недоступно")

        return runMutationAction("Не удалось удалить операцию") {
            mutations.deleteTransaction(transactionId)
        }
    }

    private suspend fun runMutationAction(errorMessage: String, action: suspend () -> Unit): FinanceDashboardEvent {
        state = state.copy(isLoading = true, errorMessage = null)
        return runCatching { action() }
            .fold(
                onSuccess = { refresh() },
                onFailure = { throwable ->
                    state = state.copy(
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
}

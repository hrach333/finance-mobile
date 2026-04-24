package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.repository.AccountMutationsRepository
import com.hrach.financeapp.data.repository.FinanceOverviewRepository

class FinanceDashboardController(
    private val repository: FinanceOverviewRepository
) {
    private val loader = FinanceOverviewLoader(repository)
    private val accountMutations = repository as? AccountMutationsRepository

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
        val groupId = state.overview?.activeGroupId ?: return failAccountAction("Нет активной группы")
        val mutations = accountMutations ?: return failAccountAction("Редактирование счетов пока недоступно")

        return runAccountAction {
            mutations.createAccount(
                groupId = groupId,
                name = name,
                type = type,
                initialBalance = initialBalance
            )
        }
    }

    suspend fun updateAccount(account: AccountOverview, name: String, type: String, initialBalance: Double): FinanceDashboardEvent {
        val accountId = account.id ?: return failAccountAction("Не удалось определить счет")
        val groupId = account.groupId ?: state.overview?.activeGroupId ?: return failAccountAction("Нет активной группы")
        val mutations = accountMutations ?: return failAccountAction("Редактирование счетов пока недоступно")

        return runAccountAction {
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
        val accountId = account.id ?: return failAccountAction("Не удалось определить счет")
        val mutations = accountMutations ?: return failAccountAction("Редактирование счетов пока недоступно")

        return runAccountAction {
            mutations.deleteAccount(accountId)
        }
    }

    private suspend fun runAccountAction(action: suspend () -> Unit): FinanceDashboardEvent {
        state = state.copy(isLoading = true, errorMessage = null)
        return runCatching { action() }
            .fold(
                onSuccess = { refresh() },
                onFailure = { throwable ->
                    state = state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Не удалось изменить счет"
                    )
                    FinanceDashboardEvent.None
                }
            )
    }

    private fun failAccountAction(message: String): FinanceDashboardEvent {
        state = state.copy(isLoading = false, errorMessage = message)
        return FinanceDashboardEvent.None
    }
}

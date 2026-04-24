package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.repository.FinanceOverviewRepository

class FinanceDashboardController(
    repository: FinanceOverviewRepository
) {
    private val loader = FinanceOverviewLoader(repository)

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
}

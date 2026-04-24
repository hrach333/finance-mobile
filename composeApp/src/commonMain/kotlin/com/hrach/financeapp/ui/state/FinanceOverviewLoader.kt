package com.hrach.financeapp.ui.state

import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.repository.FinanceOverviewRepository

class FinanceOverviewLoader(
    private val repository: FinanceOverviewRepository
) {
    suspend fun load(): FinanceOverviewLoadResult {
        return runCatching { repository.getOverview() }
            .fold(
                onSuccess = { overview -> FinanceOverviewLoadResult.Success(overview) },
                onFailure = { throwable ->
                    val message = throwable.message ?: "Не удалось загрузить данные"
                    if (message.isAuthFailureMessage()) {
                        FinanceOverviewLoadResult.AuthExpired
                    } else {
                        FinanceOverviewLoadResult.Failure(message)
                    }
                }
            )
    }
}

sealed interface FinanceOverviewLoadResult {
    data class Success(val overview: FinanceOverview) : FinanceOverviewLoadResult
    data object AuthExpired : FinanceOverviewLoadResult
    data class Failure(val message: String) : FinanceOverviewLoadResult
}

private fun String.isAuthFailureMessage(): Boolean {
    return contains("401", ignoreCase = true) ||
        contains("unauthorized", ignoreCase = true) ||
        contains("unauthenticated", ignoreCase = true)
}

package com.hrach.financeapp.data.repository

import com.hrach.financeapp.data.model.FinanceOverview

interface FinanceOverviewRepository {
    fun getOverview(): FinanceOverview
}

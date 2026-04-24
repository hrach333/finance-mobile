package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SummaryDto(
    val income: Double,
    val expense: Double,
    val balance: Double
)

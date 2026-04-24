package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiListResponse<T>(
    val data: List<T>
)

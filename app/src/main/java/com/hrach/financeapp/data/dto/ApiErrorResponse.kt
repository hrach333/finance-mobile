package com.hrach.financeapp.data.dto

data class ApiErrorResponse(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null
)

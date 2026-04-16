package com.hrach.financeapp.data.dto

data class ApiErrorResponse(
    val message: String? = null,
    val errors: Any? = null  // Может быть Map<String, List<String>> или List<String>
)

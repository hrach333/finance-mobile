package com.hrach.financeapp.data.dto

data class ApiErrorResponse(
    val message: String? = null,
    val errors: Any? = null  // РњРѕР¶РµС‚ Р±С‹С‚СЊ Map<String, List<String>> РёР»Рё List<String>
)


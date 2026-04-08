package com.hrach.financeapp.data.dto

data class UpdateCategoryRequest(
    val name: String,
    val type: String,
    val iconKey: String? = null
)

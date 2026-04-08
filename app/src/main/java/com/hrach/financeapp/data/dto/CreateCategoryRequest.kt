package com.hrach.financeapp.data.dto

data class CreateCategoryRequest(
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String? = null
)

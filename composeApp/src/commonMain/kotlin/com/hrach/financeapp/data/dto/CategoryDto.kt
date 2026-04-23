package com.hrach.financeapp.data.dto

data class CategoryDto(
    val id: Int,
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String? = null
)


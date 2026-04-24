package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String? = null
)

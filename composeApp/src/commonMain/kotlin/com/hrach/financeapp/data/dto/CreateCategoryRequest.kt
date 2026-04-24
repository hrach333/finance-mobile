package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val groupId: Int,
    val type: String,
    val name: String,
    val iconKey: String? = null
)

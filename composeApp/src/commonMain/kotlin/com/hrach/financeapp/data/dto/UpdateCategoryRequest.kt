package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val type: String,
    val iconKey: String? = null
)

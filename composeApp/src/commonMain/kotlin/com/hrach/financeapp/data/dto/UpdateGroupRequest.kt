package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupRequest(
    val name: String,
    val baseCurrency: String
)

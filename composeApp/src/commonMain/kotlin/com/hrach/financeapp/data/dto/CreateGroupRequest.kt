package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val baseCurrency: String
)

package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val id: Int,
    val name: String,
    val baseCurrency: String
)

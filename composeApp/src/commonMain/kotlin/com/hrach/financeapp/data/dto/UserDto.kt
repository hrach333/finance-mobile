package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val name: String?,
    val email: String
)

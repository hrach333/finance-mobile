package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupMemberDto(
    val id: Int,
    val userId: Int? = null,
    val role: String,
    val user: UserDto? = null
)

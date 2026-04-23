package com.hrach.financeapp.data.dto

data class GroupMemberDto(
    val id: Int,
    val userId: Int? = null,
    val role: String,
    val user: UserDto? = null
)


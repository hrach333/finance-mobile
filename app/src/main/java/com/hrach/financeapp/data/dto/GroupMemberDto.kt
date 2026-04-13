package com.hrach.financeapp.data.dto

import com.google.gson.annotations.SerializedName

data class GroupMemberDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int? = null,
    val role: String,
    val user: UserDto? = null
)

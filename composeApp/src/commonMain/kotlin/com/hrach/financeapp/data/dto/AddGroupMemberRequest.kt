package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddGroupMemberRequest(
    val email: String,
    val role: String
)

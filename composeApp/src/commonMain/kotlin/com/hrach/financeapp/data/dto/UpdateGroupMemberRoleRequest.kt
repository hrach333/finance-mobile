package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateGroupMemberRoleRequest(
    val role: String
)

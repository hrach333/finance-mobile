package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String = ""
)

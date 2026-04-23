package com.hrach.financeapp.data.dto

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)


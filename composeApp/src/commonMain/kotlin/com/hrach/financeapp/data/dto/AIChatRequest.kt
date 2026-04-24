package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class AIChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: ChatMessage
)

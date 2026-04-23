package com.hrach.financeapp.data.dto

data class AIChatRequest(
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class AIChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: ChatMessage
)


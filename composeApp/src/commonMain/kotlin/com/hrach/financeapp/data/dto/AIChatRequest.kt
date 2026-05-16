package com.hrach.financeapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
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
    val message: ChatMessage? = null,
    val response: String? = null,
    val error: String? = null,
    val choices: List<Choice> = emptyList()
) {
    fun contentOrNull(): String? =
        message?.content
            ?: response
            ?: choices.firstOrNull()?.message?.content

    fun debugSummary(): String =
        error
            ?: message?.content?.takeIf { it.isNotBlank() }
            ?: response?.takeIf { it.isNotBlank() }
            ?: choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
            ?: toString()
}

@Serializable
data class Choice(
    val message: ChatMessage
)

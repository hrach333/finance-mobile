package com.hrach.financeapp.data.network

import com.hrach.financeapp.data.dto.AIChatRequest
import com.hrach.financeapp.data.dto.AIChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AIService {
    @POST("/v1/chat/completions")
    suspend fun getChatCompletion(@Body request: AIChatRequest): AIChatResponse
}

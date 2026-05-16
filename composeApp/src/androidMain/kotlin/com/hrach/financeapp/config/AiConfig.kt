package com.hrach.financeapp.config

import com.hrach.financeapp.BuildConfig

actual object AiConfig {
    actual val ollamaModel: String
        get() = BuildConfig.OLLAMA_MODEL.ifBlank { "llama3.1" }
}

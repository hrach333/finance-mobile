package com.hrach.financeapp.config

actual object AiConfig {
    actual val ollamaModel: String
        get() = System.getProperty("OLLAMA_MODEL")?.takeIf { it.isNotBlank() } ?: "llama3.1"
}

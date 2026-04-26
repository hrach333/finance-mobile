package com.hrach.financeapp.data.auth

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class DesktopSessionStore(
    private val sessionFile: Path = Path.of(
        System.getProperty("user.home"),
        ".smartbudget",
        "session.properties"
    )
) : SessionStore {
    override suspend fun getToken(): String? {
        return readProperties().getProperty(KEY_TOKEN)?.takeIf { it.isNotBlank() }
    }

    override suspend fun saveToken(token: String) {
        writeProperties(
            readProperties().apply {
                setProperty(KEY_TOKEN, token)
            }
        )
    }

    override suspend fun clearToken() {
        val properties = readProperties()
        properties.remove(KEY_TOKEN)
        if (properties.isEmpty) {
            Files.deleteIfExists(sessionFile)
        } else {
            writeProperties(properties)
        }
    }

    override suspend fun isOnboardingCompleted(): Boolean =
        readProperties().getProperty(KEY_ONBOARDING_COMPLETED)?.toBooleanStrictOrNull() == true

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        writeProperties(
            readProperties().apply {
                setProperty(KEY_ONBOARDING_COMPLETED, completed.toString())
            }
        )
    }

    private fun readProperties(): Properties {
        if (!sessionFile.exists()) return Properties()
        return Properties().apply {
            sessionFile.inputStream().use(::load)
        }
    }

    private fun writeProperties(properties: Properties) {
        sessionFile.parent.createDirectories()
        sessionFile.outputStream().use { output ->
            properties.store(output, "Умный бюджет session")
        }
    }

    private companion object {
        const val KEY_TOKEN = "auth_token"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}

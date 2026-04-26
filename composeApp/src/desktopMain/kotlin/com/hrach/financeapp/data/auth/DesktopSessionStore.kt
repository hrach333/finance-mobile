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
        if (!sessionFile.exists()) return null
        return Properties().apply {
            sessionFile.inputStream().use(::load)
        }.getProperty(KEY_TOKEN)?.takeIf { it.isNotBlank() }
    }

    override suspend fun saveToken(token: String) {
        sessionFile.parent.createDirectories()
        Properties().apply {
            setProperty(KEY_TOKEN, token)
            sessionFile.outputStream().use { output ->
                store(output, "Умный бюджет session")
            }
        }
    }

    override suspend fun clearToken() {
        Files.deleteIfExists(sessionFile)
    }

    private companion object {
        const val KEY_TOKEN = "auth_token"
    }
}

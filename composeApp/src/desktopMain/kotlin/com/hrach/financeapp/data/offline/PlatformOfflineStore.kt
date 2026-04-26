package com.hrach.financeapp.data.offline

import java.io.File

actual class PlatformOfflineStore actual constructor() {
    private val file = File(System.getProperty("user.home"), ".smartbudget/offline-finance.json")

    actual fun read(): String? {
        return if (file.exists()) file.readText() else null
    }

    actual fun write(value: String) {
        file.parentFile?.mkdirs()
        file.writeText(value)
    }
}

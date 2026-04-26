package com.hrach.financeapp.data.offline

import platform.Foundation.NSUserDefaults

actual class PlatformOfflineStore actual constructor() {
    actual fun read(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(KEY)
    }

    actual fun write(value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, KEY)
    }

    private companion object {
        const val KEY = "offline_finance_database"
    }
}

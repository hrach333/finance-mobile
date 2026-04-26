package com.hrach.financeapp.data.offline

import android.content.Context
import com.hrach.financeapp.mvp.MainActivity

actual class PlatformOfflineStore actual constructor() {
    actual fun read(): String? {
        return appContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE).getString(KEY, null)
    }

    actual fun write(value: String) {
        appContext.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, value)
            .apply()
    }

    private companion object {
        const val STORE_NAME = "offline_finance_store"
        const val KEY = "database"

        val appContext: Context
            get() = MainActivity.applicationContextProvider()
    }
}

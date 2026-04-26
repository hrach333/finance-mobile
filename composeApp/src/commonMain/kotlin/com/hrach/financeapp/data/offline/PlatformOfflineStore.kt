package com.hrach.financeapp.data.offline

expect class PlatformOfflineStore() {
    fun read(): String?
    fun write(value: String)
}

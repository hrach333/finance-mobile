package com.hrach.financeapp.data.offline

import android.util.Log
import com.hrach.financeapp.data.db.entity.PendingOperationEntity

/**
 * Логгер для OfflineManager с набором полезных функций отладки
 */
class OfflineLogger(private val tag: String = "OfflineSync") {
    
    fun logOperationSaved(operationId: Long, localId: String, operationType: String, groupId: String?) {
        Log.i(tag, """
            ✓ Operation Saved
            | ID: $operationId
            | LocalID: $localId
            | Type: $operationType
            | Group: $groupId
        """.trimMargin())
    }
    
    fun logSyncStarted(operationCount: Int) {
        Log.i(tag, """
            🔄 Sync Started
            | Operations to sync: $operationCount
        """.trimMargin())
    }
    
    fun logSyncSuccess(operationId: Long, attempts: Int) {
        Log.i(tag, """
            ✅ Sync Success
            | Operation ID: $operationId
            | Attempts: $attempts
        """.trimMargin())
    }
    
    fun logSyncFailed(operationId: Long, error: String, retryCount: Int, maxRetries: Int) {
        if (retryCount >= maxRetries) {
            Log.e(tag, """
                ❌ Sync Failed (Max retries exceeded)
                | Operation ID: $operationId
                | Error: $error
                | Retries: $retryCount/$maxRetries
            """.trimMargin())
        } else {
            Log.w(tag, """
                ⚠️ Sync Failed (Will retry)
                | Operation ID: $operationId
                | Error: $error
                | Retries: $retryCount/$maxRetries
            """.trimMargin())
        }
    }
    
    fun logNetworkStateChanged(isOnline: Boolean) {
        Log.i(tag, """
            🌐 Network State Changed
            | Status: ${if (isOnline) "ONLINE ✓" else "OFFLINE ✗"}
        """.trimMargin())
    }
    
    fun logOfflineModeActive(pendingCount: Int) {
        Log.i(tag, """
            📱 Offline Mode Active
            | Pending operations: $pendingCount
            | Data will be synced when connection is restored
        """.trimMargin())
    }
    
    fun logSyncInterval(intervalMs: Int) {
        Log.d(tag, "Minimum sync interval: ${intervalMs}ms")
    }
    
    fun logOperationDetails(operation: PendingOperationEntity) {
        Log.d(tag, """
            📋 Operation Details
            | ID: ${operation.id}
            | LocalID: ${operation.localId}
            | Type: ${operation.operationType}
            | Status: ${operation.status}
            | Created: ${operation.createdAt}
            | Retries: ${operation.retryCount}/${operation.maxRetries}
            | Error: ${operation.errorMessage ?: "None"}
            | Last Sync: ${operation.lastSyncAttempt}
        """.trimMargin())
    }
    
    fun logCleanup(deletedCount: Int) {
        Log.i(tag, "Cleanup: Removed $deletedCount old synced operations")
    }
}

/**
 * Extension функция для логирования операции
 */
fun PendingOperationEntity.logDetails(logger: OfflineLogger = OfflineLogger()) {
    logger.logOperationDetails(this)
}

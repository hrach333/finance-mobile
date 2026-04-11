package com.hrach.financeapp.data.db

import android.content.Context
import android.util.Log
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import java.time.LocalDateTime

/**
 * Helper класс для управления offline базой данных
 * Помогает в инициализации, миграции и отладке
 */
class OfflineDatabaseHelper(private val context: Context) {
    companion object {
        private const val TAG = "OfflineDatabaseHelper"
    }
    
    private val database by lazy { FinanceDatabase.getInstance(context) }
    
    /**
     * Получить статистику офлайн операций
     */
    suspend fun getStatistics(): DatabaseStats {
        val dao = database.pendingOperationDao()
        val allOperations = dao.getAll()
        
        val stats = DatabaseStats(
            totalOperations = allOperations.size,
            pendingCount = allOperations.count { it.status == PendingOperationEntity.STATUS_PENDING },
            syncingCount = allOperations.count { it.status == PendingOperationEntity.STATUS_SYNCING },
            failedCount = allOperations.count { it.status == PendingOperationEntity.STATUS_FAILED },
            syncedCount = allOperations.count { it.status == PendingOperationEntity.STATUS_SYNCED },
            oldestOperation = allOperations.minByOrNull { it.createdAt }?.createdAt,
            newestOperation = allOperations.maxByOrNull { it.createdAt }?.createdAt
        )
        
        Log.d(TAG, "DB Stats: $stats")
        return stats
    }
    
    /**
     * Очистить все операции из БД (для тестирования)
     */
    suspend fun clearAllOperations() {
        val dao = database.pendingOperationDao()
        val allOps = dao.getAll()
        dao.deleteByIds(allOps.map { it.id })
        Log.d(TAG, "Все операции удалены из БД")
    }
    
    /**
     * Очистить только failed операции
     */
    suspend fun clearFailedOperations() {
        val dao = database.pendingOperationDao()
        val failedOps = dao.getOperationsByStatus(PendingOperationEntity.STATUS_FAILED)
        dao.deleteByIds(failedOps.map { it.id })
        Log.d(TAG, "Удалены ${failedOps.size} неудачных операций")
    }
    
    /**
     * Создать тестовую операцию (для отладки)
     */
    suspend fun createTestOperation(
        operationType: String = PendingOperationEntity.TYPE_CREATE_TRANSACTION,
        status: String = PendingOperationEntity.STATUS_PENDING,
        groupId: String = "1"
    ): Long {
        val dao = database.pendingOperationDao()
        val operation = PendingOperationEntity(
            operationType = operationType,
            localId = "test_${System.currentTimeMillis()}",
            jsonData = """{"test": true}""",
            status = status,
            groupId = groupId,
            createdAt = LocalDateTime.now()
        )
        val id = dao.insert(operation)
        Log.d(TAG, "Создана тестовая операция: $id ($operationType, $status)")
        return id
    }
    
    /**
     * Получить все операции
     */
    suspend fun getAllOperations() = database.pendingOperationDao().getAll()
    
    /**
     * Получить операции по статусу
     */
    suspend fun getOperationsByStatus(status: String) = 
        database.pendingOperationDao().getOperationsByStatus(status)
    
    /**
     * Удалить операцию по ID
     */
    suspend fun deleteOperation(id: Long) {
        database.pendingOperationDao().getById(id)?.let { operation ->
            database.pendingOperationDao().delete(operation)
            Log.d(TAG, "Операция $id удалена")
        }
    }
    
    /**
     * Переместить операцию обратно в PENDING для повтора
     */
    suspend fun resetOperationForRetry(id: Long) {
        database.pendingOperationDao().getById(id)?.let { operation ->
            database.pendingOperationDao().update(
                operation.copy(
                    status = PendingOperationEntity.STATUS_PENDING,
                    retryCount = 0,
                    errorMessage = null,
                    lastSyncAttempt = null
                )
            )
            Log.d(TAG, "Операция $id переместена в PENDING для повтора")
        }
    }
}

/**
 * Data class для статистики БД
 */
data class DatabaseStats(
    val totalOperations: Int,
    val pendingCount: Int,
    val syncingCount: Int,
    val failedCount: Int,
    val syncedCount: Int,
    val oldestOperation: LocalDateTime?,
    val newestOperation: LocalDateTime?
) {
    override fun toString(): String {
        return """
            |=== Offline Database Statistics ===
            |Total Operations: $totalOperations
            |  - Pending: $pendingCount
            |  - Syncing: $syncingCount
            |  - Failed: $failedCount
            |  - Synced: $syncedCount
            |Oldest: $oldestOperation
            |Newest: $newestOperation
        """.trimMargin()
    }
}

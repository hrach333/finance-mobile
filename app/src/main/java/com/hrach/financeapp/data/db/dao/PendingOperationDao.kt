package com.hrach.financeapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    
    /**
     * Вставить новую операцию для синхронизации
     */
    @Insert
    suspend fun insert(operation: PendingOperationEntity): Long
    
    /**
     * Обновить статус или информацию об операции
     */
    @Update
    suspend fun update(operation: PendingOperationEntity)
    
    /**
     * Удалить операцию
     */
    @Delete
    suspend fun delete(operation: PendingOperationEntity)
    
    /**
     * Получить все ожидающие операции (статус PENDING)
     */
    @Query("SELECT * FROM pending_operations WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getOperationsByStatus(status: String): List<PendingOperationEntity>
    
    /**
     * Получить все операции для конкретной группы
     */
    @Query("SELECT * FROM pending_operations WHERE groupId = :groupId AND status != :excludeStatus ORDER BY createdAt ASC")
    suspend fun getGroupOperations(groupId: String, excludeStatus: String = "SYNCED"): List<PendingOperationEntity>
    
    /**
     * Получить операцию по ID
     */
    @Query("SELECT * FROM pending_operations WHERE id = :id")
    suspend fun getById(id: Long): PendingOperationEntity?
    
    /**
     * Получить операцию по локальному ID
     */
    @Query("SELECT * FROM pending_operations WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: String): PendingOperationEntity?
    
    /**
     * Получить все ожидающие операции в Real-time (как Flow)
     */
    @Query("SELECT * FROM pending_operations WHERE status = :status ORDER BY createdAt ASC")
    fun observeOperationsByStatus(status: String): Flow<List<PendingOperationEntity>>
    
    /**
     * Получить количество ожидающих операций
     */
    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = :status")
    fun countByStatus(status: String): Flow<Int>
    
    /**
     * Получить операции, которые нужно повторить (не удались или ждут повтора)
     */
    @Query("SELECT * FROM pending_operations WHERE status IN ('PENDING', 'FAILED') AND retryCount < maxRetries ORDER BY lastSyncAttempt ASC LIMIT :limit")
    suspend fun getPendingForSync(limit: Int = 10): List<PendingOperationEntity>
    
    /**
     * Удалить все синхронизированные операции (очистка)
     */
    @Query("DELETE FROM pending_operations WHERE status = :status AND createdAt < datetime('now', '-7 days')")
    suspend fun deleteOldSyncedOperations(status: String = "SYNCED")
    
    /**
     * Удалить завершённые операции
     */
    @Query("DELETE FROM pending_operations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
    
    /**
     * Получить все операции
     */
    @Query("SELECT * FROM pending_operations ORDER BY createdAt DESC")
    suspend fun getAll(): List<PendingOperationEntity>
}

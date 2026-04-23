package com.hrach.financeapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity для хранения операций, которые ожидают синхронизации с сервером
 * Используется при офлайн режиме для сохранения данных локально
 */
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Тип операции: CREATE_TRANSACTION, UPDATE_TRANSACTION, DELETE_TRANSACTION, CREATE_ACCOUNT, etc.
    val operationType: String,
    
    // ID ресурса на сервере (если обновление/удаление)
    val remoteId: String? = null,
    
    // Локальный временный ID для отслеживания
    val localId: String,
    
    // JSON данные операции
    val jsonData: String,
    
    // Статус: PENDING, SYNCING, FAILED, SYNCED
    val status: String = "PENDING",
    
    // Количество попыток синхронизации
    val retryCount: Int = 0,
    
    // Максимальное количество попыток
    val maxRetries: Int = 3,
    
    // Сообщение об ошибке при последней попытке
    val errorMessage: String? = null,
    
    // Когда была создана операция
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    // Когда была последняя попытка синхронизации
    val lastSyncAttempt: LocalDateTime? = null,
    
    // Группа, к которой относится операция (для фильтрации)
    val groupId: String? = null
) {
    companion object {
        const val TYPE_CREATE_TRANSACTION = "CREATE_TRANSACTION"
        const val TYPE_UPDATE_TRANSACTION = "UPDATE_TRANSACTION"
        const val TYPE_DELETE_TRANSACTION = "DELETE_TRANSACTION"
        const val TYPE_CREATE_ACCOUNT = "CREATE_ACCOUNT"
        const val TYPE_UPDATE_ACCOUNT = "UPDATE_ACCOUNT"
        const val TYPE_DELETE_ACCOUNT = "DELETE_ACCOUNT"
        const val TYPE_CREATE_CATEGORY = "CREATE_CATEGORY"
        const val TYPE_UPDATE_CATEGORY = "UPDATE_CATEGORY"
        const val TYPE_DELETE_CATEGORY = "DELETE_CATEGORY"
        const val TYPE_ADD_MEMBER = "ADD_MEMBER"
        const val TYPE_REMOVE_MEMBER = "REMOVE_MEMBER"
        
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SYNCING = "SYNCING"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_SYNCED = "SYNCED"
    }
}

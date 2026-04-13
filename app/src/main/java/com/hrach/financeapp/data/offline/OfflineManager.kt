package com.hrach.financeapp.data.offline

import android.util.Log
import com.google.gson.Gson
import com.hrach.financeapp.data.api.FinanceApi
import com.hrach.financeapp.data.db.dao.PendingOperationDao
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.UpdateTransactionRequest
import com.hrach.financeapp.data.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Manager для управления офлайн операциями и синхронизацией
 */
class OfflineManager(
    private val dao: PendingOperationDao,
    private val api: FinanceApi,
    private val networkMonitor: NetworkMonitor,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "OfflineManager"
        private const val MIN_SYNC_INTERVAL_MS = 5000 // Минимум 5 сек между попытками синха
    }
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    val pendingOperations: Flow<List<PendingOperationEntity>> = dao.observeOperationsByStatus(PendingOperationEntity.STATUS_PENDING)
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<LocalDateTime?>(null)
    val lastSyncTime: StateFlow<LocalDateTime?> = _lastSyncTime.asStateFlow()
    
    private var lastSyncAttempt = 0L
    private val scope = CoroutineScope(Dispatchers.IO)
    
    init {
        // Наблюдаем за состоянием сети и запускаем синхронизацию при восстановлении
        scope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    Log.d(TAG, "Интернет восстановлен, начинаем синхронизацию")
                    syncPendingOperations()
                } else {
                    Log.d(TAG, "Интернет отключен, переходим в офлайн режим")
                }
            }
        }
        
        // Отслеживаем количество ожидающих операций
        scope.launch {
            dao.countByStatus(PendingOperationEntity.STATUS_PENDING).collect { count ->
                _pendingCount.value = count
            }
        }
    }
    
    /**
     * Сохранить операцию для позже синхронизации
     */
    suspend fun savePendingOperation(
        operationType: String,
        jsonData: String,
        groupId: String? = null,
        remoteId: String? = null
    ): Long {
        val localId = "local_${System.currentTimeMillis()}_${(Math.random() * 10000).toInt()}"
        
        val operation = PendingOperationEntity(
            operationType = operationType,
            localId = localId,
            jsonData = jsonData,
            groupId = groupId,
            remoteId = remoteId,
            status = PendingOperationEntity.STATUS_PENDING
        )
        
        val id = dao.insert(operation)
        Log.d(TAG, "✅ Операция сохранена в БД: ID=$id, Type=$operationType, LocalId=$localId, GroupId=$groupId")
        Log.d(TAG, "📋 Данные: $jsonData")
        return id
    }
    
    /**
     * Получить локальный ID операции по БД ID
     */
    suspend fun getLocalId(dbId: Long): String? {
        return dao.getById(dbId)?.localId
    }
    
    /**
     * Получить операцию по локальному ID
     */
    suspend fun getOperationByLocalId(localId: String): PendingOperationEntity? {
        return dao.getByLocalId(localId)
    }
    
    /**
     * Синхронизировать все ожидающие операции
     */
    suspend fun syncPendingOperations() {
        // Избегаем слишком частых попыток синхронизации
        val now = System.currentTimeMillis()
        if (now - lastSyncAttempt < MIN_SYNC_INTERVAL_MS) {
            Log.d(TAG, "Слишком рано для синхронизации, ждем...")
            return
        }
        
        if (!networkMonitor.isConnected()) {
            Log.d(TAG, "Нет интернета, не можем синхронизироваться")
            return
        }
        
        if (_isSyncing.value) {
            Log.d(TAG, "Синхронизация уже в процессе")
            return
        }
        
        _isSyncing.value = true
        lastSyncAttempt = now
        
        try {
            val pendingOps = dao.getPendingForSync(limit = 10)
            Log.d(TAG, "Начинаем синхронизацию ${pendingOps.size} операций")
            
            for (operation in pendingOps) {
                syncOperation(operation)
            }
            
            _lastSyncTime.value = LocalDateTime.now()
            _syncError.value = null
            Log.d(TAG, "Синхронизация завершена успешно")
            
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val errorMsg = "Ошибка синхронизации: ${e.message}"
            _syncError.value = errorMsg
            Log.e(TAG, errorMsg, e)
        } finally {
            _isSyncing.value = false
        }
    }
    
    /**
     * Синхронизировать одну операцию
     */
    private suspend fun syncOperation(operation: PendingOperationEntity) {
        try {
            // Обновляем статус на SYNCING
            dao.update(operation.copy(status = PendingOperationEntity.STATUS_SYNCING))
            
            when (operation.operationType) {
                PendingOperationEntity.TYPE_CREATE_TRANSACTION -> {
                    val request = gson.fromJson(operation.jsonData, CreateTransactionRequest::class.java)
                    api.createTransaction(request)
                    markAsSynced(operation)
                }
                
                PendingOperationEntity.TYPE_UPDATE_TRANSACTION -> {
                    val request = gson.fromJson(operation.jsonData, UpdateTransactionRequest::class.java)
                    val id = operation.remoteId?.toInt() ?: return markAsFailed(operation, "Нет ID транзакции")
                    api.updateTransaction(id, request)
                    markAsSynced(operation)
                }
                
                PendingOperationEntity.TYPE_DELETE_TRANSACTION -> {
                    val id = operation.remoteId?.toInt() ?: return markAsFailed(operation, "Нет ID транзакции")
                    api.deleteTransaction(id)
                    markAsSynced(operation)
                }
                
                // Добавьте другие типы операций по необходимости
                else -> {
                    Log.w(TAG, "Неизвестный тип операции: ${operation.operationType}")
                    markAsFailed(operation, "Неизвестный тип")
                }
            }
            
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Неизвестная ошибка"
            Log.e(TAG, "Ошибка синхронизации операции ${operation.id}: $errorMsg", e)
            markAsFailed(operation, errorMsg)
        }
    }
    
    /**
     * Отметить операцию как синхронизированную
     */
    private suspend fun markAsSynced(operation: PendingOperationEntity) {
        dao.update(
            operation.copy(
                status = PendingOperationEntity.STATUS_SYNCED,
                lastSyncAttempt = LocalDateTime.now()
            )
        )
        Log.d(TAG, "Операция ${operation.id} успешно синхронизирована")
    }
    
    /**
     * Отметить операцию как неудачную
     */
    private suspend fun markAsFailed(operation: PendingOperationEntity, errorMsg: String) {
        val newRetryCount = operation.retryCount + 1
        val newStatus = if (newRetryCount >= operation.maxRetries) {
            PendingOperationEntity.STATUS_FAILED
        } else {
            PendingOperationEntity.STATUS_PENDING
        }
        
        dao.update(
            operation.copy(
                status = newStatus,
                retryCount = newRetryCount,
                errorMessage = errorMsg,
                lastSyncAttempt = LocalDateTime.now()
            )
        )
        
        Log.e(TAG, "Операция ${operation.id} не синхронизирована: $errorMsg (попыток: $newRetryCount/${operation.maxRetries})")
    }
    
    /**
     * Получить все ожидающие операции
     */
    suspend fun getPendingOperations(): List<PendingOperationEntity> {
        return dao.getOperationsByStatus(PendingOperationEntity.STATUS_PENDING)
    }
    
    /**
     * Очистить старые синхронизированные операции
     */
    suspend fun cleanupOldOperations() {
        dao.deleteOldSyncedOperations()
        Log.d(TAG, "Старые синхронизированные операции удалены")
    }
    
    /**
     * Удалить конкретную операцию
     */
    suspend fun deleteOperation(operationId: Long) {
        dao.getById(operationId)?.let { operation ->
            dao.delete(operation)
            Log.d(TAG, "Операция $operationId удалена")
        }
    }
    
    /**
     * Повторить неудачную операцию
     */
    suspend fun retryFailedOperation(operationId: Long) {
        dao.getById(operationId)?.let { operation ->
            if (operation.status == PendingOperationEntity.STATUS_FAILED) {
                dao.update(
                    operation.copy(
                        status = PendingOperationEntity.STATUS_PENDING,
                        retryCount = 0,
                        errorMessage = null
                    )
                )
                Log.d(TAG, "Операция $operationId отмечена для повтора")
            }
        }
    }
}

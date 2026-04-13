package com.hrach.financeapp.data.offline

/**
 * ПРИМЕРЫ ИСПОЛЬЗОВАНИЯ OFFLINE MANAGER
 * 
 * 1. ИНИЦИАЛИЗАЦИЯ В MAIN ACTIVITY (уже сделано в MainActivity.kt)
 *    - Создается FinanceDatabase
 *    - Создается NetworkMonitor
 *    - Создается OfflineManager
 *    - Передается в FinanceRepository
 * 
 * 2. АВТОМАТИЧЕСКОЕ СОХРАНЕНИЕ ПРИ ОШИБКАХ СЕТИ
 * 
 *    // В Repository методы автоматически сохраняют операции при разрыве сети:
 *    suspend fun createTransaction(request: CreateTransactionRequest) = try {
 *        api.createTransaction(request)  // Пытаемся отправить на сервер
 *    } catch (e: Exception) {
 *        // Если нет сети - операция автоматически сохраняется локально
 *        if (networkMonitor?.isConnected() == false && offlineManager != null) {
 *            offlineManager.savePendingOperation(...)
 *        } else {
 *            throw e
 *        }
 *    }
 * 
 * 3. ОТСЛЕЖИВАНИЕ СТАТУСА СИНХРОНИЗАЦИИ В VIEWMODEL
 * 
 *    // Получаем StateFflow для мониторинга
 *    val isSyncing: StateFlow<Boolean> = offlineManager.isSyncing
 *    val pendingCount: StateFlow<Int> = offlineManager.pendingCount
 *    val syncError: StateFlow<String?> = offlineManager.syncError
 *    
 *    // Используем в Composable
 *    val isSyncing by offlineManager.isSyncing.collectAsState()
 *    val pendingCount by offlineManager.pendingCount.collectAsState()
 * 
 * 4. ИСПОЛЬЗОВАНИЕ В HOMEVIEWMODEL
 * 
 *    class HomeViewModel(
 *        private val repository: FinanceRepository,
 *        private val offlineManager: OfflineManager? = null
 *    ) : ViewModel() {
 *        val isSyncing = offlineManager?.isSyncing ?: MutableStateFlow(false).asStateFlow()
 *        val pendingCount = offlineManager?.pendingCount ?: MutableStateFlow(0).asStateFlow()
 *        val syncError = offlineManager?.syncError ?: MutableStateFlow(null).asStateFlow()
 *        
 *        fun manualSync() {
 *            viewModelScope.launch {
 *                offlineManager?.syncPendingOperations()
 *            }
 *        }
 *    }
 * 
 * 5. ОБРАБОТКА ОШИБОК И ПОВТОРЫ
 * 
 *    // OfflineManager автоматически:
 *    // - Повторяет неудачные операции (максимум 3 раза)
 *    // - Сохраняет ошибки
 *    // - Позволяет вручную повторить попытку
 *    
 *    suspend fun retryFailedOperation(operationId: Long) {
 *        offlineManager?.retryFailedOperation(operationId)
 *    }
 * 
 * 6. СИНХРОНИЗАЦИЯ АВТОМАТИЧЕСКИ ЗАПУСКАЕТСЯ:
 * 
 *    - При восстановлении интернета (NetworkMonitor отслеживает изменения)
 *    - Вручную через offlineManager.syncPendingOperations()
 *    - С минимальным интервалом 5 секунд между попытками
 * 
 * 7. ОЧИСТКА СТАРЫХ ОПЕРАЦИЙ
 * 
 *    // Периодически вызывать для удаления старых синхронизированных операций
 *    suspend fun cleanupOldOperations() {
 *        offlineManager?.cleanupOldOperations()
 *    }
 * 
 * 8. СТРУКТУРА ТАБЛИЦЫ pending_operations:
 * 
 *    - id: Long (Primary Key)
 *    - operationType: String (CREATE_TRANSACTION, UPDATE_TRANSACTION, DELETE_TRANSACTION, etc.)
 *    - remoteId: String? (ID на сервере для обновления/удаления)
 *    - localId: String (Уникальный локальный ID)
 *    - jsonData: String (Сохраненные данные в JSON формате)
 *    - status: String (PENDING, SYNCING, FAILED, SYNCED)
 *    - retryCount: Int (текущие попытки)
 *    - maxRetries: Int (максимум 3)
 *    - errorMessage: String? 
 *    - createdAt: LocalDateTime
 *    - lastSyncAttempt: LocalDateTime?
 *    - groupId: String?
 * 
 * 9. ТЕКУЩИЕ ПОДДЕРЖИВАЕМЫЕ ОПЕРАЦИИ:
 * 
 *    - CREATE_TRANSACTION, UPDATE_TRANSACTION, DELETE_TRANSACTION
 *    - CREATE_ACCOUNT, UPDATE_ACCOUNT, DELETE_ACCOUNT
 *    - CREATE_CATEGORY, UPDATE_CATEGORY, DELETE_CATEGORY
 *    - ADD_MEMBER, REMOVE_MEMBER
 * 
 *    Для других операций добавьте обработку в OfflineManager.syncOperation()
 */

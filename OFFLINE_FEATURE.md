# Офлайн функциональность приложения Finance Mobile

## Описание

Приложение теперь поддерживает работу в офлайн режиме. Все операции (создание/обновление/удаление транзакций, счетов, категорий) автоматически сохраняются локально в SQLite базу данных при отсутствии интернета. При восстановлении сетевого соединения операции автоматически синхронизируются с сервером.

## Компоненты

### 1. **NetworkMonitor** (`data/network/NetworkMonitor.kt`)
- Отслеживает состояние сетевого соединения
- Использует ConnectivityManager для мониторинга
- Предоставляет Flow<Boolean> для реактивного отслеживания подключения

### 2. **FinanceDatabase** (`data/db/FinanceDatabase.kt`)
- Room база данных для хранения офлайн операций
- Синглтон паттерн для доступа
- Содержит таблицу `pending_operations`

### 3. **PendingOperationEntity** (`data/db/entity/PendingOperationEntity.kt`)
- Сущность для хранения операций в очереди синхронизации
- Поля:
  - `operationType`: тип операции (CREATE_TRANSACTION, UPDATE_TRANSACTION, DELETE_TRANSACTION, и т.д.)
  - `remoteId`: ID на сервере
  - `localId`: локальный уникальный ID
  - `jsonData`: данные операции в JSON
  - `status`: PENDING, SYNCING, FAILED, SYNCED
  - `retryCount`: количество попыток синхронизации

### 4. **OfflineManager** (`data/offline/OfflineManager.kt`)
- Основной класс для управления офлайн операциями
- Функциональность:
  - Сохранение операций при ошибках сети
  - Автоматическая синхронизация при восстановлении интернета
  - Повторные попытки при ошибках (максимум 3 раза)
  - Отслеживание статуса синхронизации

### 5. **OfflineSyncStatus** UI компонент (`ui/components/OfflineSyncStatus.kt`)
- Отображает статус синхронизации в UI
- Показывает количество ожидающих операций
- Индикатор загрузки при синхронизации
- Сообщения об ошибках

## Как это работает

### Процесс сохранения операции

```
1. Пользователь создает/обновляет операцию
2. App пытается отправить на сервер
3. Если нет интернета → операция сохраняется в SQLite
4. Пользователь видит уведомление об офлайн режиме
5. При восстановлении сети → операция автоматически отправляется
```

### Жизненный цикл операции

```
PENDING → SYNCING → SYNCED ✓
        ↓
       FAILED (если 3 попытки не удались)
        ↓ (можно повторить вручную)
       PENDING → SYNCING → SYNCED ✓
```

## Поддерживаемые операции

- ✅ Создание/Обновление/Удаление транзакций
- ✅ Создание/Обновление/Удаление счетов
- ✅ Создание/Обновление/Удаление категорий
- ✅ Добавление/Удаление членов группы

Для добавления новых операций обновите:
1. `PendingOperationEntity` - добавьте константу типа операции
2. `OfflineManager.syncOperation()` - добавьте обработчик
3. `FinanceRepository` - оберните операцию в try-catch с сохранением

## Интеграция в ViewModel

```kotlin
class HomeViewModel(
    private val repository: FinanceRepository,
    private val offlineManager: OfflineManager? = null,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModel() {
    // Отслеживание статуса
    val isSyncing: StateFlow<Boolean> = offlineManager?.isSyncing ?: MutableStateFlow(false).asStateFlow()
    val pendingCount: StateFlow<Int> = offlineManager?.pendingCount ?: MutableStateFlow(0).asStateFlow()
    val syncError: StateFlow<String?> = offlineManager?.syncError ?: MutableStateFlow(null).asStateFlow()
    
    // Ручная синхронизация
    fun manualSync() {
        viewModelScope.launch {
            offlineManager?.syncPendingOperations()
        }
    }
}
```

## UI интеграция

```kotlin
// В HomeScreen
OfflineSyncStatus(
    isSyncing = isSyncing,
    pendingCount = pendingCount,
    syncError = syncError,
    isOnline = isOnline
)
```

## Разрешения Android

Добавлены в AndroidManifest.xml:
- `android.permission.INTERNET` - для API запросов
- `android.permission.ACCESS_NETWORK_STATE` - для отслеживания сети

## Конфигурация

### Минимальный интервал между синхронизациями
```kotlin
private const val MIN_SYNC_INTERVAL_MS = 5000 // 5 секунд
```

### Максимальное количество повторов
```kotlin
val maxRetries: Int = 3
```

### Удаление старых операций
Синхронизированные операции старше 7 дней удаляются автоматически:
```kotlin
suspend fun cleanupOldOperations() {
    dao.deleteOldSyncedOperations(status = "SYNCED")
}
```

## Обработка ошибок

В Repository каждый метод создания/обновления/удаления имеет структуру:

```kotlin
suspend fun createTransaction(request: CreateTransactionRequest) = try {
    api.createTransaction(request)  // Пытаемся отправить
} catch (e: Exception) {
    if (networkMonitor?.isConnected() == false && offlineManager != null) {
        // Нет сети - сохраняем локально
        offlineManager.savePendingOperation(...)
    } else {
        throw e  // Есть сеть, но другая ошибка
    }
}
```

## Логирование

Все действия логируются с тегом `OfflineManager`:
- Сохранение операций
- Попытки синхронизации
- Успешные синхронизации
- Ошибки при синхронизации

Просмотрите логи:
```bash
adb logcat | grep OfflineManager
```

## Тестирование

### Тест офлайн режима

1. **Отключите интернет**
   - Включите режим самолета или отключите WiFi в эмуляторе

2. **Создайте операцию**
   - Пользователь видит уведомление об офлайн режиме
   - Операция сохраняется локально

3. **Включите интернет обратно**
   - Операция автоматически синхронизируется
   - Уведомление исчезает

### Просмотр таблицы БД

```bash
adb shell
cd /data/data/com.hrach.financeapp/databases
sqlite3 finance_database

# Просмотр всех операций
SELECT * FROM pending_operations;

# Просмотр только ожидающих
SELECT * FROM pending_operations WHERE status = 'PENDING';

# Просмотр ошибок
SELECT * FROM pending_operations WHERE status = 'FAILED';
```

## Future improvements

- [ ] Синхронизация по расписанию через WorkManager
- [ ] UI для просмотра истории ошибок синхронизации
- [ ] Кэширование получаемых данных (фото, основные список)
- [ ] Сжатие данных перед синхронизацией
- [ ] Konflikt resolution (когда данные изменены с обоих сторон)
- [ ] Batch синхронизация для оптимизации трафика

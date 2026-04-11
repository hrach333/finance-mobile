## Быстрый старт - Офлайн функциональность

### 1️⃣ Что было сделано?

Ваше приложение теперь поддерживает работу без интернета:

✅ **Автоматическое сохранение** операций локально при отсутствии интернета
✅ **Автоматическая синхронизация** с сервером при восстановлении соединения  
✅ **SQLite база данных** для хранения ожидающих операций
✅ **Надежность** - операции повторяются до 3 раз при ошибках
✅ **UI индикатор** - пользователь видит статус синхронизации

### 2️⃣ Структура добавленных файлов

```
app/src/main/java/com/hrach/financeapp/
├── data/
│   ├── db/                          # Room Database
│   │   ├── FinanceDatabase.kt       # Main DB class
│   │   ├── OfflineDatabaseHelper.kt # Helper для отладки
│   │   ├── converter/
│   │   │   └── LocalDateTimeConverter.kt
│   │   ├── dao/
│   │   │   └── PendingOperationDao.kt
│   │   └── entity/
│   │       └── PendingOperationEntity.kt
│   ├── network/
│   │   └── NetworkMonitor.kt        # Отслеживание сети
│   └── offline/
│       ├── OfflineManager.kt        # Главный класс
│       ├── OfflineExtensions.kt     # Extension функции
│       ├── OfflineLogger.kt         # Логирование
│       └── OFFLINE_USAGE_EXAMPLES.kt # Примеры
└── ui/
    └── components/
        └── OfflineSyncStatus.kt     # UI компонент
```

### 3️⃣ Как это работает?

**Сценарий 1: Интернет есть**
```
Пользователь создает операцию
         ↓
API запрос на сервер
         ↓
Успех → Данные обновлены ✓
```

**Сценарий 2: Интернета нет**
```
Пользователь создает операцию
         ↓
API запрос → Ошибка "нет сети"
         ↓
Операция сохранена в SQLite
         ↓
Пользователь видит "Офлайн режим"
         ↓
Интернет вернулся → Автоматическая синхронизация ✓
```

### 4️⃣ Использование в коде

**В HomeViewModel уже добавлено:**

```kotlin
val isSyncing: StateFlow<Boolean>      // Идет ли синхронизация?
val pendingCount: StateFlow<Int>       // Сколько операций ждут?
val syncError: StateFlow<String?>      // Есть ли ошибки?

// Ручная синхронизация
fun manualSync() { ... }

// Повторить неудачную операцию
fun retryOfflineOperation(operationId: Long) { ... }
```

**В HomeScreen уже интегрировано:**

```kotlin
OfflineSyncStatus(
    isSyncing = isSyncing,
    pendingCount = pendingCount,
    syncError = syncError,
    isOnline = true
)
```

### 5️⃣ В Repository автоматически обработано

Все методы CRUD (Create, Read, Update, Delete) уже обрабатывают офлайн режим:

- ✅ `createTransaction()`
- ✅ `updateTransaction()`
- ✅ `deleteTransaction()`
- ✅ `createAccount()`
- ✅ `updateAccount()`
- ✅ `deleteAccount()`
- ✅ `createCategory()`
- ✅ `updateCategory()`
- ✅ `deleteCategory()`

### 6️⃣ Тестирование

**Эмулировать офлайн режим:**

1. Откройте эмулятор Android
2. Нажмите Ctrl+K (или используйте меню для отключения сети)
3. Выполните операцию в приложении
4. Увидите уведомление об офлайн режиме
5. Включите сеть обратно
6. Операция автоматически синхронизируется

**Просм. БД:**

```bash
adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database

# В SQLite:
.tables                              # Показать таблицы
SELECT * FROM pending_operations;   # Все операции
SELECT COUNT(*) FROM pending_operations WHERE status='PENDING';
.dump pending_operations            # Экспорт
```

### 7️⃣ Логирование

Все события логируются. Просмотрите в Android Studio:

```bash
adb logcat | grep "Offline"
```

Или используйте собственный OfflineLogger:

```kotlin
val logger = OfflineLogger()
logger.logOperationSaved(1, "local_123", "CREATE_TRANSACTION", "group_1")
logger.logNetworkStateChanged(false)
```

### 8️⃣ Расширение функциональности

**Добавить новый тип операции:**

1. Добавить константу в `PendingOperationEntity`:
```kotlin
const val TYPE_MY_OPERATION = "MY_OPERATION"
```

2. Добавить обработку в `OfflineManager.syncOperation()`:
```kotlin
PendingOperationEntity.TYPE_MY_OPERATION -> {
    val request = gson.fromJson(operation.jsonData, MyRequest::class.java)
    api.myOperation(request)
    markAsSynced(operation)
}
```

3. Обернуть в Repository:
```kotlin
suspend fun myOperation(request: MyRequest) = try {
    api.myOperation(request)
} catch (e: Exception) {
    if (networkMonitor?.isConnected() == false && offlineManager != null) {
        offlineManager.savePendingOperation(
            operationType = PendingOperationEntity.TYPE_MY_OPERATION,
            jsonData = gson.toJson(request)
        )
    } else {
        throw e
    }
}
```

### 9️⃣ Производительность

- Минимальный интервал между синхронизациями: **5 сек**
- Максимум операций за раз: **10**
- Максимум повторов: **3**
- Удаление старых синх-ых операций: **автоматически (старше 7 дней)**

### 🔟 Расширенные возможности

**OfflineDatabaseHelper** помогает в отладке:

```kotlin
val helper = OfflineDatabaseHelper(context)

// Статистика
val stats = helper.getStatistics()
println(stats)  // Красивый вывод

// Для тестирования
helper.createTestOperation()     // Создать тестовую операцию
helper.clearFailedOperations()   // Очистить ошибки
helper.clearAllOperations()      // Полная очистка
```

### ❓ FAQ

**Q: Что если пользователь переустановит приложение?**
A: Данные будут потеряны (они локальные). Это нормально для offline cache.

**Q: Что если конфликт: данные изменены с обеих сторон?**
A: Текущая версия перезаписывает. Для production нужна merge strategy.

**Q: Как отключить офлайн функциональность?**
A: Передайте `null` для offlineManager и networkMonitor в Repository.

**Q: Памяти хватит на все операции?**
A: SQLite эффективна, но для production добавьте пагинацию.

---

✨ **Готово!** Приложение теперь работает без интернета! ✨

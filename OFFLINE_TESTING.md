# 🧪 Инструкция по тестированию Offline функциональности

## ✅ Шаги для проверки

### 1. Включите режим самолета
```
Settings → Airplane Mode → ON
ИЛИ
Ctrl+K в эмуляторе (если эмулятор поддерживает)
```

### 2. Откройте приложение 
- Если приложение уже открыто без интернета - закройте и перезапустите
- Вы должны увидеть экран авторизации

### 3. Проверьте логи
Откройте Android Studio Logcat и ищите:
```
adb logcat | grep -E "FinanceRepository|OfflineManager|NetworkMonitor"
```

Вы должны видеть:
```
FinanceRepository - ❌ Нет интернета. 
OfflineManager - ✅ Операция сохранена в БД
```

### 4. Создайте операцию в офлайн режиме

**Вариант 1: Если вы раньше загружали данные с интернетом**
- Перейдите на HomeScreen 
- Перейдите в "Добавить транзакцию"
- Выберите счет и категорию
- Нажмите создать

**Вариант 2: Если нет кэшированных данных**
- Должны видеть пустой список счетов/категорий
- Это нормально - данные загружаются только с интернетом

### 5. Посмотрите BDD записную книжку
```bash
adb shell
cd /data/data/com.hrach.financeapp/databases

# Просмотрите сохраненные операции
sqlite3 finance_database
> SELECT * FROM pending_operations;
> SELECT COUNT(*) FROM pending_operations WHERE status='PENDING';
```

Вы должны видеть вашу операцию с статусом `PENDING`.

### 6. Включите интернет обратно
```
Settings → Airplane Mode → OFF
ИЛИ
Ctrl+K еще раз
```

### 7. Посмотрите автоматическую синхронизацию

В логах вы должны видеть:
```
OfflineManager - 🔄 Sync Started
OfflineManager - ✅ Sync Success
```

В БД статус операции должен измениться на `SYNCED`.

---

## 🔍 Проверка логов более подробно

### Смотрите все операции
```bash
adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database

# Все операции
SELECT id, operationType, status, createdAt, errorMessage FROM pending_operations;

# Неудачные операции
SELECT * FROM pending_operations WHERE status = 'FAILED';

# Статистика
SELECT COUNT(*) as total, status FROM pending_operations GROUP BY status;
```

---

## ❌ Если не работает

### 1. Проверьте что NetworkMonitor инициализирован
```
LogCat → ищите "NetworkMonitor - ONLINE" или "NetworkMonitor - OFFLINE"
```

### 2. Проверьте что OfflineManager создан
```
LogCat → ищите "OfflineManager" в тегах
```

### 3. Проверьте таблицу в БД существует
```bash
adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database
> .tables
# Должна быть таблица "pending_operations"
```

### 4. Проверьте что операция дошла до Repository
```
LogCat → ищите "FinanceRepository - createTransaction"
```

### 5. Включите все логи для DebugLogging
В файле build.gradle.kts убедитесь что включен:
```gradle
debugImplementation("androidx.compose.ui:ui-tooling")
```

---

## 📊 Ожидаемые результаты

✅ **Без интернета:**
- Операция должна сохраниться в БД
- Статус = `PENDING`
- Пользователь видит уведомление об офлайн режиме

⚡ **При восстановлении интернета:**
- Операция автоматически отправляется на сервер (через 5-10 сек)
- Статус меняется с `PENDING` → `SYNCING` → `SYNCED`
- Пользователь видит индикатор "Синхронизация..."

❌ **При ошибке:**
- Статус = `FAILED`
- `retryCount` увеличивается на 1
- `errorMessage` содержит текст ошибки
- Через 5 сек откроется новая попытка (макс 3 попытки)

---

## 🐛 Debug команды

### Очистить все операции
```kotlin
// В коде можно вызвать:
offlineManager?.clearAllOperations()  // Функция из OfflineManager
```

### Создать тестовую операцию
```bash
adb shell
sqlite3 /data/data/com.hrach.financeapp/databases/finance_database
INSERT INTO pending_operations (operationType, localId, jsonData, status, groupId) 
VALUES ('CREATE_TRANSACTION', 'test_123', '{"test":"data"}', 'PENDING', '1');
```

### Посмотреть структуру таблицы
```bash
adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database
PRAGMA table_info(pending_operations);
```

---

## 📝 Logcat фильтры

```bash
# Только OfflineManager
adb logcat OfflineManager

# Только FinanceRepository
adb logcat FinanceRepository

# Только ошибки
adb logcat | grep -i error

# Форматированный вывод
adb logcat -v threadtime | grep -E "Offline|Repository"
```

---

## ✨ Что это должно делать:

1. **Автоматическое сохранение** - при попытке создать операцию без интернета она сохраняется локально
2. **Видимый статус** - пользователь видит уведомление об офлайн режиме
3. **Автоматическая синхронизация** - когда интернет вернулся, операция отправляется автоматически
4. **Надежность** - операция повторяется до 3 раз при ошибках

Если что-то из этого не работает - смотрите логи! 🔍

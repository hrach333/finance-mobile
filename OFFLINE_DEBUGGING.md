# 🔍 Отладка: Почему операции не сохраняются в офлайн

## 📌 Шаг за шагом - как найти проблему

### Шаг 1: Проверка разрешений

**Откройте `app/src/main/AndroidManifest.xml`:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- ✅ ВАЖНО!
    
    ...
</manifest>
```

**Если разрешения нет** → добавьте их и пересоберите!

---

### Шаг 2: Проверка инициализации в MainActivity

**Откройте `MainActivity.kt`:**

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        
        // ✅ Проверьте что все создается
        val database = FinanceDatabase.getInstance(applicationContext)
        val networkMonitor = NetworkMonitor(applicationContext)
        val offlineManager = OfflineManager(
            dao = database.pendingOperationDao(),
            api = ApiClient.financeApi,
            networkMonitor = networkMonitor,
            gson = gson
        )
        
        // ✅ И передается в Repository
        val repository = FinanceRepository(
            api = ApiClient.financeApi,
            offlineManager = offlineManager,  // Должен быть не null!
            networkMonitor = networkMonitor
        )
        
        // Остальной код...
    }
}
```

**Что проверить:**
- [ ] `FinanceDatabase` создается?
- [ ] `NetworkMonitor` создается?
- [ ] `OfflineManager` создается без null?
- [ ] `offlineManager` передается в Repository?

---

### Шаг 3: Включите режим самолета и смотрите логи

```bash
# Цифра 1: Откройте Logcat в Android Studio
# Сверху найдите выпадающее меню и выберите свой девайс

# Затем в поле поиска введите фильтры:
FinanceRepository
OfflineManager  
NetworkMonitor

# Или используйте терминал:
adb logcat | grep -E "FinanceRepository|OfflineManager"
```

**Ожидаемые логи при включении режима самолета:**

```
NetworkMonitor: 📡 Сеть потеряна - OFFLINE
OfflineManager: Интернет отключен, переходим в офлайн режим
```

---

### Шаг 4: Создайте операцию и смотрите логи

```
1. В приложении нажмите "Добавить транзакцию"
2. Заполните данные
3. Нажмите "Создать"

ОЖИДАЕМЫЕ ЛОГИ:

FinanceRepository: createTransaction - Интернет: false, OfflineManager: true
FinanceRepository: ❌ Нет интернета. Сохранена транзакция в офлайн режиме
OfflineManager: ✅ Операция сохранена в БД: ID=1, Type=CREATE_TRANSACTION
```

---

### Шаг 5: Проверьте БД

```bash
# Откройте Device File Explorer в Android Studio
# Перейдите: data → data → com.hrach.financeapp → databases
# Загрузите файл finance_database

# ИЛИ через терминал:
adb shell
cd /data/data/com.hrach.financeapp/databases
sqlite3 finance_database

# Затем в sqlite3 интерпретаторе:
SELECT * FROM pending_operations;

# Должна быть ваша операция с status='PENDING'
```

---

## ❌ Диагностика проблем

### Проблема 1: "OfflineManager is null"

**Логи содержат:**
```
FinanceRepository: createTransaction - Интернет: false, OfflineManager: false
```

**Решение:**
```kotlin
// В MainActivity проверьте:
val offlineManager = OfflineManager(...) // Не должен быть null!

// Передайте в Repository:
val repository = FinanceRepository(
    api = ApiClient.financeApi,
    offlineManager = offlineManager,  // ❌ Если null - вот проблема!
    networkMonitor = networkMonitor
)
```

---

### Проблема 2: "NetworkMonitor не видит что нет интернета"

**Логи содержат:**
```
FinanceRepository: createTransaction - Интернет: true
```

Но интернета на самом деле нет!

**Решение:**
```bash
# Проверьте что режим самолета действительно включен:
adb shell dumpsys connectivity | grep -i airplane

# Проверьте что эмулятор поддерживает:
emulator -list-avds

# Иногда нужно перезагрузить эмулятор после режима самолета
```

---

### Проблема 3: "Таблица pending_operations не существует"

**Логи содержат:**
```
Error: table pending_operations does not exist
```

**Решение:**
```kotlin
// В FinanceDatabase проверьте:
@Database(
    entities = [PendingOperationEntity::class],  // ✅ Entity зарегистрирована?
    version = 1,
    exportSchema = true
)
```

Если добавили новое поле → увеличьте version и добавьте Migration.

---

### Проблема 4: "Ошибка при вставке в БД"

**Что проверить:**
```bash
# Посмотрите структуру таблицы:
sqlite3 /data/data/com.hrach.financeapp/databases/finance_database

PRAGMA table_info(pending_operations);

# Все ли поля есть?
```

---

## 🎯 Чек-лист отладки

- [ ] Разрешения в `AndroidManifest.xml` добавлены?
- [ ] `FinanceDatabase` создается в `MainActivity`?
- [ ] `NetworkMonitor` создается в `MainActivity`?
- [ ] `OfflineManager` создается в `MainActivity`?
- [ ] `OfflineManager` передается в `Repository`?
- [ ] Режим самолета действительно включен?
- [ ] Смотрели логи в Logcat?
- [ ] Таблица в БД существует?
- [ ] Операция видна в SQLite?

---

## 🚀 Финальный тест

Если все выше сделано:

```bash
# 1. Отключите интернет (режим самолета)
# 2. Создайте операцию
# 3. Посмотрите логи:
adb logcat | grep OfflineManager

# 4. Проверьте БД:
adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database
> SELECT COUNT(*) FROM pending_operations WHERE status='PENDING';

# Должно быть > 0

# 5. Включите интернет:
# 6. Через 5-10 сек смотрите синхронизацию:
adb logcat | grep "Sync"

# 7. Проверьте статус потом:
> SELECT * FROM pending_operations WHERE status='SYNCED';
```

---

## ❓ Если ничего не помогло

Пожалуйста, поделитесь:

1. **Полный лог из Logcat:**
   ```bash
   adb logcat > logcat_output.txt
   ```

2. **Вывод БД:**
   ```bash
   adb shell sqlite3 /data/data/com.hrach.financeapp/databases/finance_database ".dump pending_operations" > db_dump.txt
   ```

3. **Версия Android:**
   ```bash
   adb shell getprop ro.build.version.release
   ```

4. **Версия эмулятора:**
   ```bash
   emulator -version
   ```

С этой информацией намного легче найти проблему! 🔍

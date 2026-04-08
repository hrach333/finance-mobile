# Finance Mobile Starter

Это стартовый Android-клиент на Kotlin + Jetpack Compose для твоего Laravel API.

## Как использовать

1. Создай пустой Android-проект или распакуй этот архив.
2. Открой его в Android Studio.
3. Дождись Gradle Sync.
4. Проверь адрес API в `ApiClient.kt`.
5. Запусти приложение.

## Адрес API

Сейчас стоит:

```kotlin
private const val BASE_URL = "https://api.hrach.ru/api/"
```

Если тестируешь локально в эмуляторе, используй:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/api/"
```

Если тестируешь на реальном телефоне в одной сети Wi‑Fi, укажи IP компьютера:

```kotlin
private const val BASE_URL = "http://192.168.1.50:8000/api/"
```

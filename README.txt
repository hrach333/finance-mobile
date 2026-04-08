Finance Mobile V3 Patch

Распакуй архив в корень Android-проекта с заменой файлов.

Что добавлено:
- русский UI для типов операций и счетов
- создание счетов
- создание категорий
- улучшенный экран добавления операции
- более понятные карточки и подписи

После распаковки:
1. Sync Project with Gradle Files
2. Build > Rebuild Project
3. Run

Если API не на https://finance.hrach.ru/api/, измени BASE_URL в:
app/src/main/java/com/hrach/financeapp/data/api/ApiClient.kt

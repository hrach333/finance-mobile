package com.hrach.financeapp.data.offline

import android.util.Log
import com.google.gson.Gson
import com.hrach.financeapp.data.network.NetworkMonitor

/**
 * Extension функции для работы с офлайн операциями
 */

/**
 * Выполнить операцию с автоматическим сохранением при разрыве соединения
 */
suspend inline fun <T> withOfflineFallback(
    offlineManager: OfflineManager?,
    networkMonitor: NetworkMonitor?,
    operationType: String,
    jsonData: String,
    groupId: String? = null,
    remoteId: String? = null,
    apiCall: suspend () -> T
): Result<T> {
    return try {
        // Пытаемся выполнить API запрос
        val result = apiCall()
        Result.success(result)
    } catch (e: Exception) {
        // Если нет интернета - сохраняем операцию для позже
        if (networkMonitor?.isConnected() == false && offlineManager != null) {
            Log.d("OfflineFallback", "Сохранена офлайн операция: $operationType")
            offlineManager.savePendingOperation(
                operationType = operationType,
                jsonData = jsonData,
                groupId = groupId,
                remoteId = remoteId
            )
            // Возвращаем успех, как если бы операция выполнилась
            // Фактический результат будет синхронизирован позже
            @Suppress("UNCHECKED_CAST")
            Result.success(Unit as T)
        } else {
            Result.failure(e)
        }
    }
}

/**
 * Выполнить операцию и сохранить в офлайн очередь если она не завершится
 */
suspend inline fun <T> executeWithOfflineQueue(
    offlineManager: OfflineManager?,
    operationType: String,
    groupId: String? = null,
    remoteId: String? = null,
    crossinline serialize: suspend () -> String,
    crossinline apiCall: suspend () -> T
): T? {
    return try {
        apiCall()
    } catch (e: Exception) {
        if (offlineManager != null) {
            try {
                val jsonData = serialize()
                offlineManager.savePendingOperation(
                    operationType = operationType,
                    jsonData = jsonData,
                    groupId = groupId,
                    remoteId = remoteId
                )
                Log.d("OfflineQueue", "Операция $operationType добавлена в очередь")
                null
            } catch (serializationError: Exception) {
                Log.e("OfflineQueue", "Ошибка сохранения операции", serializationError)
                throw e
            }
        } else {
            throw e
        }
    }
}

/**
 * Безопасный вызов с обработкой офлайн режима
 */
suspend inline fun <T> safeApiCall(
    offlineManager: OfflineManager?,
    networkMonitor: NetworkMonitor?,
    operationType: String? = null,
    jsonData: String? = null,
    groupId: String? = null,
    remoteId: String? = null,
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        val result = block()
        Result.success(result)
    } catch (e: Exception) {
        Log.e("SafeApiCall", "API ошибка: ${e.message}", e)
        
        // Если есть данные для сохранения и нет сети
        if (operationType != null && jsonData != null && 
            networkMonitor?.isConnected() == false && offlineManager != null) {
            offlineManager.savePendingOperation(
                operationType = operationType,
                jsonData = jsonData,
                groupId = groupId,
                remoteId = remoteId
            )
            Result.success(Unit as T)
        } else {
            Result.failure(e)
        }
    }
}

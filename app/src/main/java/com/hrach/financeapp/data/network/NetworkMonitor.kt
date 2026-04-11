package com.hrach.financeapp.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Монитор состояния сетевого соединения
 * Отслеживает подключение/отключение интернета
 */
class NetworkMonitor(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkMonitor"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Flow, который эмитирует true когда есть интернет, false когда нет
     */
    val isOnline: Flow<Boolean> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "🌐 Сеть доступна - ONLINE")
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                Log.d(TAG, "📡 Сеть потеряна - OFFLINE")
                trySend(isConnected())
            }
            
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                Log.d(TAG, "⚙️ Возможности сети изменились - Internet: $hasInternet")
                trySend(isConnected())
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Отправляем текущее состояние при подписке
        val currentState = isConnected()
        Log.d(TAG, "📍 Начальное состояние - Online: $currentState")
        trySend(currentState)
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            Log.d(TAG, "🛑 NetworkMonitor остановлен")
        }
    }
    
    /**
     * Проверить текущее состояние сети синхронно
     */
    fun isConnected(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork ?: return false.also {
                Log.d(TAG, "✓ Активная сеть: нет")
            }
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false.also {
                Log.d(TAG, "✓ Возможности сети: нет")
            }
            val connected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d(TAG, "✓ Проверка сети: $connected")
            connected
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка при проверке сети: ${e.message}")
            false
        }
    }
}

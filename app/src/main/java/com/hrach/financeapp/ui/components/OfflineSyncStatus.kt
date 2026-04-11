package com.hrach.financeapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Компонент для отображения статуса офлайн синхронизации
 */
@Composable
fun OfflineSyncStatus(
    isSyncing: Boolean,
    pendingCount: Int,
    syncError: String?,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    // Показываем уведомление если есть ошибка, идет синхронизация или есть ожидающие операции
    val shouldShowStatus = !isOnline || isSyncing || pendingCount > 0 || syncError != null
    
    AnimatedVisibility(
        visible = shouldShowStatus,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    when {
                        syncError != null -> MaterialTheme.colorScheme.errorContainer
                        !isOnline -> MaterialTheme.colorScheme.tertiaryContainer
                        isSyncing -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            when {
                syncError != null -> {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Ошибка синхронизации",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                    )
                }
                isSyncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.width(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                !isOnline -> {
                    Icon(
                        imageVector = Icons.Filled.CloudOff,
                        contentDescription = "Офлайн режим",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                pendingCount > 0 -> {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = "Синхронизация ожидает",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = when {
                    syncError != null -> "Ошибка: $syncError"
                    isSyncing -> "Синхронизация..."
                    !isOnline -> "Офлайн режим - операции будут синхронизированы"
                    pendingCount > 0 -> "Ожидает синхронизацию ($pendingCount операций)"
                    else -> ""
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    syncError != null -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}

/**
 * Компонент для отображения кнопки синхронизации
 */
@Composable
fun SyncButton(
    isSyncing: Boolean,
    pendingCount: Int,
    isOnline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = pendingCount > 0 && isOnline && !isSyncing,
        modifier = modifier
    ) {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            enabled = !isSyncing
        ) {
            Icon(
                imageVector = Icons.Filled.CloudSync,
                contentDescription = "Синхронизировать",
                modifier = Modifier.width(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Синхронизировать ($pendingCount)")
        }
    }
}

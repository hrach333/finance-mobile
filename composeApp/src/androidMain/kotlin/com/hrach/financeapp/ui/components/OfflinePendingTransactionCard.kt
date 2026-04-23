package com.hrach.financeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.ui.utils.categoryIcon

@Composable
fun OfflinePendingTransactionCard(
    operation: PendingOperationEntity,
    categoryName: String?,
    accountName: String?
) {
    val isCreateTx = operation.operationType == PendingOperationEntity.TYPE_CREATE_TRANSACTION
    val request = if (isCreateTx) {
        try {
            Gson().fromJson(operation.jsonData, CreateTransactionRequest::class.java)
        } catch (_: Exception) {
            null
        }
    } else null

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFFFE1A8))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Оффлайн",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFEDD5)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        text = operation.operationType.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = request?.comment?.takeIf { it.isNotBlank() } ?: "Операция создана офлайн",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = request?.amount?.let { "${it} ₽" } ?: "—",
                color = Color(0xFF854D0E),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (!categoryName.isNullOrBlank() || !accountName.isNullOrBlank()) {
                Text(
                    text = listOfNotNull(categoryName, accountName).joinToString(separator = " • "),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (request == null) {
                Text(
                    text = "Данные сохранены локально и будут отправлены при восстановлении сети.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF92400E),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

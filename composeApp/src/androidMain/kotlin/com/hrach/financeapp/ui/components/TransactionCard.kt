package com.hrach.financeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.ui.utils.formatIsoDateForUi
import com.hrach.financeapp.ui.utils.categoryIcon
import com.hrach.financeapp.ui.utils.getIconBackgroundColor

@Composable
fun TransactionCard(
    item: TransactionDto,
    category: CategoryDto?,
    accountName: String?
) {
    val isIncome = item.type.uppercase() == "INCOME"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EDF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = getIconBackgroundColor(category?.iconKey).copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(category?.iconKey),
                    contentDescription = null,
                    tint = getIconBackgroundColor(category?.iconKey)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category?.name ?: "Без категории",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.comment ?: accountName ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = formatIsoDateForUi(item.transactionDate),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "${item.amount} ₽",
                color = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

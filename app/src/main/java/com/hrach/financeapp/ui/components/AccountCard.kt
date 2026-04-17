package com.hrach.financeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.ui.utils.accountTypeIcon
import com.hrach.financeapp.ui.utils.toAccountTypeLabel
import com.hrach.financeapp.ui.utils.getAccountTypeColor
import com.hrach.financeapp.ui.utils.getAccountTypeColor

@Composable
fun AccountCard(
    account: AccountDto,
    onEdit: (AccountDto) -> Unit,
    onDelete: (AccountDto) -> Unit
) {
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
                        color = getAccountTypeColor(account.type).copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = accountTypeIcon(account.type),
                    contentDescription = null,
                    tint = getAccountTypeColor(account.type)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )
                Text(account.type.toAccountTypeLabel(), style = MaterialTheme.typography.bodySmall)
            }

            Text(
                text = "${account.currentBalance} ${account.currency}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(4.dp))
            IconButton(onClick = { onEdit(account) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
            }
            IconButton(onClick = { onDelete(account) }) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Удалить")
            }
        }
    }
}

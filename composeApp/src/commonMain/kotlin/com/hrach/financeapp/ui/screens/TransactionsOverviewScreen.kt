package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.OverviewColorToken
import com.hrach.financeapp.data.model.TransactionKind
import com.hrach.financeapp.data.model.TransactionOverview

@Composable
fun TransactionsOverviewScreen(
    overview: FinanceOverview,
    onCreateTransaction: (String, Double, Int, String, String) -> Unit,
    onUpdateTransaction: (TransactionOverview, String, Double, Int, String, String) -> Unit,
    onDeleteTransaction: (TransactionOverview) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<TransactionOverview?>(null) }
    var deleteTarget by remember { mutableStateOf<TransactionOverview?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Операции", subtitle = "Последние доходы и расходы")
        }
        item {
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White)
            ) {
                Text("Добавить операцию")
            }
        }
        items(overview.transactions) { transaction ->
            TransactionOverviewCard(
                transaction = transaction,
                onEdit = { editTarget = transaction },
                onDelete = { deleteTarget = transaction }
            )
        }
    }

    if (showCreateDialog) {
        TransactionOverviewEditorDialog(
            title = "Новая операция",
            accounts = overview.accounts,
            onDismiss = { showCreateDialog = false },
            onSave = { type, amount, accountId, date, comment ->
                onCreateTransaction(type, amount, accountId, date, comment)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { transaction ->
        TransactionOverviewEditorDialog(
            title = "Редактировать операцию",
            accounts = overview.accounts,
            initialType = if (transaction.kind == TransactionKind.Income) "INCOME" else "EXPENSE",
            initialAmount = transaction.amount.toString(),
            initialAccountId = transaction.accountId,
            initialDate = transaction.transactionDate,
            initialComment = transaction.comment,
            onDismiss = { editTarget = null },
            onSave = { type, amount, accountId, date, comment ->
                onUpdateTransaction(transaction, type, amount, accountId, date, comment)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { transaction ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(transaction)
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE85B6A), contentColor = Color.White)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Отмена")
                }
            },
            title = { Text("Удалить операцию?") },
            text = { Text("${transaction.category}: ${transaction.amountLabel}") }
        )
    }
}

@Composable
fun TransactionOverviewCard(
    transaction: TransactionOverview,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val tint = transaction.colorToken.toScreenColor()

    Card(
        backgroundColor = Color(0xFFF4EDF7),
        elevation = 3.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.category.take(1), color = tint, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.category, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text(transaction.comment, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
            }
            Text(transaction.dateLabel, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
            Text(
                transaction.amountLabel,
                color = if (transaction.kind == TransactionKind.Income) Color(0xFF16A34A) else Color(0xFFDC2626),
                fontWeight = FontWeight.Bold
            )
        }
        if (onEdit != null || onDelete != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 68.dp, end = 14.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                onEdit?.let {
                    TextButton(onClick = it) {
                        Text("Изменить")
                    }
                }
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("Удалить", color = Color(0xFFE85B6A))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionOverviewEditorDialog(
    title: String,
    accounts: List<AccountOverview>,
    initialType: String = "EXPENSE",
    initialAmount: String = "",
    initialAccountId: Int? = accounts.firstOrNull()?.id,
    initialDate: String = "2026-04-24",
    initialComment: String = "",
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String, String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var amount by remember { mutableStateOf(initialAmount) }
    var accountId by remember { mutableStateOf(initialAccountId) }
    var date by remember { mutableStateOf(initialDate.takeIf { it.isNotBlank() } ?: "2026-04-24") }
    var comment by remember { mutableStateOf(initialComment) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.replace(',', '.').toDoubleOrNull()
                    val selectedAccountId = accountId
                    when {
                        selectedAccountId == null -> error = "Выберите счет"
                        parsedAmount == null || parsedAmount <= 0.0 -> error = "Введите корректную сумму"
                        date.isBlank() -> error = "Введите дату"
                        else -> onSave(type, parsedAmount, selectedAccountId, date.trim(), comment.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White)
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionTypeButton("EXPENSE", type, "Расход") { type = it }
                    TransactionTypeButton("INCOME", type, "Доход") { type = it }
                }

                TextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        error = null
                    },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextField(
                    value = date,
                    onValueChange = {
                        date = it
                        error = null
                    },
                    label = { Text("Дата, например 2026-04-24") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Счет", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                    accounts.forEach { account ->
                        val selected = account.id == accountId
                        Button(
                            onClick = {
                                accountId = account.id
                                error = null
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selected) Color(0xFF5E4B8B) else Color(0xFFF1E7FB),
                                contentColor = if (selected) Color.White else Color(0xFF5E4B8B)
                            ),
                            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(account.title)
                        }
                    }
                }

                error?.let {
                    Text(text = it, color = Color(0xFFE85B6A), style = MaterialTheme.typography.body2)
                }
            }
        }
    )
}

@Composable
private fun TransactionTypeButton(
    value: String,
    selectedValue: String,
    label: String,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    Button(
        onClick = { onSelected(value) },
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (selected) Color(0xFF5E4B8B) else Color(0xFFF1E7FB),
            contentColor = if (selected) Color.White else Color(0xFF5E4B8B)
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.caption)
    }
}

private fun OverviewColorToken.toScreenColor(): Color {
    return when (this) {
        OverviewColorToken.Income -> Color(0xFF16A34A)
        OverviewColorToken.Expense -> Color(0xFFE85B6A)
        OverviewColorToken.Primary -> Color(0xFF5E4B8B)
        OverviewColorToken.Secondary -> Color(0xFF4C5E8B)
        OverviewColorToken.Muted -> Color(0xFF6B6579)
    }
}

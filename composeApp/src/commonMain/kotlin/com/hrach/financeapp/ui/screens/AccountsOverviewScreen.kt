package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrach.financeapp.data.model.AccountOverview
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.OverviewColorToken

@Composable
fun AccountsOverviewScreen(
    overview: FinanceOverview,
    onCreateAccount: (String, String, Double) -> Unit,
    onUpdateAccount: (AccountOverview, String, String, Double) -> Unit,
    onDeleteAccount: (AccountOverview) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<AccountOverview?>(null) }
    var deleteTarget by remember { mutableStateOf<AccountOverview?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Счета", subtitle = "Баланс по семейной группе")
        }
        item {
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White)
            ) {
                Text("Добавить счет")
            }
        }
        items(overview.accounts) { account ->
            AccountOverviewCard(
                account = account,
                onEdit = { editTarget = account },
                onDelete = { deleteTarget = account }
            )
        }
    }

    if (showCreateDialog) {
        AccountOverviewEditorDialog(
            title = "Новый счет",
            onDismiss = { showCreateDialog = false },
            onSave = { name, type, balance ->
                onCreateAccount(name, type, balance)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { account ->
        AccountOverviewEditorDialog(
            title = "Редактировать счет",
            initialName = account.title,
            initialType = account.type,
            initialBalance = account.currentBalance.toString(),
            onDismiss = { editTarget = null },
            onSave = { name, type, balance ->
                onUpdateAccount(account, name, type, balance)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { account ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAccount(account)
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
            title = { Text("Удалить счет?") },
            text = { Text("Счет ${account.title} будет удален.") }
        )
    }
}

@Composable
private fun AccountOverviewCard(
    account: AccountOverview,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(account.title, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                Text(account.balanceLabel, color = account.colorToken.toScreenColor(), fontWeight = FontWeight.Bold)
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(account.subtitle, color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) {
                        Text("Изменить")
                    }
                    TextButton(onClick = onDelete) {
                        Text("Удалить", color = Color(0xFFE85B6A))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountOverviewEditorDialog(
    title: String,
    initialName: String = "",
    initialType: String = "CASH",
    initialBalance: String = "0",
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var balance by remember { mutableStateOf(initialBalance) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedBalance = balance.replace(',', '.').toDoubleOrNull()
                    when {
                        name.isBlank() -> error = "Введите название"
                        parsedBalance == null -> error = "Введите корректный баланс"
                        else -> onSave(name.trim(), type, parsedBalance)
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
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountTypeButton("CASH", type, "Наличные") { type = it }
                    AccountTypeButton("CARD", type, "Карта") { type = it }
                    AccountTypeButton("SAVINGS", type, "Копилка") { type = it }
                }

                TextField(
                    value = balance,
                    onValueChange = {
                        balance = it
                        error = null
                    },
                    label = { Text("Баланс") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                error?.let {
                    Text(text = it, color = Color(0xFFE85B6A), style = MaterialTheme.typography.body2)
                }
            }
        }
    )
}

@Composable
private fun AccountTypeButton(
    value: String,
    selectedValue: String,
    label: String,
    onSelected: (String) -> Unit
) {
    val selected = value == selectedValue
    Button(
        onClick = { onSelected(value) },
        modifier = Modifier.width(92.dp),
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

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
import com.hrach.financeapp.data.currency.CurrencyCatalog
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.GroupOverview

@Composable
fun GroupsOverviewScreen(
    overview: FinanceOverview,
    onBack: () -> Unit,
    onCreateGroup: (String, String) -> Unit,
    onUpdateGroup: (GroupOverview, String, String) -> Unit,
    onSelectGroup: (GroupOverview) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<GroupOverview?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            GroupsHeader(onBack = onBack)
        }
        item {
            if (overview.isOfflineMode) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = Color(0xFFF9F6FC),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                    elevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "В офлайн режиме доступна одна группа: «Мой бюджет». Зарегистрируйтесь, чтобы создавать несколько бюджетов и синхронизировать их.",
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF6B6579)
                    )
                }
            } else {
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White)
                ) {
                    Text("Создать группу")
                }
            }
        }
        items(overview.groups) { group ->
            GroupOverviewCard(
                group = group,
                selected = group.id == overview.activeGroupId,
                onSelect = { onSelectGroup(group) },
                onEdit = { if (!overview.isOfflineMode) editTarget = group },
                canEdit = !overview.isOfflineMode
            )
        }
    }

    if (showCreateDialog) {
        GroupOverviewEditorDialog(
            title = "Новая группа",
            onDismiss = { showCreateDialog = false },
            onSave = { name, currency ->
                onCreateGroup(name, currency)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { group ->
        GroupOverviewEditorDialog(
            title = "Редактировать группу",
            initialName = group.name,
            initialCurrency = group.baseCurrency,
            onDismiss = { editTarget = null },
            onSave = { name, currency ->
                onUpdateGroup(group, name, currency)
                editTarget = null
            }
        )
    }
}

@Composable
private fun GroupsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text("Группы", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold, color = Color(0xFF23212B))
            Text("Рабочие пространства бюджета", color = Color(0xFF4B4760), style = MaterialTheme.typography.body2)
        }
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF1E7FB), contentColor = Color(0xFF5E4B8B)),
            elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
        ) {
            Text("←")
        }
    }
}

@Composable
private fun GroupOverviewCard(
    group: GroupOverview,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    canEdit: Boolean
) {
    val accent = if (selected) Color(0xFF16A34A) else Color(0xFF5E4B8B)
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(group.name, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                    Text(
                        "Валюта: ${CurrencyCatalog.symbolFor(group.baseCurrency)} ${group.baseCurrency}",
                        color = Color(0xFF6B6579),
                        style = MaterialTheme.typography.body2
                    )
                }
                Text(
                    text = if (selected) "Активна" else "Доступна",
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.body2
                )
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (canEdit) {
                    RoundIconButton(FinanceIcon.Edit, "Изменить группу", onEdit, size = 36.dp)
                }
                RoundIconButton(
                    FinanceIcon.Check,
                    "Выбрать группу",
                    onSelect,
                    size = 36.dp,
                    background = if (selected) AppGreen.copy(alpha = 0.16f) else AppLilac,
                    contentColor = if (selected) AppGreen else AppPurple,
                    enabled = !selected
                )
            }
        }
    }
}

@Composable
private fun GroupOverviewEditorDialog(
    title: String,
    initialName: String = "",
    initialCurrency: String = CurrencyCatalog.DEFAULT_CODE,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var currency by remember { mutableStateOf(CurrencyCatalog.normalize(initialCurrency)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Введите название"
                        currency.isBlank() -> error = "Выберите валюту"
                        else -> onSave(name.trim(), CurrencyCatalog.normalize(currency))
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

                Text("Валюта", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyCatalog.supported.forEach { option ->
                        CurrencyButton(option.code, currency) { currency = it }
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
private fun CurrencyButton(value: String, selectedValue: String, onSelected: (String) -> Unit) {
    val selected = value == selectedValue
    Button(
        onClick = { onSelected(value) },
        modifier = Modifier.width(86.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (selected) Color(0xFF5E4B8B) else Color(0xFFF1E7FB),
            contentColor = if (selected) Color.White else Color(0xFF5E4B8B)
        ),
        elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
    ) {
        Text("$value ${CurrencyCatalog.symbolFor(value)}", style = MaterialTheme.typography.caption)
    }
}

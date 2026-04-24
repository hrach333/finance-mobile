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
import com.hrach.financeapp.data.model.CategoryOverview
import com.hrach.financeapp.data.model.FinanceOverview

@Composable
fun CategoriesOverviewScreen(
    overview: FinanceOverview,
    onCreateCategory: (String, String, String?) -> Unit,
    onUpdateCategory: (CategoryOverview, String, String, String?) -> Unit,
    onDeleteCategory: (CategoryOverview) -> Unit
) {
    val expenses = overview.categories.filter { it.type == "EXPENSE" }
    val incomes = overview.categories.filter { it.type == "INCOME" }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryOverview?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryOverview?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Категории", subtitle = "Группировка доходов и расходов")
        }
        item {
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White)
            ) {
                Text("Добавить категорию")
            }
        }

        item { SectionTitle("Расходы") }
        items(expenses) { category ->
            CategoryOverviewCard(
                category = category,
                onEdit = { editTarget = category },
                onDelete = { deleteTarget = category }
            )
        }

        item { SectionTitle("Доходы") }
        items(incomes) { category ->
            CategoryOverviewCard(
                category = category,
                onEdit = { editTarget = category },
                onDelete = { deleteTarget = category }
            )
        }
    }

    if (showCreateDialog) {
        CategoryOverviewEditorDialog(
            title = "Новая категория",
            onDismiss = { showCreateDialog = false },
            onSave = { name, type, iconKey ->
                onCreateCategory(name, type, iconKey)
                showCreateDialog = false
            }
        )
    }

    editTarget?.let { category ->
        CategoryOverviewEditorDialog(
            title = "Редактировать категорию",
            initialName = category.name,
            initialType = category.type,
            initialIconKey = category.iconKey ?: defaultIconForType(category.type),
            onDismiss = { editTarget = null },
            onSave = { name, type, iconKey ->
                onUpdateCategory(category, name, type, iconKey)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { category ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(category)
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
            title = { Text("Удалить категорию?") },
            text = { Text("Категория ${category.name} будет удалена.") }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold, color = Color(0xFF23212B))
}

@Composable
private fun CategoryOverviewCard(
    category: CategoryOverview,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0xFFF9F6FC),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(category.name, color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                    Text(category.type.toCategoryTypeLabel(), color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                }
                Text(category.iconKey ?: defaultIconForType(category.type), color = Color(0xFF5E4B8B), fontWeight = FontWeight.Bold)
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

@Composable
private fun CategoryOverviewEditorDialog(
    title: String,
    initialName: String = "",
    initialType: String = "EXPENSE",
    initialIconKey: String = "shopping",
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var iconKey by remember { mutableStateOf(initialIconKey) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Введите название"
                    } else {
                        onSave(name.trim(), type, iconKey)
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
                    CategoryTypeButton("EXPENSE", type, "Расход") {
                        type = it
                        iconKey = defaultIconForType(it)
                    }
                    CategoryTypeButton("INCOME", type, "Доход") {
                        type = it
                        iconKey = defaultIconForType(it)
                    }
                }

                Text("Иконка", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                iconOptionsForType(type).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            CategoryIconButton(
                                value = option,
                                selectedValue = iconKey,
                                onSelected = { iconKey = it }
                            )
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
private fun CategoryTypeButton(
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

@Composable
private fun CategoryIconButton(
    value: String,
    selectedValue: String,
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
        Text(value, style = MaterialTheme.typography.caption)
    }
}

private fun String.toCategoryTypeLabel(): String = when (this) {
    "INCOME" -> "Доход"
    else -> "Расход"
}

private fun defaultIconForType(type: String): String = if (type == "INCOME") "salary" else "shopping"

private fun iconOptionsForType(type: String): List<String> {
    return if (type == "INCOME") {
        listOf("salary", "work", "gift", "bonus", "cash")
    } else {
        listOf("shopping", "transport", "health", "home", "food", "other")
    }
}

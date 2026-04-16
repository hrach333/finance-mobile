package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.ui.utils.categoryIconOptions
import com.hrach.financeapp.viewmodel.HomeViewModel

private fun accountTypeLabel(type: String): String {
    return when (type.uppercase()) {
        "CASH" -> "Наличные"
        "CARD" -> "Карта"
        "SAVINGS" -> "Накопления"
        else -> type
    }
}

private fun transactionTypeLabel(type: String): String {
    return when (type.uppercase()) {
        "INCOME" -> "Доход"
        "EXPENSE" -> "Расход"
        else -> type
    }
}

private fun categoryIcon(iconKey: String?): ImageVector {
    val option = categoryIconOptions.firstOrNull { it.key == iconKey }
    return option?.icon ?: categoryIconOptions.last().icon
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountEditorDialog(
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
    var typeExpanded by remember { mutableStateOf(false) }

    val accountTypes = listOf("CASH", "CARD", "SAVINGS")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedBalance = balance.toDoubleOrNull() ?: return@Button
                    if (name.isBlank()) return@Button
                    onSave(name, type, parsedBalance)
                }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = accountTypeLabel(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип счета") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        accountTypes.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(accountTypeLabel(item)) },
                                onClick = {
                                    type = item
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it },
                    label = { Text("Баланс") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorDialog(
    title: String,
    initialName: String = "",
    initialType: String = "EXPENSE",
    initialIconKey: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var type by remember { mutableStateOf(initialType) }
    var iconKey by remember { mutableStateOf(initialIconKey ?: "shopping") }
    var typeExpanded by remember { mutableStateOf(false) }

    val categoryTypes = listOf("EXPENSE", "INCOME")
    val categoryIcons = categoryIconOptions.map { it.key }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    onSave(name, type, iconKey)
                }
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = if (type == "INCOME") "Доход" else "Расход",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        categoryTypes.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (item == "INCOME") "Доход" else "Расход")
                                },
                                onClick = {
                                    type = item
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Иконка")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(180.dp)
                ) {
                    items(categoryIcons) { key ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = if (iconKey == key) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CircleShape
                                )
                                .clickable { iconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon(key),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorDialog(
    title: String,
    viewModel: HomeViewModel,
    initialItem: TransactionDto? = null,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, Int, String) -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var type by remember { mutableStateOf(initialItem?.type ?: "EXPENSE") }
    var amount by remember { mutableStateOf(initialItem?.amount?.toString() ?: "") }
    var comment by remember { mutableStateOf(initialItem?.comment ?: "") }
    var selectedAccountId by remember { mutableStateOf(initialItem?.accountId ?: accounts.firstOrNull()?.id) }
    var selectedCategoryId by remember { mutableStateOf(initialItem?.categoryId ?: categories.firstOrNull()?.id) }

    var typeExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { it.type == type }

    if (selectedCategoryId != null && filteredCategories.none { it.id == selectedCategoryId }) {
        selectedCategoryId = filteredCategories.firstOrNull()?.id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                    val accountId = selectedAccountId ?: return@Button
                    val categoryId = selectedCategoryId ?: return@Button
                    onSave(type, parsedAmount, accountId, categoryId, comment)
                }
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
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = transactionTypeLabel(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Тип операции") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        listOf("EXPENSE", "INCOME").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(transactionTypeLabel(item)) },
                                onClick = {
                                    type = item
                                    typeExpanded = false
                                    selectedCategoryId = categories.firstOrNull { it.type == item }?.id
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Сумма") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        value = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Счет") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = filteredCategories.firstOrNull { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        filteredCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Комментарий") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
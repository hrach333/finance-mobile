package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.ui.utils.toTransactionLabel
import com.hrach.financeapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(viewModel: HomeViewModel, paddingValues: PaddingValues, onSaved: () -> Unit) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val expenseCategories = categories.filter { it.type == "EXPENSE" }
    val incomeCategories = categories.filter { it.type == "INCOME" }
    var type by remember { mutableStateOf("EXPENSE") }
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var accountExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var selectedCategoryId by remember { mutableStateOf(expenseCategories.firstOrNull()?.id) }
    val currentCategories = if (type == "INCOME") incomeCategories else expenseCategories

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Новая операция", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                    OutlinedTextField(value = type.toTransactionLabel(), onValueChange = {}, readOnly = true, label = { Text("Тип") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        DropdownMenuItem(text = { Text("Расход") }, onClick = { type = "EXPENSE"; selectedCategoryId = expenseCategories.firstOrNull()?.id; typeExpanded = false })
                        DropdownMenuItem(text = { Text("Доход") }, onClick = { type = "INCOME"; selectedCategoryId = incomeCategories.firstOrNull()?.id; typeExpanded = false })
                    }
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Сумма") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = !accountExpanded }) {
                    OutlinedTextField(value = accounts.firstOrNull { it.id == selectedAccountId }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Счет") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                        accounts.forEach { account -> DropdownMenuItem(text = { Text(account.name) }, onClick = { selectedAccountId = account.id; accountExpanded = false }) }
                    }
                }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(value = currentCategories.firstOrNull { it.id == selectedCategoryId }?.name ?: "", onValueChange = {}, readOnly = true, label = { Text("Категория") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        currentCategories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategoryId = category.id; categoryExpanded = false }) }
                    }
                }
                OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Комментарий") }, modifier = Modifier.fillMaxWidth())
            }
        }
        Button(onClick = {
            val parsedAmount = amount.toDoubleOrNull() ?: return@Button
            val accountId = selectedAccountId ?: return@Button
            val categoryId = selectedCategoryId ?: return@Button
            viewModel.createTransaction(type, parsedAmount, accountId, categoryId, comment)
            onSaved()
        }, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
    }
}

package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.Gson
import com.hrach.financeapp.data.db.entity.PendingOperationEntity
import com.hrach.financeapp.data.dto.CreateTransactionRequest
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.ui.components.OfflinePendingTransactionCard
import com.hrach.financeapp.ui.components.TransactionCard
import com.hrach.financeapp.ui.utils.formatIsoDateForUi
import com.hrach.financeapp.ui.utils.parseUiOrIsoDateToIso
import com.hrach.financeapp.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: HomeViewModel, paddingValues: PaddingValues) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val pendingOfflineOperations by viewModel.pendingOfflineOperations.collectAsStateWithLifecycle(initialValue = emptyList())
    val gson = remember { Gson() }
    var deleteTarget by remember { mutableStateOf<TransactionDto?>(null) }
    var editTarget by remember { mutableStateOf<TransactionDto?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var dateFrom by remember { mutableStateOf("") }
    var dateTo by remember { mutableStateOf("") }
    var dateFieldTarget by remember { mutableStateOf<String?>(null) }
    var page by remember { mutableStateOf(0) }
    val pageSize = 15

    val filteredTransactions = remember(transactions, selectedCategoryId, dateFrom, dateTo) {
        val fromDate = parseUiOrIsoDateToIso(dateFrom)?.let(LocalDate::parse)
        val toDate = parseUiOrIsoDateToIso(dateTo)?.let(LocalDate::parse)
        transactions.filter { transaction ->
            val categoryMatches = selectedCategoryId == null || transaction.categoryId == selectedCategoryId
            val transactionLocalDate = runCatching { LocalDate.parse(transaction.transactionDate) }.getOrNull()
            val fromMatches = fromDate == null || (transactionLocalDate != null && !transactionLocalDate.isBefore(fromDate))
            val toMatches = toDate == null || (transactionLocalDate != null && !transactionLocalDate.isAfter(toDate))
            categoryMatches && fromMatches && toMatches
        }
    }
    val totalPages = (filteredTransactions.size + pageSize - 1) / pageSize
    val pagedTransactions = filteredTransactions.drop(page * pageSize).take(pageSize)

    LaunchedEffect(filteredTransactions.size) {
        if (page > 0 && page >= totalPages) {
            page = (totalPages - 1).coerceAtLeast(0)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F0FF),
                        Color(0xFFF8F6FC),
                        Color(0xFFE6DFEA)
                    )
                )
            )
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Операции", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                OutlinedTextField(
                    value = selectedCategoryId?.let { id -> categories.firstOrNull { it.id == id }?.name } ?: "Все категории",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                androidx.compose.material3.DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Все категории") },
                        onClick = {
                            selectedCategoryId = null
                            categoryExpanded = false
                        }
                    )
                    categories.forEach { category ->
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
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateFrom,
                    onValueChange = { dateFrom = it },
                    label = { Text("Дата с") },
                    trailingIcon = {
                        IconButton(onClick = { dateFieldTarget = "from" }) {
                            Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Выбрать дату начала")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dateTo,
                    onValueChange = { dateTo = it },
                    label = { Text("Дата по") },
                    trailingIcon = {
                        IconButton(onClick = { dateFieldTarget = "to" }) {
                            Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Выбрать дату окончания")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Text(
                text = "Найдено: ${filteredTransactions.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (pendingOfflineOperations.isNotEmpty()) {
            item {
                Text(
                    "Операции, сохранённые офлайн",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            items(pendingOfflineOperations) { operation ->
                val request = if (operation.operationType == PendingOperationEntity.TYPE_CREATE_TRANSACTION) {
                    try {
                        gson.fromJson(operation.jsonData, CreateTransactionRequest::class.java)
                    } catch (_: Exception) {
                        null
                    }
                } else null
                OfflinePendingTransactionCard(
                    operation = operation,
                    categoryName = request?.categoryId?.let { categories.firstOrNull { category -> category.id == it }?.name },
                    accountName = request?.accountId?.let { viewModel.accountName(it) }
                )
            }
        }

        items(pagedTransactions) { item ->
            TransactionCard(
                item = item,
                category = categories.firstOrNull { it.id == item.categoryId },
                accountName = viewModel.accountName(item.accountId)
            )
        }
        if (totalPages > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { if (page > 0) page -= 1 }, enabled = page > 0) {
                        Text("Назад")
                    }
                    Text("Страница ${page + 1} из $totalPages", modifier = Modifier.padding(top = 10.dp))
                    Button(onClick = { if (page < totalPages - 1) page += 1 }, enabled = page < totalPages - 1) {
                        Text("Вперёд")
                    }
                }
            }
        }
    }

    dateFieldTarget?.let { target ->
        val selectedDateValue = if (target == "from") dateFrom else dateTo
        val selectedDateMillis = parseUiOrIsoDateToIso(selectedDateValue)
            ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { dateFieldTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val isoDate = java.time.Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        val uiDate = formatIsoDateForUi(isoDate)
                        if (target == "from") dateFrom = uiDate else dateTo = uiDate
                    }
                    dateFieldTarget = null
                }) { Text("Выбрать") }
            },
            dismissButton = { TextButton(onClick = { dateFieldTarget = null }) { Text("Отмена") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTransaction(item.id)
                    deleteTarget = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } },
            title = { Text("Удалить операцию?") },
            text = { Text("Операция будет удалена безвозвратно.") }
        )
    }

    editTarget?.let { item ->
        TransactionEditorDialog(
            title = "Редактировать операцию",
            viewModel = viewModel,
            initialItem = item,
            onDismiss = { editTarget = null },
            onSave = { type, amount, accountId, categoryId, comment ->
                viewModel.updateTransaction(item, type, amount, accountId, categoryId, comment)
                editTarget = null
            }
        )
    }
}

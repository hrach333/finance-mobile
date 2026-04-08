package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.hrach.financeapp.data.dto.TransactionDto
import com.hrach.financeapp.ui.components.TransactionCard
import com.hrach.financeapp.viewmodel.HomeViewModel

@Composable
fun TransactionsScreen(viewModel: HomeViewModel, paddingValues: PaddingValues) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<TransactionDto?>(null) }
    var editTarget by remember { mutableStateOf<TransactionDto?>(null) }

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
        items(transactions) { item ->
            TransactionCard(
                item = item,
                category = categories.firstOrNull { it.id == item.categoryId },
                accountName = viewModel.accountName(item.accountId)
            )
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

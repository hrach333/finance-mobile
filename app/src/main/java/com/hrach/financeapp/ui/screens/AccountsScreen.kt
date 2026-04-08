package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.data.dto.AccountDto
import com.hrach.financeapp.ui.components.AccountCard
import com.hrach.financeapp.viewmodel.HomeViewModel

@Composable
fun AccountsScreen(viewModel: HomeViewModel, paddingValues: PaddingValues) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<AccountDto?>(null) }
    var editTarget by remember { mutableStateOf<AccountDto?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Счета", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { FilledTonalButton(onClick = { showCreate = true }) { Text("Добавить счет") } }
        items(accounts) { item ->
            AccountCard(item, onEdit = { editTarget = item }, onDelete = { deleteTarget = item })
        }
    }

    if (showCreate) {
        AccountEditorDialog(
            title = "Новый счет",
            onDismiss = { showCreate = false },
            onSave = { name, type, balance ->
                viewModel.createAccount(name, type, balance)
                showCreate = false
            }
        )
    }

    editTarget?.let { account ->
        AccountEditorDialog(
            title = "Редактировать счет",
            initialName = account.name,
            initialType = account.type,
            initialBalance = account.currentBalance.toString(),
            onDismiss = { editTarget = null },
            onSave = { name, type, balance ->
                viewModel.updateAccount(
                    account = account,
                    name = name,
                    type = type,
                    initialBalance = balance,
                    shared = account.shared,
                    active = account.isActive
                )
                editTarget = null
            }
        )
    }

    deleteTarget?.let { account ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAccount(account.id)
                    deleteTarget = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } },
            title = { Text("Удалить счет?") },
            text = { Text("Счет ${account.name} будет удален.") }
        )
    }
}

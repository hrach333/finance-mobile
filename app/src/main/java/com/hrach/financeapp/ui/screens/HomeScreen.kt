package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hrach.financeapp.data.dto.GroupDto
import com.hrach.financeapp.ui.components.SummaryHeroCard
import com.hrach.financeapp.ui.components.TransactionCard
import com.hrach.financeapp.viewmodel.HomeViewModel
import com.hrach.financeapp.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    sessionViewModel: SessionViewModel,
    paddingValues: PaddingValues,
    onOpenCategories: () -> Unit,
    onOpenMembers: () -> Unit
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()
    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }

    var expanded by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var showEditGroup by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFCCCFDF), Color(0xFFEFD6EF), Color(0xFFABA7CE))
                )
            )
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Главная",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF23212B)
                )
                TextButton(onClick = { sessionViewModel.logout() }) {
                    Text("Выйти")
                }
            }
            if (currentUser != null) {
                Text(
                    text = "Пользователь: ${currentUser!!.name}",
                    color = Color(0xFF4B4760)
                )
            }
        }

        if (groups.isEmpty()) {
            item {
                EmptyGroupsCard(
                    loading = loading,
                    onCreateClick = { showCreateGroup = true }
                )
            }

            if (!error.isNullOrBlank()) {
                item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            }

            item { Spacer(Modifier.height(8.dp)) }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedGroup?.name ?: "Выбери группу",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Группа") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        viewModel.selectGroup(group.id)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showCreateGroup = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Создать группу")
                    }
                    IconButton(
                        onClick = { showEditGroup = true },
                        enabled = selectedGroup != null
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Редактировать группу")
                    }
                }
            }

            item {
                SummaryHeroCard(
                    balance = "${summary?.balance ?: 0.0} ₽",
                    income = "${summary?.income ?: 0.0} ₽",
                    expense = "${summary?.expense ?: 0.0} ₽"
                )
            }

            if (loading) {
                item { Text("Загрузка...") }
            }

            if (!error.isNullOrBlank()) {
                item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            }

            item {
                Text(
                    text = "Последние операции",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF23212B)
                )
            }

            items(transactions.take(5)) { item ->
                TransactionCard(
                    item = item,
                    category = categories.firstOrNull { it.id == item.categoryId },
                    accountName = viewModel.accountName(item.accountId)
                )
            }

            item {
                ActionCard(
                    title = "Участники группы",
                    tint = Color(0xFF4C5E8B),
                    boxColor = Color(0xFFE5ECFB),
                    icon = { Icon(imageVector = Icons.Filled.Group, contentDescription = null, tint = Color(0xFF4C5E8B)) },
                    onClick = onOpenMembers
                )
            }

            item {
                ActionCard(
                    title = "Категории",
                    tint = Color(0xFF5E4B8B),
                    boxColor = Color(0xFFF1E7FB),
                    icon = { Icon(imageVector = Icons.Filled.LocalOffer, contentDescription = null, tint = Color(0xFF5E4B8B)) },
                    onClick = onOpenCategories
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showCreateGroup) {
        GroupEditorDialog(
            title = "Новая группа",
            onDismiss = { showCreateGroup = false },
            onSave = { name, currency ->
                viewModel.createGroup(name, currency)
                showCreateGroup = false
            }
        )
    }

    if (showEditGroup && selectedGroup != null) {
        GroupEditorDialog(
            title = "Редактировать группу",
            initialName = selectedGroup.name,
            initialCurrency = selectedGroup.baseCurrency,
            onDismiss = { showEditGroup = false },
            onSave = { name, currency ->
                viewModel.updateGroup(name, currency)
                showEditGroup = false
            }
        )
    }
}

@Composable
private fun EmptyGroupsCard(
    loading: Boolean,
    onCreateClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6FC)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "У тебя пока нет семейной группы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F2B3A)
            )
            Text(
                text = "Сначала создай группу. После этого заработают счета, категории, операции и управление участниками.",
                color = Color(0xFF4B4760)
            )
            Button(
                onClick = onCreateClick,
                enabled = !loading
            ) {
                Text("Создать группу")
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    tint: Color,
    boxColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6FC)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth(0.18f)
                    .background(color = boxColor, shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2F2B3A)
            )
        }
    }
}

@Composable
private fun GroupEditorDialog(
    title: String,
    initialName: String = "",
    initialCurrency: String = "RUB",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var currency by remember { mutableStateOf(initialCurrency) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    if (name.isBlank() || currency.isBlank()) return@FilledTonalButton
                    onSave(name.trim(), currency.trim().uppercase())
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
                    label = { Text("Название группы") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Базовая валюта") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    )
}

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.hrach.financeapp.data.dto.CategoryDto
import com.hrach.financeapp.ui.utils.categoryIcon
import com.hrach.financeapp.ui.utils.toCategoryTypeLabel
import com.hrach.financeapp.viewmodel.HomeViewModel

@Composable
fun CategoriesScreen(
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val expenses = categories.filter { it.type == "EXPENSE" }
    val incomes = categories.filter { it.type == "INCOME" }
    var showCreate by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryDto?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryDto?>(null) }

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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color(0xFF454545)
                    )
                }

                Text(
                    text = "Категории",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF23212B)
                )
            }
        }

        item {
            FilledTonalButton(onClick = { showCreate = true }) {
                Text("Добавить категорию")
            }
        }

        item { Text("Расходы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(expenses) { item -> CategoryRow(item, { editTarget = item }, { deleteTarget = item }) }
        item { Text("Доходы", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(incomes) { item -> CategoryRow(item, { editTarget = item }, { deleteTarget = item }) }
    }

    if (showCreate) {
        CategoryEditorDialog(
            title = "Новая категория",
            onDismiss = { showCreate = false },
            onSave = { name, type, iconKey ->
                viewModel.createCategory(name, type, iconKey)
                showCreate = false
            }
        )
    }

    editTarget?.let { category ->
        CategoryEditorDialog(
            title = "Редактировать категорию",
            initialName = category.name,
            initialType = category.type,
            initialIconKey = category.iconKey,
            onDismiss = { editTarget = null },
            onSave = { name, type, iconKey ->
                viewModel.updateCategory(category, name, type, iconKey)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { category ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteCategory(category.id)
                    deleteTarget = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Отмена") } },
            title = { Text("Удалить категорию?") },
            text = { Text("Категория ${category.name} будет удалена.") }
        )
    }
}

@Composable
private fun CategoryRow(item: CategoryDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4EDF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = categoryIcon(item.iconKey),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text(item.type.toCategoryTypeLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Редактировать") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.DeleteOutline, "Удалить") }
        }
    }
}

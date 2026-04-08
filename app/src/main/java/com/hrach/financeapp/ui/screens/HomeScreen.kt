package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.hrach.financeapp.ui.components.SummaryHeroCard
import com.hrach.financeapp.ui.components.TransactionCard
import com.hrach.financeapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
    onOpenCategories: () -> Unit
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = groups.firstOrNull { it.id == selectedGroupId }?.name ?: "Выбери группу"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCCCFDF),
                        Color(0xFFEFD6EF),
                        Color(0xFFABA7CE)
                    )
                )
            )
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Главная",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF23212B)
            )
        }

        item {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedLabel,
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
            Card(
                onClick = onOpenCategories,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F6FC)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth(0.18f)
                            .background(
                                color = Color(0xFFF1E7FB),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalOffer,
                            contentDescription = null,
                            tint = Color(0xFF5E4B8B)
                        )
                    }

                    Text(
                        text = "Категории",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2F2B3A)
                    )
                }
            }
        }

        item { androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp)) }
    }
}

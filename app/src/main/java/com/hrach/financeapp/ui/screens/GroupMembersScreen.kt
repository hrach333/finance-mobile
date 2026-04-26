package com.hrach.financeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.TopAppBar
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
import com.hrach.financeapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMembersScreen(viewModel: HomeViewModel, paddingValues: PaddingValues, onBack: () -> Unit) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val selectedGroupId by viewModel.selectedGroupId.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("member") }
    var roleExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Участники группы") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF5F0FF), Color(0xFFF8F6FC), Color(0xFFE6DFEA))
                    )
                )
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

        if (isOfflineMode) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Совместный бюджет начинается с аккаунта", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Сейчас бюджет хранится только на этом устройстве. Зарегистрируйтесь, чтобы приглашать близких, вести общий бюджет вместе и не потерять данные при смене телефона.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (selectedGroupId == null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Сначала создай или выбери группу", fontWeight = FontWeight.SemiBold)
                        Text("Пока группа не выбрана, добавление участников недоступно.")
                    }
                }
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email пользователя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = !roleExpanded }) {
                        OutlinedTextField(
                            value = role,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Роль") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        androidx.compose.material3.DropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                            listOf("member", "admin").forEach { itemRole ->
                                DropdownMenuItem(
                                    text = { Text(itemRole) },
                                    onClick = {
                                        role = itemRole
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.addMember(email.trim(), role)
                            email = ""
                            role = "member"
                        },
                        enabled = email.isNotBlank() && !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить участника")
                    }
                }
            }
        }

        if (!error.isNullOrBlank()) {
            item { Text(error.orEmpty(), color = MaterialTheme.colorScheme.error) }
        }

        items(members) { member ->
            var itemRole by remember(member.id) { mutableStateOf(member.role) }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(member.user?.name ?: member.user?.email ?: "Участник #${member.id}", fontWeight = FontWeight.SemiBold)
                    Text(member.user?.email ?: "Email не получен")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            itemRole = if (itemRole == "admin") "member" else "admin"
                            viewModel.updateMemberRole(member.id, itemRole)
                        }) {
                            Text(if (itemRole == "admin") "Сделать member" else "Сделать admin")
                        }
                        TextButton(onClick = { viewModel.deleteMember(member.id) }) {
                            Text("Удалить")
                        }
                    }
                    Text("Текущая роль: $itemRole")
                }
            }
        }
        }
    }
}

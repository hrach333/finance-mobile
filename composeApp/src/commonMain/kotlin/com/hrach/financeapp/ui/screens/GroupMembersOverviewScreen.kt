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
import com.hrach.financeapp.data.model.FinanceOverview
import com.hrach.financeapp.data.model.GroupMemberOverview

@Composable
fun GroupMembersOverviewScreen(
    overview: FinanceOverview,
    onAddMember: (String, String) -> Unit,
    onUpdateMemberRole: (GroupMemberOverview, String) -> Unit,
    onDeleteMember: (GroupMemberOverview) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("member") }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader(title = "Участники", subtitle = overview.activeGroupName)
        }

        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color(0xFFF9F6FC),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                elevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                        },
                        label = { Text("Email пользователя") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoleButton("member", role, "member") { role = it }
                        RoleButton("admin", role, "admin") { role = it }
                    }
                    Button(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                error = "Введите email"
                            } else {
                                onAddMember(email.trim(), role)
                                email = ""
                                role = "member"
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5E4B8B), contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Добавить участника")
                    }
                    error?.let {
                        Text(text = it, color = Color(0xFFE85B6A), style = MaterialTheme.typography.body2)
                    }
                }
            }
        }

        items(overview.members) { member ->
            GroupMemberCard(
                member = member,
                onToggleRole = {
                    val newRole = if (member.role == "admin") "member" else "admin"
                    onUpdateMemberRole(member, newRole)
                },
                onDelete = { onDeleteMember(member) }
            )
        }
    }
}

@Composable
private fun GroupMemberCard(
    member: GroupMemberOverview,
    onToggleRole: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(
                    icon = FinanceIcon.Group,
                    contentDescription = "Участник",
                    onClick = {},
                    enabled = false,
                    size = 44.dp,
                    background = AppLilac,
                    contentColor = AppPurple
                )
                Column {
                    Text(member.userName ?: member.userEmail ?: "Участник #${member.id}", color = Color(0xFF2F2B3A), fontWeight = FontWeight.Bold)
                    Text(member.userEmail ?: "Email не получен", color = Color(0xFF6B6579), style = MaterialTheme.typography.body2)
                    Text("Роль: ${member.role}", color = Color(0xFF5E4B8B), fontWeight = FontWeight.SemiBold)
                }
            }
            Divider(color = Color.White.copy(alpha = 0.7f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                RoundIconButton(FinanceIcon.Settings, "Изменить роль", onToggleRole, size = 36.dp)
                RoundIconButton(FinanceIcon.Delete, "Удалить участника", onDelete, size = 36.dp, background = Color(0xFFFFE7EC), contentColor = AppRed)
            }
        }
    }
}

@Composable
private fun RoleButton(
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
